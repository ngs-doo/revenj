using System;
using System.Collections.Concurrent;
using System.Data;
using System.Diagnostics.Contracts;
using Oracle.DataAccess.Client;
using Revenj.Common;
using Revenj.Logging;

namespace Revenj.DatabasePersistence.Oracle
{
	public class OracleQueryManager : IDatabaseQueryManager
	{
		private readonly string ConnectionString;

		private readonly ILogFactory LogFactory;

		private readonly ConcurrentDictionary<IDatabaseQuery, OracleTransaction> OpenTransactions = new ConcurrentDictionary<IDatabaseQuery, OracleTransaction>();
		private readonly ConcurrentDictionary<IDatabaseQuery, OracleConnection> OpenConnections = new ConcurrentDictionary<IDatabaseQuery, OracleConnection>();
		private readonly Func<OracleConnection, OracleTransaction, ILogFactory, IOracleDatabaseQuery> QueryFactory;

		public OracleQueryManager(
			ConnectionInfo connectionInfo,
			ILogFactory logFactory,
			Func<OracleConnection, OracleTransaction, ILogFactory, IOracleDatabaseQuery> queryFactory)
		{
			Contract.Requires(connectionInfo != null);
			Contract.Requires(connectionInfo.ConnectionString != null);
			Contract.Requires(logFactory != null);
			Contract.Requires(queryFactory != null);

			this.ConnectionString = connectionInfo.ConnectionString;
			this.LogFactory = logFactory;
			this.QueryFactory = queryFactory;
		}

		public IDatabaseQuery StartQuery(bool withTransaction)
		{
			var connection = new OracleConnection(ConnectionString);
			OracleTransaction transaction = null;
			if (withTransaction)
			{
				try
				{
					connection.Open();
				}
				catch (Exception ex)
				{
					LogFactory.Create("Oracle database layer - start query").Error(ex.ToString());
					try { connection.Close(); }
					catch { }
					OracleConnection.ClearAllPools();
					connection = new OracleConnection(ConnectionString);
					connection.Open();
				}
				transaction = connection.BeginTransaction();
			}
			IDatabaseQuery query;
			try
			{
				query = QueryFactory(connection, transaction, LogFactory);
			}
			catch (Exception ex)
			{
				var log = LogFactory.Create("Oracle database layer - start query");
				log.Error(ex.ToString());
				log.Info("Transactions: {0}, connections: {1}".With(OpenTransactions.Count, OpenConnections.Count));
				throw;
			}
			if (withTransaction)
				OpenTransactions.TryAdd(query, transaction);
			OpenConnections.TryAdd(query, connection);
			return query;
		}

		public void EndQuery(IDatabaseQuery query, bool success)
		{
			if (query == null)
				return;
			bool failure = false;
			if (query.InTransaction)
			{
				OracleTransaction transaction;
				if (!OpenTransactions.TryRemove(query, out transaction))
					throw new FrameworkException("Can't find query transaction");

				var conn = transaction.Connection;
				if (conn != null)
				{
					if (success)
						transaction.Commit();
					else
						transaction.Rollback();
					conn.Close();
				}
				else failure = success;
			}
			OracleConnection connection;
			OpenConnections.TryRemove(query, out connection);
			if (connection != null && connection.State == ConnectionState.Open)
				connection.Close();
			if (failure)
				throw new FrameworkException("Transaction can't be committed since connection is null");
		}
	}
}
