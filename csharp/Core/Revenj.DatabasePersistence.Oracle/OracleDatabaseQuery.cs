using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.IO;
using System.Text;
using Oracle.DataAccess.Client;
using Revenj.Common;
using Revenj.DatabasePersistence.Oracle.Converters;

namespace Revenj.DatabasePersistence.Oracle
{
	public interface IOracleDatabaseQuery : IDatabaseQuery
	{
		void Notify(OracleNotifyInfoConverter[] notifiers, string target);
	}

	internal class OracleDatabaseQuery : IOracleDatabaseQuery, IDisposable
	{
		private static readonly OracleAQAgent[] Recipients;
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Database");

		static OracleDatabaseQuery()
		{
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

		private readonly object sync = new object();
		private readonly Lazy<OracleAQQueue> Queue;
		private bool BrokenTransaction;

		public OracleDatabaseQuery(
			OracleConnection connection,
			OracleTransaction transaction)
		{
			Contract.Requires(connection != null);

			this.Connection = connection;
			this.ConnectionString = connection.ConnectionString;
			this.Transaction = transaction;
			Queue =
				new Lazy<OracleAQQueue>(() =>
				{
					var q = new OracleAQQueue("\"-DSL-\".NOTIFY_QUEUE", Connection, OracleAQMessageType.Udt, "-DSL-.NOTIFY_INFO_TYPE");
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
				TraceSource.TraceEvent(TraceEventType.Error, 5110, "{0}", ex);
				try { Connection.Close(); }
				catch { }
				try { OracleConnection.ClearAllPools(); }
				catch (Exception cpex)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 5111, "{0}", cpex);
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
				TraceSource.TraceEvent(TraceEventType.Error, 5112, "{0}", ex);
				try
				{
					OracleConnection.ClearAllPools();
				}
				catch (Exception cpex)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 5113, "{0}", cpex);
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
				TraceSource.TraceEvent(TraceEventType.Verbose, 5114, command.CommandText);
				TraceSource.TraceEvent(TraceEventType.Error, 5114, "{0}", ex);
				if (tryRecover && ex.InnerException is IOException)
				{
					ResetConnection();
					return ExecuteNonQuery(command, false);
				}
				else throw FormatException(ex, command);
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5115, "{0}", ex);
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
			bool hasRead = false;
			try
			{
				lock (Transaction ?? sync)
				{
					PrepareCommand(command);
					using (var dr = command.ExecuteReader())
					{
						while (dr.Read())
						{
							hasRead = true;
							action(dr);
						}
					}
				}
			}
			catch (OracleException ex)
			{
				BrokenTransaction = !tryRecover;
				TraceSource.TraceEvent(TraceEventType.Verbose, 5116, command.CommandText);
				TraceSource.TraceEvent(TraceEventType.Error, 5116, "{0}", ex);
				if (tryRecover && ex.InnerException is IOException && !hasRead)
				{
					ResetConnection();
					ExecuteDataReader(command, action, false);
				}
				else throw FormatException(ex, command);
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5117, "{0}", ex);
				if (tryRecover && !hasRead)
				{
					ResetConnection();
					ExecuteDataReader(command, action, false);
				}
				else throw;
			}
			finally
			{
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
				TraceSource.TraceEvent(TraceEventType.Verbose, 5117, command.CommandText);
				TraceSource.TraceEvent(TraceEventType.Error, 5117, "{0}", ex);
				if (tryRecover && ex.InnerException is IOException)
				{
					ResetConnection();
					return FillDataSet(command, ds, false);
				}
				else throw FormatException(ex, command);
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5118, "{0}", ex);
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
				TraceSource.TraceEvent(TraceEventType.Error, 5119, "{0}", ex);
			}
		}
	}
}
