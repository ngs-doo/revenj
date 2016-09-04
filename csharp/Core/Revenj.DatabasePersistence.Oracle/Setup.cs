using System;
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
			if (string.IsNullOrEmpty(connectionString))
				throw new ArgumentNullException("connectionString", "Connection string not provided");
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
namespace Revenj
{
	public static class Oracle
	{
		public static IServiceProvider Setup(
			string connectionString,
			bool withAspects = false,
			bool externalConfiguration = false)
		{
			return DSL.Core.Setup(withAspects, externalConfiguration, b => Revenj.DatabasePersistence.Oracle.Setup.ConfigureOracle(b, connectionString));
		}
	}
	public class OracleModule : Revenj.Extensibility.Autofac.Module
	{
		protected override void Load(Extensibility.Autofac.ContainerBuilder builder)
		{
			var cs = ConfigurationManager.AppSettings["Revenj.ConnectionString"] ?? ConfigurationManager.AppSettings["ConnectionString"];
			if (string.IsNullOrEmpty(cs))
				throw new ConfigurationErrorsException(@"ConnectionString is missing from configuration. Add ConnectionString to <appSettings>
Example: <add key=""ConnectionString"" value=""Data Source=(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=MyOracleHost)(PORT=1521)))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=xe)));User Id=oracle;Password=123456;"" />");

			Revenj.DatabasePersistence.Oracle.Setup.ConfigureOracle(builder, cs);
			base.Load(builder);
		}
	}
}