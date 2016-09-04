using System.Diagnostics.Contracts;
using Revenj.DatabasePersistence.Postgres.Npgsql;

namespace Revenj.DatabasePersistence.Postgres
{
	internal class ConnectionInfo
	{
		private readonly NpgsqlConnection Connection;
		internal static int LastCommandTimeout = 20;

		public ConnectionInfo(string connectionString)
		{
			Contract.Requires(connectionString != null);

			this.ConnectionString = connectionString;
			this.Connection = new NpgsqlConnection(connectionString);
			LastCommandTimeout = Connection.CommandTimeout;
		}

		internal string ConnectionString { get; private set; }
		internal NpgsqlConnection GetConnection() { return Connection.Clone(); }
	}
}
