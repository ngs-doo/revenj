using System;
using System.Diagnostics.Contracts;
using System.Runtime.Serialization;

namespace Revenj.DatabasePersistence.Postgres
{
	public interface IPostgresVersionInfo
	{
		int Major { get; }
		int Minor { get; }
	}

	[Serializable]
	internal class PostgresVersionInfo : IPostgresVersionInfo, ISerializable
	{
		public PostgresVersionInfo(ConnectionInfo connectionInfo)
		{
			Contract.Requires(connectionInfo != null);
			Contract.Requires(connectionInfo.ConnectionString != null);

			using (var connection = connectionInfo.GetConnection())
			{
				connection.Open();
				var version = connection.PostgreSqlVersion;
				Major = version.Major;
				Minor = version.Minor;
				connection.Close();
			}
		}

		private PostgresVersionInfo(SerializationInfo info, StreamingContext context) { }

		public int Major { get; private set; }
		public int Minor { get; private set; }

		void ISerializable.GetObjectData(SerializationInfo info, StreamingContext context) { }
	}
}
