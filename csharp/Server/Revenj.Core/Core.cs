using System;
using Revenj.Core;

namespace DSL
{
	public static class Core
	{
		internal enum Container
		{
			Autofac,
			DryIoc
		}

		public static IServiceProvider SetupPostgres(
			string connectionString,
			bool withAspects = false,
			bool externalConfiguration = false)
		{
			return Setup(Container.Autofac, Database.Postgres, connectionString, withAspects, externalConfiguration);
		}

		public static IServiceProvider SetupOracle(
			string connectionString,
			bool withAspects = false,
			bool externalConfiguration = false)
		{
			return Setup(Container.Autofac, Database.Oracle, connectionString, withAspects, externalConfiguration);
		}

		private static IServiceProvider Setup(Container container, Database db, string connectionString, bool withAspects, bool externalConfiguration)
		{
			if (string.IsNullOrEmpty(connectionString))
				throw new ArgumentNullException("connectionString", "Connection string not provided");
			return ContainerConfiguration.Configure(container, db, connectionString, withAspects, externalConfiguration);
		}
	}
}
