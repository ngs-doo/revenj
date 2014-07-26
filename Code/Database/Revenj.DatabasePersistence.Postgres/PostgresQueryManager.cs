using System;
using System.Collections.Concurrent;
using System.Diagnostics.Contracts;
using Npgsql;
using Revenj.Common;
using Revenj.Logging;

namespace Revenj.DatabasePersistence.Postgres
{
	public class PostgresQueryManager : IDatabaseQueryManager, IDisposable
	{
		private static int CpuCount = Environment.ProcessorCount;
		private static int InitialCount = Environment.ProcessorCount < 17 ? 17 : Environment.ProcessorCount * 2 - 1;

		private readonly IConnectionPool Connections;
		private readonly ILogFactory LogFactory;
		private readonly ConcurrentDictionary<IDatabaseQuery, NpgsqlTransaction> OpenTransactions =
			new ConcurrentDictionary<IDatabaseQuery, NpgsqlTransaction>(CpuCount, InitialCount);
		private readonly ConcurrentDictionary<IDatabaseQuery, NpgsqlConnection> OpenConnections =
			new ConcurrentDictionary<IDatabaseQuery, NpgsqlConnection>(CpuCount, InitialCount);
		private readonly Func<NpgsqlConnection, NpgsqlTransaction, ILogFactory, IPostgresDatabaseQuery> QueryFactory;

		public PostgresQueryManager(
			IConnectionPool connections,
			ILogFactory logFactory,
			Func<NpgsqlConnection, NpgsqlTransaction, ILogFactory, IPostgresDatabaseQuery> queryFactory)
		{
			Contract.Requires(connections != null);
			Contract.Requires(logFactory != null);
			Contract.Requires(queryFactory != null);

			this.Connections = connections;
			this.LogFactory = logFactory;
			this.QueryFactory = queryFactory;
		}

		public IDatabaseQuery StartQuery(bool withTransaction)
		{
			var connection = Connections.Take(withTransaction);
			NpgsqlTransaction transaction = null;
			if (withTransaction)
				transaction = connection.BeginTransaction();
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
			bool released = false;
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
					Connections.Release(conn, success);
					released = true;
				}
				else failure = success;
			}
			NpgsqlConnection connection;
			if (OpenConnections.TryRemove(query, out connection) && !released)
				Connections.Release(connection, success);
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
						}
						catch (Exception tex)
						{
							LogFactory.Create("Postgres database layer - hanging transaction").Error(tex.ToString());
						}
						Connections.Release(conn, false);
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
					Connections.Release(c, false);
				OpenConnections.Clear();
			}
			catch (Exception ex)
			{
				LogFactory.Create("Postgres database layer - dispose").Error(ex.ToString());
			}
		}
	}
}
