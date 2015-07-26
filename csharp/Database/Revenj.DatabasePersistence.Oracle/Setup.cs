using System.Configuration;
using Revenj.DatabasePersistence.Oracle.QueryGeneration;
using Revenj.DomainPatterns;
using Revenj.Extensibility;

namespace Revenj.DatabasePersistence.Oracle
{
	public static class Setup
	{
		public static int MinBatchSize { get; private set; }

		static Setup()
		{
			MinBatchSize = 1000;
			var mbs = ConfigurationManager.AppSettings["Database.MinBatchSize"];
			int n;
			if (!string.IsNullOrEmpty(mbs) && int.TryParse(mbs, out n))
				MinBatchSize = n;
		}

		public static void ConfigureOracle(this IObjectFactoryBuilder builder, string connectionString)
		{
			builder.RegisterSingleton(new Revenj.DatabasePersistence.Oracle.ConnectionInfo(connectionString));
			builder.RegisterType<OracleQueryManager, IDatabaseQueryManager>(InstanceScope.Context);
			builder.RegisterType<OracleDatabaseQuery, IOracleDatabaseQuery>();
			builder.RegisterFunc(c => c.Resolve<IDatabaseQueryManager>().CreateQuery(), InstanceScope.Context);
			builder.RegisterType<OracleAdvancedQueueNotification, IDataChangeNotification, IEagerNotification>(InstanceScope.Singleton);
			builder.RegisterType<OracleObjectFactory, IOracleConverterRepository, IOracleConverterFactory>(InstanceScope.Singleton);
			builder.RegisterType<QueryExecutor>();
		}
	}
}
