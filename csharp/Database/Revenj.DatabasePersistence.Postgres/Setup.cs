using System.Configuration;
using Revenj.DatabasePersistence.Postgres.QueryGeneration;
using Revenj.DomainPatterns;
using Revenj.Extensibility;

namespace Revenj.DatabasePersistence.Postgres
{
	public static class Setup
	{
		public static int MinBatchSize { get; private set; }
		public static long MaxObjectSize { get; private set; }

		static Setup()
		{
			MinBatchSize = 1000;
			MaxObjectSize = 1024 * 1024;
			var mbs = ConfigurationManager.AppSettings["Database.MinBatchSize"];
			int n;
			if (!string.IsNullOrEmpty(mbs) && int.TryParse(mbs, out n))
				MinBatchSize = n;
			mbs = ConfigurationManager.AppSettings["Database.MaxObjectSize"];
			long m;
			if (!string.IsNullOrEmpty(mbs) && long.TryParse(mbs, out m))
				MaxObjectSize = m;
		}

		public static void ConfigurePostgres(this IObjectFactoryBuilder builder, string connectionString)
		{
			var ci = new Revenj.DatabasePersistence.Postgres.ConnectionInfo(connectionString);
			builder.RegisterSingleton(ci);
			builder.RegisterType<PostgresConnectionPool, IConnectionPool>(InstanceScope.Singleton);
			builder.RegisterType<PostgresQueryManager, IDatabaseQueryManager>(InstanceScope.Context);
			builder.RegisterType<PostgresDatabaseQuery, IPostgresDatabaseQuery>();
			builder.RegisterFunc(c => c.Resolve<IDatabaseQueryManager>().CreateQuery(), InstanceScope.Context);
			builder.RegisterType<PostgresDatabaseNotification, IDataChangeNotification, IEagerNotification>(InstanceScope.Singleton);
			builder.RegisterType<PostgresObjectFactory, IPostgresConverterRepository, IPostgresConverterFactory>(InstanceScope.Singleton);
			builder.RegisterType<QueryExecutor>();
			builder.RegisterSingleton<IPostgresVersionInfo>(new PostgresVersionInfo(ci));
		}
	}
}
