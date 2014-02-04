using System;
using System.Collections.Concurrent;
using System.Data;
using System.Diagnostics.Contracts;
using NGS.Common;
using NGS.Logging;
using Npgsql;

namespace NGS.DatabasePersistence.Postgres
{
	public class PostgresQueryManager : IDatabaseQueryManager, IDisposable
	{
		private readonly ConnectionInfo ConnectionInfo;

		private readonly ILogFactory LogFactory;
		private static int CpuCount = Environment.ProcessorCount;
		private static int InitialCount = Environment.ProcessorCount < 17 ? 17 : Environment.ProcessorCount * 2 - 1;
		private readonly ConcurrentDictionary<IDatabaseQuery, NpgsqlTransaction> OpenTransactions =
			new ConcurrentDictionary<IDatabaseQuery, NpgsqlTransaction>(CpuCount, InitialCount);
		private readonly ConcurrentDictionary<IDatabaseQuery, NpgsqlConnection> OpenConnections =
			new ConcurrentDictionary<IDatabaseQuery, NpgsqlConnection>(CpuCount, InitialCount);
		private readonly Func<NpgsqlConnection, NpgsqlTransaction, ILogFactory, IPostgresDatabaseQuery> QueryFactory;

		public PostgresQueryManager(
			ConnectionInfo connectionInfo,
			ILogFactory logFactory,
			Func<NpgsqlConnection, NpgsqlTransaction, ILogFactory, IPostgresDatabaseQuery> queryFactory)
		{
			Contract.Requires(connectionInfo != null);
			Contract.Requires(connectionInfo.ConnectionString != null);
			Contract.Requires(logFactory != null);
			Contract.Requires(queryFactory != null);

			this.ConnectionInfo = connectionInfo;
			this.LogFactory = logFactory;
			this.QueryFactory = queryFactory;
		}

		public IDatabaseQuery StartQuery(bool withTransaction)
		{
			var connection = ConnectionInfo.GetConnection();
			NpgsqlTransaction transaction = null;
			if (withTransaction)
			{
				try
				{
					connection.Open();
				}
				catch (Exception ex)
				{
					LogFactory.Create("Postgres database layer - start query").Error(ex.ToString());
					try { connection.Close(); }
					catch { }
					NpgsqlConnection.ClearAllPools();
					connection = ConnectionInfo.GetConnection();
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
				var log = LogFactory.Create("Postgres database layer - start query");
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
				NpgsqlTransaction transaction;
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
			NpgsqlConnection connection;
			OpenConnections.TryRemove(query, out connection);
			try
			{
				if (connection != null && connection.State == ConnectionState.Open)
					connection.Close();
			}
			catch (Exception ex)
			{
				var log = LogFactory.Create("Postgres database layer - end query");
				log.Error(ex.ToString());
				log.Info("Transactions: {0}, connections: {1}".With(OpenTransactions.Count, OpenConnections.Count));
			}
			if (failure)
				throw new FrameworkException("Transaction can't be committed since connection is null");
		}

		public void Dispose()
		{
			try
			{
				foreach (var tran in OpenTransactions.Values)
				{
					var conn = tran.Connection;
					if (conn != null)
					{
						try
						{
							tran.Rollback();
							conn.Close();
						}
						catch (Exception tex)
						{
							LogFactory.Create("Postgres database layer - hanging transaction").Error(tex.ToString());
						}
					}
				}
				OpenTransactions.Clear();
			}
			catch (Exception ex)
			{
				LogFactory.Create("Postgres database layer - dispose").Error(ex.ToString());
			}
			try
			{
				foreach (var c in OpenConnections.Values)
					if (c.State == ConnectionState.Open)
					{
						try
						{
							c.Close();
						}
						catch (Exception cex)
						{
							LogFactory.Create("Postgres database layer - hanging connection").Error(cex.ToString());
						}
					}
				OpenConnections.Clear();
			}
			catch (Exception ex)
			{
				LogFactory.Create("Postgres database layer - dispose").Error(ex.ToString());
			}
		}
	}
}
