using System;
using NGS.DomainPatterns;
using Revenj.Core;

namespace DSL
{
	public static class Core
	{
		public static IServiceLocator SetupPostgres(string connectionString, bool withAspects = false)
		{
			return Setup(Database.Postgres, connectionString, withAspects);
		}

		/*public static IServiceLocator SetupOracle(string connectionString)
		{
			return Setup(Database.Oracle, connectionString);
		}*/

		private static IServiceLocator Setup(Database db, string connectionString, bool withAspects)
		{
			if (string.IsNullOrEmpty(connectionString))
				throw new ArgumentNullException("connectionString", "Connection string not provided");
			return AutofacConfiguration.Configure(db, connectionString, withAspects);
		}
	}
}
