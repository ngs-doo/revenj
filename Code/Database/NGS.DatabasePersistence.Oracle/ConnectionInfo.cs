using System.Diagnostics.Contracts;

namespace NGS.DatabasePersistence.Oracle
{
	public class ConnectionInfo
	{
		public ConnectionInfo(string connectionString)
		{
			Contract.Requires(connectionString != null);

			this.ConnectionString = connectionString;
		}

		public string ConnectionString { get; private set; }
	}
}
