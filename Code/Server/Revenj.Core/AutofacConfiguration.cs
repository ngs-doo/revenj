using System;
using System.Configuration;
using System.IO;
using System.Linq;
using Revenj.DatabasePersistence.Postgres;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Security;
using Revenj.Serialization;

namespace Revenj.Core
{
	internal static class AutofacConfiguration
	{
		public static IServiceProvider Configure(Database database, string connectionString, bool withAspects, bool externalConfiguration)
		{
			var state = new SystemState();
			var builder = Revenj.Extensibility.Setup.UseAutofac(externalConfiguration, false, withAspects);
			builder.RegisterSingleton<ISystemState>(state);

			var dllPlugins = externalConfiguration == false ? new string[0] :
				(from key in ConfigurationManager.AppSettings.AllKeys
				 where key.StartsWith("PluginsPath", StringComparison.OrdinalIgnoreCase)
				 let path = ConfigurationManager.AppSettings[key]
				 let pathRelative = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path)
				 let chosenPath = Directory.Exists(pathRelative) ? pathRelative : path
				 select chosenPath)
				.ToArray();
			var assemblies = Revenj.Utility.AssemblyScanner.GetAssemblies().Where(it => it.FullName.StartsWith("Revenj."));
			builder.ConfigureExtensibility(assemblies, dllPlugins, false);
			if (database == Core.Database.Postgres)
				builder.ConfigurePostgres(connectionString);
			//else
			//SetupOracle(builder, connectionString);
			var serverModels =
				(from asm in Revenj.Utility.AssemblyScanner.GetAssemblies()
				 let type = asm.GetType("SystemBoot.Configuration")
				 where type != null && type.GetMethod("Initialize") != null
				 select asm)
				.ToList();
			builder.ConfigurePatterns(_ => serverModels);
			builder.ConfigureSerialization();
			builder.ConfigureSecurity(false);

			var factory = builder.Build();
			factory.Resolve<IDomainModel>();//TODO: explicit model initialization
			state.IsBooting = false;
			state.Started(factory);
			return factory;
		}

		/*
		private static void SetupOracle(Revenj.Extensibility.Autofac.ContainerBuilder builder, string cs)
		{
			builder.RegisterInstance(new Revenj.DatabasePersistence.Oracle.ConnectionInfo(cs));
			builder.RegisterType<OracleQueryManager>().As<IDatabaseQueryManager>().InstancePerLifetimeScope();
			builder.RegisterType<OracleDatabaseQuery>().As<IOracleDatabaseQuery>();
			builder.Register(c => c.Resolve<IDatabaseQueryManager>().CreateQuery()).As<IDatabaseQuery>().InstancePerLifetimeScope();
			builder.RegisterType<OracleAdvancedQueueNotification>().As<IDataChangeNotification>().SingleInstance();

			builder.RegisterType<OracleObjectFactory>().As<IOracleConverterRepository, IOracleConverterFactory>().SingleInstance();

			builder.RegisterType<Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryExecutor>();
		}
		*/
	}
}