using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Diagnostics.Contracts;
using System.IO;
using System.Text;
using NGS.Common;
using NGS.DatabasePersistence.Oracle.Converters;
using NGS.Logging;
using Oracle.DataAccess.Client;

namespace NGS.DatabasePersistence.Oracle
{
	public interface IOracleDatabaseQuery : IDatabaseQuery
	{
		void Notify(OracleNotifyInfoConverter[] notifiers, string target);
	}

	public class OracleDatabaseQuery : IOracleDatabaseQuery, IDisposable
	{
		public static int MinBatchSize { get; private set; }
		private static readonly OracleAQAgent[] Recipients;

		static OracleDatabaseQuery()
		{
			MinBatchSize = 1000;
			var mbs = ConfigurationManager.AppSettings["Database.MinBatchSize"];
			int n;
			if (!string.IsNullOrEmpty(mbs) && int.TryParse(mbs, out n))
				MinBatchSize = n;
			var rec = new List<OracleAQAgent>();
			rec.Add(new OracleAQAgent(ConfigurationManager.AppSettings["Oracle.QueueConsumer"] ?? "Local"));
			foreach (string k in ConfigurationManager.AppSettings.Keys)
				if (k.StartsWith("Oracle.Recipient"))
					rec.Add(new OracleAQAgent(ConfigurationManager.AppSettings[k]));
			Recipients = rec.ToArray();
		}

		private readonly string ConnectionString;
		private OracleConnection Connection;
		private readonly OracleTransaction Transaction;

		private readonly ILogFactory LogFactory;

		private readonly object sync = new object();
		private readonly Lazy<OracleAQQueue> Queue;
		private bool BrokenTransaction;

		public OracleDatabaseQuery(
			OracleConnection connection,
			OracleTransaction transaction,
			ILogFactory logFactory)
		{
			Contract.Requires(connection != null);
			Contract.Requires(logFactory != null);

			this.Connection = connection;
			this.ConnectionString = connection.ConnectionString;
			this.Transaction = transaction;
			this.LogFactory = logFactory;
			Queue =
				new Lazy<OracleAQQueue>(() =>
				{
					var q = new OracleAQQueue("\"-NGS-\".NOTIFY_QUEUE", Connection, OracleAQMessageType.Udt, "-NGS-.NOTIFY_INFO_TYPE");
					q.EnqueueOptions.Visibility = OracleAQVisibilityMode.OnCommit;
					q.EnqueueOptions.DeliveryMode = OracleAQMessageDeliveryMode.Persistent;
					return q;
				});
		}

		public IDbCommand NewCommand()
		{
			return new OracleCommand { BindByName = true };
		}

		public bool InTransaction
		{
			get
			{
				return Transaction != null && (BrokenTransaction || Transaction.Connection != null);
			}
		}

		private static FrameworkException FormatException(OracleException ex, IDbCommand command)
		{
			return new FrameworkException("Error executing command: " + ex.Message + Environment.NewLine + command.CommandText, ex);
		}

		private void PrepareCommand(IDbCommand command)
		{
			try
			{
				if (Transaction == null && Connection.State == ConnectionState.Closed)
				{
					if (Connection.ConnectionString != ConnectionString)
						Connection.ConnectionString = ConnectionString;
					Connection.Open();
				}
			}
			catch (Exception ex)
			{
				LogFactory.Create("Oracle database layer - prepare command").Error(ex.ToString());
				try { Connection.Close(); }
				catch { }
				try
				{
					OracleConnection.ClearAllPools();
				}
				catch (Exception cpex)
				{
					LogFactory.Create("Oracle database layer - clear pools").Error(cpex.ToString());
				}
				if (Connection.State == ConnectionState.Closed)
				{
					if (Connection.ConnectionString != ConnectionString)
						Connection.ConnectionString = ConnectionString;
					Connection.Open();
				}
			}
			command.Connection = Connection;
			command.Transaction = Transaction;
		}

		private void ResetConnection()
		{
			try
			{
				Connection.Close();
				Connection.Dispose();
			}
			catch (Exception ex)
			{
				LogFactory.Create("Oracle database layer - reset connection").Error(ex.ToString());
				try
				{
					OracleConnection.ClearAllPools();
				}
				catch (Exception cpex)
				{
					LogFactory.Create("Oracle database layer - clear pools").Error(cpex.ToString());
				}
			}
			Connection = new OracleConnection(ConnectionString);
		}

		public int ExecuteNonQuery(StringBuilder query)
		{
			var com = new OracleCommand(query.ToString(), Connection);
			return Execute(com);
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
			catch (OracleException ex)
			{
				BrokenTransaction = !tryRecover;
				var logger = LogFactory.Create("Oracle database layer - execute non query");
				logger.Trace(command.CommandText);
				logger.Error(ex.ToString());
				if (tryRecover && ex.InnerException is IOException)
				{
					ResetConnection();
					return ExecuteNonQuery(command, false);
				}
				else throw FormatException(ex, command);
			}
			catch (Exception ex)
			{
				LogFactory.Create("Oracle database layer - execute non query").Error(ex.ToString());
				if (tryRecover)
				{
					ResetConnection();
					return ExecuteNonQuery(command, false);
				}
				else throw;
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
			catch (OracleException ex)
			{
				BrokenTransaction = !tryRecover;
				var logger = LogFactory.Create("Oracle database layer - execute non query");
				logger.Trace(command.CommandText);
				logger.Error(ex.ToString());
				if (tryRecover && ex.InnerException is IOException && !hasRead)
				{
					ResetConnection();
					ExecuteDataReader(command, action, false);
				}
				else throw FormatException(ex, command);
			}
			catch (Exception ex)
			{
				LogFactory.Create("Oracle database layer - execute data reader").Error(ex.ToString());
				if (tryRecover && !hasRead)
				{
					ResetConnection();
					ExecuteDataReader(command, action, false);
				}
				else throw;
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
					LogFactory.Create("Oracle database layer - execute data reader").Error(ex.ToString());
				}
				command.Transaction = null;
				command.Connection = null;
			}
		}

		public int Fill(IDbCommand command, DataSet ds)
		{
			if (command is OracleCommand)
				return FillDataSet(command as OracleCommand, ds, Transaction == null);

			return FillDataSet(CopyCommand(command), ds, Transaction == null);
		}

		private OracleCommand CopyCommand(IDbCommand command)
		{
			var sqlCom = new OracleCommand(command.CommandText);
			sqlCom.CommandTimeout = command.CommandTimeout;
			sqlCom.CommandType = command.CommandType;
			foreach (IDataParameter par in command.Parameters)
			{
				var npgPar = new OracleParameter(par.ParameterName, par.DbType);
				npgPar.Direction = par.Direction;
				npgPar.Value = par.Value;
				sqlCom.Parameters.Add(npgPar);
			}
			return sqlCom;
		}

		private int FillDataSet(OracleCommand command, DataSet ds, bool tryRecover)
		{
			try
			{
				lock (Transaction ?? sync)
				{
					command.Connection = Connection;
					command.Transaction = Transaction;
					var da = new OracleDataAdapter(command);
					return da.Fill(ds);
				}
			}
			catch (OracleException ex)
			{
				BrokenTransaction = !tryRecover;
				var logger = LogFactory.Create("Oracle database layer - fill table");
				logger.Trace(command.CommandText);
				logger.Error(ex.ToString());
				if (tryRecover && ex.InnerException is IOException)
				{
					ResetConnection();
					return FillDataSet(command, ds, false);
				}
				else throw FormatException(ex, command);
			}
			catch (Exception ex)
			{
				LogFactory.Create("Oracle database layer - fill table").Error(ex.ToString());
				if (tryRecover)
				{
					ResetConnection();
					return FillDataSet(command, ds, false);
				}
				else throw;
			}
			finally
			{
				command.Transaction = null;
				command.Connection = null;
			}
		}

		//TODO: think about moving this just before transaction commit so Oracle can be switched to in memory queue
		//TODO: which doesn't need to be persisted. invalid notify should only result in more processing
		public void Notify(OracleNotifyInfoConverter[] notifiers, string target)
		{
			if (notifiers == null || notifiers.Length == 0)
				return;
			var msgs = new OracleAQMessage[notifiers.Length];
			var sender = new OracleAQAgent(target);
			for (int i = 0; i < msgs.Length; i++)
				msgs[i] = new OracleAQMessage(notifiers[i]) { SenderId = sender, Recipients = Recipients };
			Queue.Value.EnqueueArray(msgs);
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
				LogFactory.Create("Oracle database layer - dispose").Error(ex.ToString());
			}
		}
	}
}
