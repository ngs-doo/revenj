using System;
using System.Configuration;
using Revenj.DatabasePersistence.Postgres.QueryGeneration;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Utility;

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
			if (string.IsNullOrEmpty(connectionString))
				throw new ArgumentNullException("connectionString", "Connection string not provided");
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
			builder.RegisterFunc(c => BulkReaderHelper.BulkRead(c, ChunkedMemoryStream.Create()), InstanceScope.Context);
		}
	}
}

namespace Revenj
{
	public class PostgresModule : Revenj.Extensibility.Autofac.Module
	{
		protected override void Load(Extensibility.Autofac.ContainerBuilder builder)
		{
			var cs = ConfigurationManager.AppSettings["Revenj.ConnectionString"] ?? ConfigurationManager.AppSettings["ConnectionString"];
			if (string.IsNullOrEmpty(cs))
				throw new ConfigurationErrorsException(@"ConnectionString is missing from configuration. Add ConnectionString to <appSettings>
Example: <add key=""ConnectionString"" value=""server=postgres.localhost;port=5432;database=MyDatabase;user=postgres;password=123456;encoding=unicode"" />");

			Revenj.DatabasePersistence.Postgres.Setup.ConfigurePostgres(builder, cs);
			base.Load(builder);
		}
	}
}
