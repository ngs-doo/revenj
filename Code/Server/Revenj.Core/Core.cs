using System;
using NGS.DomainPatterns;
using Revenj.Core;

namespace DSL
{
	public static class Core
	{
		public static IServiceLocator SetupPostgres(string connectionString)
		{
			return Setup(Database.Postgres, connectionString);
		}

		public static IServiceLocator SetupOracle(string connectionString)
		{
			return Setup(Database.Oracle, connectionString);
		}

		private static IServiceLocator Setup(Database db, string connectionString)
		{
			if (string.IsNullOrEmpty(connectionString))
				throw new ArgumentNullException("connectionString", "Connection string not provided");
			return AutofacConfiguration.Configure(db, connectionString);
		}
	}
}
