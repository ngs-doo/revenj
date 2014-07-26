using System;
using System.Data.Common;

namespace NGS.DatabasePersistence.Postgres
{
	[Serializable]
	public class PostgresException : DbException
	{
		public PostgresException(string message)
			: base(message) { }
		protected PostgresException(
		  System.Runtime.Serialization.SerializationInfo info,
		  System.Runtime.Serialization.StreamingContext context)
			: base(info, context) { }
	}
}
