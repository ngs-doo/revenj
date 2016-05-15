using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.IO;
using System.Linq;
using System.Text;
using Revenj.Common;
using Revenj.DatabasePersistence.Postgres.Converters;
using Revenj.DatabasePersistence.Postgres.Npgsql;
using Revenj.DomainPatterns;

namespace Revenj.DatabasePersistence.Postgres
{
	public interface IPostgresDatabaseQuery : IDatabaseQuery
	{
		void BulkInsert(string table, IEnumerable<IPostgresTuple[]> data);
		T[] Search<T>(ISpecification<T> filter, int? limit, int? offset, IEnumerable<KeyValuePair<string, bool>> order, Func<IDataReader, T> converter);
		long Count<T>(ISpecification<T> filter);
	}

	public static class PostgresCommandFactory
	{
		public static IDbCommand NewCommand(Stream stream)
		{
			return new NpgsqlCommand(stream) { CommandTimeout = ConnectionInfo.LastCommandTimeout };
		}

		public static IDbCommand NewCommand(Stream stream, string template)
		{
			return new NpgsqlCommand(stream, template) { CommandTimeout = ConnectionInfo.LastCommandTimeout };
		}

		public static IDbCommand PreparedCommand(Stream stream, string name, string template, string types)
		{
			return new NpgsqlCommand(stream, template, name, types) { CommandTimeout = ConnectionInfo.LastCommandTimeout };
		}

		public static IDbCommand NewCommand(Stream stream, string template, bool sequential)
		{
			return new NpgsqlCommand(stream, template)
			{
				CommandTimeout = ConnectionInfo.LastCommandTimeout,
				ReaderBehavior = sequential ? CommandBehavior.SequentialAccess : CommandBehavior.Default
			};
		}
	}

	internal class PostgresDatabaseQuery : IPostgresDatabaseQuery, IDisposable
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Database");

		private NpgsqlConnection Connection;
		private readonly NpgsqlTransaction Transaction;
		private bool BrokenTransaction;
		private bool DifferentConnection;

		private readonly object sync = new object();

		public PostgresDatabaseQuery(NpgsqlConnection connection, NpgsqlTransaction transaction)
		{
			Contract.Requires(connection != null);

			this.Connection = connection;
			this.Transaction = transaction;
		}

		public IDbCommand NewCommand()
		{
			return new NpgsqlCommand { CommandTimeout = Connection.CommandTimeout };
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

		private void LogError(IDbCommand command, int error, NpgsqlException ex)
		{
			TraceSource.TraceEvent(TraceEventType.Verbose, error, command.CommandText);
			TraceSource.TraceEvent(TraceEventType.Information, error, "{0}:{1} - {2}", Connection.Host, Connection.Port, Connection.Database);
			TraceSource.TraceEvent(TraceEventType.Error, error, "{0}", ex);
			if (ex.ErrorSql != null)
				TraceSource.TraceEvent(TraceEventType.Error, error, ex.ErrorSql);
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
				TraceSource.TraceEvent(TraceEventType.Error, 5110, "{0}", ex);
				TraceSource.TraceEvent(TraceEventType.Information, 5110, "{0}:{1} - {2}", Connection.Host, Connection.Port, Connection.Database);
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
					TraceSource.TraceEvent(TraceEventType.Error, 5111, "{0}", ex);
					NpgsqlConnection.ClearAllPools();
				}
				Connection = new NpgsqlConnection(cs);
				DifferentConnection = true;
			}
		}

		private static bool ShouldTryRecover(NpgsqlException ex)
		{
			return ex.InnerException is IOException
				|| ex.Message.StartsWith("A timeout has occured.");//Explicit typo
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
				LogError(command, 5112, ex);
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
			catch (PostgresException ex)
			{
				TraceSource.TraceEvent(TraceEventType.Critical, 5113, "{0}", ex);
				throw;
			}
			catch (Exception ex)
			{
				if (tryRecover)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 5114, "{0}", ex);
					ResetConnection();
					return ExecuteNonQuery(command, false);
				}
				else
				{
					TraceSource.TraceEvent(TraceEventType.Critical, 5114, "{0}", ex);
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
			bool hasRead = false;
			try
			{
				var npg = command as NpgsqlCommand;
				var behavior = npg != null ? npg.ReaderBehavior : CommandBehavior.Default;
				lock (Transaction ?? sync)
				{
					PrepareCommand(command);
					using (var dr = command.ExecuteReader(behavior))
					{
						while (dr.Read())
						{
							hasRead = true;
							action(dr);
						}
					}
				}
			}
			catch (NpgsqlException ex)
			{
				BrokenTransaction = !tryRecover;
				LogError(command, 5115, ex);
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
			catch (PostgresException ex)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5116, "{0}", ex);
				throw;
			}
			catch (Exception ex)
			{
				if (tryRecover && !hasRead)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 5117, "{0}", ex);
					ResetConnection();
					ExecuteDataReader(command, action, false);
				}
				else
				{
					TraceSource.TraceEvent(TraceEventType.Critical, 5117, "{0}", ex);
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
				LogError(command, 5118, ex);
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
			catch (PostgresException ex)
			{
				TraceSource.TraceEvent(TraceEventType.Critical, 5119, "{0}", ex);
				throw;
			}
			catch (Exception ex)
			{
				if (tryRecover)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 5120, "{0}", ex);
					ResetConnection();
					return FillDataSet(command, ds, false);
				}
				else
				{
					TraceSource.TraceEvent(TraceEventType.Critical, 5120, "{0}", ex);
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

		public void BulkInsert(string table, IEnumerable<IPostgresTuple[]> data)
		{
			if (!InTransaction) throw new FrameworkException("BulkInsert can only be used within transaction");
			using (var cms = Revenj.Utility.ChunkedMemoryStream.Create())
			{
				var sw = cms.GetWriter();
				var buf = cms.SmallBuffer;
				foreach (var it in data)
				{
					var p = it[0];
					if (p != null)
						p.InsertRecord(sw, buf, string.Empty, PostgresTuple.EscapeBulkCopy);
					else
						sw.Write("\\N");
					for (int i = 1; i < it.Length; i++)
					{
						sw.Write('\t');
						p = it[i];
						if (p != null)
							p.InsertRecord(sw, buf, string.Empty, PostgresTuple.EscapeBulkCopy);
						else
							sw.Write("\\N");
					}
					sw.Write('\n');
				}
				sw.Flush();
				cms.Position = 0;
				var copy = new NpgsqlCopyIn("COPY " + table + " FROM STDIN DELIMITER '\t'", Connection);
				copy.Start();
				cms.CopyTo(copy.CopyStream);
				copy.End();
			}
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
				LogError(command, 5121, ex);
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
			catch (PostgresException ex)
			{
				TraceSource.TraceEvent(TraceEventType.Critical, 5122, "{0}", ex);
				throw;
			}
			catch (Exception ex)
			{
				if (tryRecover)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 5123, "{0}", ex);
					ResetConnection();
					return ExecuteScalar(command, false);
				}
				else
				{
					TraceSource.TraceEvent(TraceEventType.Critical, 5123, "{0}", ex);
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
				if (DifferentConnection && Connection.State == ConnectionState.Open)
					Connection.Close();
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5124, "{0}", ex);
			}
		}
	}
}
