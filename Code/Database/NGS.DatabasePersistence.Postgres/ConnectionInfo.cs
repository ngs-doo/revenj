using System.Diagnostics.Contracts;
using Npgsql;

namespace NGS.DatabasePersistence.Postgres
{
	public class ConnectionInfo
	{
		private readonly NpgsqlConnection Connection;

		public ConnectionInfo(string connectionString)
		{
			Contract.Requires(connectionString != null);

			this.ConnectionString = connectionString;
			this.Connection = new NpgsqlConnection(connectionString);
		}

		public string ConnectionString { get; private set; }
		public NpgsqlConnection GetConnection() { return Connection.Clone(); }
	}
}
