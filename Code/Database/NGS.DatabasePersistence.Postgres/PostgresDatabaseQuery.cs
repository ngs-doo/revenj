using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Diagnostics.Contracts;
using System.IO;
using System.Linq;
using System.Text;
using NGS.Common;
using NGS.DomainPatterns;
using NGS.Logging;
using Npgsql;

namespace NGS.DatabasePersistence.Postgres
{
	public interface IPostgresDatabaseQuery : IDatabaseQuery
	{
		void BulkInsert(string table, IEnumerable<Stream> data);
		T[] Search<T>(ISpecification<T> filter, int? limit, int? offset, IEnumerable<KeyValuePair<string, bool>> order, Func<IDataReader, T> converter);
		long Count<T>(ISpecification<T> filter);
	}

	public class PostgresDatabaseQuery : IPostgresDatabaseQuery, IDisposable
	{
		public static int MinBatchSize { get; private set; }
		public static long MaxObjectSize { get; private set; }

		static PostgresDatabaseQuery()
		{
			MinBatchSize = 1000;
			MaxObjectSize = 1024 * 1024;
			var mbs = ConfigurationManager.AppSettings["Database.MinBatchSize"];
			int n;
			if (!string.IsNullOrEmpty(mbs) && int.TryParse(mbs, out n))
				MinBatchSize = n;
			mbs = ConfigurationManager.AppSettings["Database.MaxObjectSize"];
			long m;
			if (!string.IsNullOrEmpty(mbs) && long.TryParse(mbs, out m))
				MaxObjectSize = m;
		}

		private NpgsqlConnection Connection;
		private readonly NpgsqlTransaction Transaction;
		private bool BrokenTransaction;

		private readonly ILogFactory LogFactory;

		private readonly object sync = new object();

		public PostgresDatabaseQuery(
			NpgsqlConnection connection,
			NpgsqlTransaction transaction,
			ILogFactory logFactory)
		{
			Contract.Requires(connection != null);
			Contract.Requires(logFactory != null);

			this.Connection = connection;
			this.Transaction = transaction;
			this.LogFactory = logFactory;
		}

		public IDbCommand NewCommand()
		{
			return new NpgsqlCommand();
		}

		public bool InTransaction
		{
			get
			{
				return Transaction != null && (BrokenTransaction || Transaction.Connection != null);
			}
		}

		private static FrameworkException FormatException(NpgsqlException ex)
		{
			var details = string.IsNullOrEmpty(ex.Detail) ? string.Empty : @"
Error: " + (ex.Detail.Contains("\r\n") ? ex.Detail : ex.Detail.Replace("\n", "\r\n"));
			if (!string.IsNullOrEmpty(ex.ErrorSql))
				return new FrameworkException(ex.Message + details + @"
SQL: " + ex.ErrorSql.Substring(0, Math.Min(ex.ErrorSql.Length, 200)), ex);
			if (!string.IsNullOrEmpty(ex.Where))
				return new FrameworkException(ex.Message + details + @"
Near: " + ex.Where, ex);
			return new FrameworkException(ex.Message + details, ex);
		}

		private void PrepareCommand(IDbCommand command)
		{
			try
			{
				if (Transaction == null && Connection.State == ConnectionState.Closed)
					Connection.Open();
			}
			catch (Exception ex)
			{
				var logger = LogFactory.Create("Postgres database layer - prepare command");
				logger.Trace("{0}:{1} - {2}".With(Connection.Host, Connection.Port, Connection.Database));
				logger.Error(ex.ToString());
				try { Connection.Close(); }
				catch { }
				NpgsqlConnection.ClearAllPools();
				//TODO new connection!?
				if (Connection.State == ConnectionState.Closed)
					Connection.Open();
			}
			command.Connection = Connection;
			command.Transaction = Transaction;
		}

		private void ResetConnection()
		{
			var cs = Connection.ConnectionString;
			if (Transaction != null)
				throw new FrameworkException("Can't reset connection inside transaction");
			lock (sync)
			{
				try
				{
					Connection.Close();
					Connection.Dispose();
				}
				catch (Exception ex)
				{
					LogFactory.Create("Postgres database layer - reset connection").Error(ex.ToString());
					NpgsqlConnection.ClearAllPools();
				}
				Connection = new NpgsqlConnection(cs);
			}
		}

		private static bool ShouldTryRecover(Exception ex)
		{
			return ex.InnerException is IOException
				|| ex.Message.StartsWith("A timeout has occured.");
		}

		public int Execute(IDbCommand command)
		{
			return ExecuteNonQuery(command, Transaction == null);
		}

		private int ExecuteNonQuery(IDbCommand command, bool tryRecover)
		{
			try
			{
				lock (Transaction ?? sync)
				{
					PrepareCommand(command);
					return command.ExecuteNonQuery();
				}
			}
			catch (NpgsqlException ex)
			{
				BrokenTransaction = !tryRecover;
				var logger = LogFactory.Create("Postgres database layer - execute non query");
				logger.Trace("{0}:{1} - {2}".With(Connection.Host, Connection.Port, Connection.Database));
				logger.Trace(command.CommandText);
				logger.Error(ex.ToString());
				if (ex.ErrorSql != null)
					logger.Error(ex.ErrorSql);
				if (tryRecover && ShouldTryRecover(ex))
				{
					ResetConnection();
					return ExecuteNonQuery(command, false);
				}
				else
				{
					if (Transaction == null)
						ResetConnection();
					throw FormatException(ex);
				}
			}
			catch (Exception ex)
			{
				LogFactory.Create("Postgres database layer - execute non query").Error(ex.ToString());
				if (tryRecover)
				{
					ResetConnection();
					return ExecuteNonQuery(command, false);
				}
				else
				{
					if (Transaction == null)
						ResetConnection();
					throw;
				}
			}
			finally
			{
				command.Transaction = null;
				command.Connection = null;
			}
		}

		public void Execute(IDbCommand command, Action<IDataReader> action)
		{
			ExecuteDataReader(command, action, Transaction == null);
		}

		private void ExecuteDataReader(IDbCommand command, Action<IDataReader> action, bool tryRecover)
		{
			IDataReader dr = null;
			bool hasRead = false;
			try
			{
				lock (Transaction ?? sync)
				{
					PrepareCommand(command);
					dr = command.ExecuteReader();
					while (dr.Read())
					{
						hasRead = true;
						action(dr);
					}
				}
			}
			catch (NpgsqlException ex)
			{
				BrokenTransaction = !tryRecover;
				var logger = LogFactory.Create("Postgres database layer - execute data reader");
				logger.Trace("{0}:{1} - {2}".With(Connection.Host, Connection.Port, Connection.Database));
				logger.Trace(command.CommandText);
				logger.Error(ex.ToString());
				if (ex.ErrorSql != null)
					logger.Error(ex.ErrorSql);
				if (tryRecover && !hasRead && ShouldTryRecover(ex))
				{
					ResetConnection();
					ExecuteDataReader(command, action, false);
				}
				else
				{
					if (Transaction == null)
						ResetConnection();
					throw FormatException(ex);
				}
			}
			catch (Exception ex)
			{
				LogFactory.Create("Postgres database layer - execute data reader").Error(ex.ToString());
				if (tryRecover && !hasRead)
				{
					ResetConnection();
					ExecuteDataReader(command, action, false);
				}
				else
				{
					if (Transaction == null)
						ResetConnection();
					throw;
				}
			}
			finally
			{
				try
				{
					if (dr != null)
						dr.Close();
				}
				catch (Exception ex)
				{
					LogFactory.Create("Postgres database layer - execute data reader - closing").Error(ex.ToString());
				}
				command.Transaction = null;
				command.Connection = null;
			}
		}

		public int Fill(IDbCommand command, DataSet ds)
		{
			if (command is NpgsqlCommand)
				return FillDataSet(command as NpgsqlCommand, ds, Transaction == null);

			return FillDataSet(CopyCommand(command), ds, Transaction == null);
		}

		private NpgsqlCommand CopyCommand(IDbCommand command)
		{
			var npgCom = new NpgsqlCommand(command.CommandText);
			npgCom.CommandTimeout = command.CommandTimeout;
			npgCom.CommandType = command.CommandType;
			foreach (IDataParameter par in command.Parameters)
			{
				var npgPar = new NpgsqlParameter(par.ParameterName, par.DbType);
				npgPar.Direction = par.Direction;
				npgPar.Value = par.Value;
				npgCom.Parameters.Add(npgPar);
			}
			return npgCom;
		}

		private int FillDataSet(NpgsqlCommand command, DataSet ds, bool tryRecover)
		{
			try
			{
				lock (Transaction ?? sync)
				{
					command.Connection = Connection;
					command.Transaction = Transaction;
					var da = new NpgsqlDataAdapter(command);
					return da.Fill(ds);
				}
			}
			catch (NpgsqlException ex)
			{
				BrokenTransaction = !tryRecover;
				var logger = LogFactory.Create("Postgres database layer - fill table");
				logger.Trace("{0}:{1} - {2}".With(Connection.Host, Connection.Port, Connection.Database));
				logger.Trace(command.CommandText);
				logger.Error(ex.ToString());
				if (ex.ErrorSql != null)
					logger.Error(ex.ErrorSql);
				if (tryRecover && ShouldTryRecover(ex))
				{
					ResetConnection();
					return FillDataSet(command, ds, false);
				}
				else
				{
					if (Transaction == null)
						ResetConnection();
					throw FormatException(ex);
				}
			}
			catch (Exception ex)
			{
				LogFactory.Create("Postgres database layer - fill table").Error(ex.ToString());
				if (tryRecover)
				{
					ResetConnection();
					return FillDataSet(command, ds, false);
				}
				else
				{
					if (Transaction == null)
						ResetConnection();
					throw;
				}
			}
			finally
			{
				command.Transaction = null;
				command.Connection = null;
			}
		}

		public void BulkInsert(string table, IEnumerable<Stream> data)
		{
			var copy = new NpgsqlCopyIn("COPY " + table + " FROM STDIN DELIMITER '\t'", Connection);
			copy.Start();
			foreach (var it in data)
			{
				it.CopyTo(copy.CopyStream);
				copy.CopyStream.WriteByte((byte)'\n');
				it.Dispose();
			}
			copy.End();
		}

		private NpgsqlCommand AddSource<T>(string select, ISpecification<T> filter)
		{
			var command = Connection.CreateCommand();
			command.Transaction = Transaction;
			if (filter != null)
			{
				var type = filter.GetType();
				var ctor = type.GetConstructors()[0];
				command.CommandText =
					@"SELECT {0} FROM ""{1}"".""{2}.{3}""({4}) it".With(
						select,
						type.DeclaringType.Namespace,
						type.DeclaringType.Name,
						type.Name,
						string.Join(", ", ctor.GetParameters().Select((it, ind) => ":p" + ind)));
				var cnt = 0;
				foreach (var p in ctor.GetParameters())
				{
					command.Parameters.AddWithValue(":p" + cnt, type.GetProperty(p.Name).GetValue(filter, null));
					cnt++;
				}
			}
			else
			{
				var source = SqlSourceAttribute.FindSource(typeof(T));
				if (source == null)
					throw new FrameworkException("Unknown data source for type: " + typeof(T).FullName + ". Only queryable types can be used as data source");
				command.CommandText = "SELECT {0} FROM {1} it".With(select, source);
			}
			return command;
		}

		private static string GetSortPath(Type type, string path, bool ascending)
		{
			var sb = new StringBuilder("it");
			var props = path.Split('.');

			foreach (var prop in props)
			{
				var pi = type.GetProperty(prop);
				if (pi == null)
				{
					var msg = "Unknown property: {0} on type {1}".With(prop, type.FullName);
					if (prop != path)
						msg += " for path " + path;
					throw new ArgumentException(msg);
				}
				type = pi.PropertyType;
				sb.Insert(0, '(');
				sb.AppendFormat(").\"{0}\"", prop);
			}
			return sb.ToString() + (ascending ? string.Empty : " DESC");
		}

		private object ExecuteScalar(NpgsqlCommand command, bool tryRecover)
		{
			try
			{
				lock (Transaction ?? sync)
				{
					return command.ExecuteScalar();
				}
			}
			catch (NpgsqlException ex)
			{
				BrokenTransaction = !tryRecover;
				var logger = LogFactory.Create("Postgres database layer - execute scalar");
				logger.Trace(command.CommandText);
				logger.Error(ex.ToString());
				logger.Error(ex.ErrorSql);
				if (tryRecover && ShouldTryRecover(ex))
				{
					ResetConnection();
					return ExecuteScalar(command, false);
				}
				else
				{
					if (Transaction == null)
						ResetConnection();
					throw FormatException(ex);
				}
			}
			catch (Exception ex)
			{
				LogFactory.Create("Postgres database layer - execute data reader").Error(ex.ToString());
				if (tryRecover)
				{
					ResetConnection();
					return ExecuteScalar(command, false);
				}
				else
				{
					if (Transaction == null)
						ResetConnection();
					throw;
				}
			}
		}

		public T[] Search<T>(
			ISpecification<T> filter,
			int? limit,
			int? offset,
			IEnumerable<KeyValuePair<string, bool>> order,
			Func<IDataReader, T> converter)
		{
			var command = AddSource<T>("it", filter);
			var sort = new List<string>();
			var type = typeof(T);
			if (order != null)
			{
				foreach (var kv in order)
					sort.Add(GetSortPath(type, kv.Key, kv.Value));
				if (sort.Count > 0)
					command.CommandText += " ORDER BY " + string.Join(", ", sort);
			}
			if (limit != null)
				command.CommandText += " LIMIT " + limit.Value;
			if (offset != null)
				command.CommandText += " OFFSET " + offset.Value;
			var list = new List<T>();
			Execute(command, dr => list.Add(converter(dr)));
			return list.ToArray();
		}

		public long Count<T>(ISpecification<T> filter)
		{
			var command = AddSource<T>("COUNT(*)", filter);
			return (long)ExecuteScalar(command, true);
		}

		public void Dispose()
		{
			try
			{
				if (Connection != null && Connection.State == ConnectionState.Open)
					Connection.Close();
			}
			catch (Exception ex)
			{
				LogFactory.Create("Postgres database layer - dispose").Error(ex.ToString());
			}
		}
	}
}
