using System;
using Revenj.Core;

namespace DSL
{
	public static class Core
	{
		public static IServiceProvider SetupPostgres(string connectionString, bool withAspects = false, bool externalConfiguration = false)
		{
			return Setup(Database.Postgres, connectionString, withAspects, externalConfiguration);
		}

		/*public static IServiceProvider SetupOracle(string connectionString)
		{
			return Setup(Database.Oracle, connectionString);
		}*/

		private static IServiceProvider Setup(Database db, string connectionString, bool withAspects, bool externalConfiguration)
		{
			if (string.IsNullOrEmpty(connectionString))
				throw new ArgumentNullException("connectionString", "Connection string not provided");
			return AutofacConfiguration.Configure(db, connectionString, withAspects, externalConfiguration);
		}
	}
}
