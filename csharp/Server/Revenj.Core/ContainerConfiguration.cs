using System;
using System.Configuration;
using System.IO;
using System.Linq;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Security;
using Revenj.Serialization;

namespace Revenj.Core
{
	internal static class ContainerConfiguration
	{
		public static IServiceProvider Configure(DSL.Core.Container container, Database database, string connectionString, bool withAspects, bool externalConfiguration)
		{
			var dllPlugins = externalConfiguration == false ? new string[0] :
				(from key in ConfigurationManager.AppSettings.AllKeys
				 where key.StartsWith("PluginsPath", StringComparison.OrdinalIgnoreCase)
				 let path = ConfigurationManager.AppSettings[key]
				 let pathRelative = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path)
				 let chosenPath = Directory.Exists(pathRelative) ? pathRelative : path
				 select chosenPath)
				.ToArray();
			var assemblies =
				from asm in Revenj.Utility.AssemblyScanner.GetAssemblies()
				where asm.FullName.StartsWith("Revenj.")
				select asm;
			var state = new SystemState();
			var builder = container == DSL.Core.Container.Autofac
				? Revenj.Extensibility.Setup.UseAutofac(assemblies, dllPlugins, externalConfiguration, false, withAspects)
				: Revenj.Extensibility.Setup.UseDryIoc(assemblies, dllPlugins, false);
			builder.RegisterSingleton<ISystemState>(state);
			if (database == Core.Database.Postgres)
				SetupPostgres(builder, connectionString);
			else
				SetupOracle(builder, connectionString);
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

		private static void SetupPostgres(IObjectFactoryBuilder builder, string connectionString)
		{
			Revenj.DatabasePersistence.Postgres.Setup.ConfigurePostgres(builder, connectionString);
		}

		private static void SetupOracle(IObjectFactoryBuilder builder, string connectionString)
		{
			Revenj.DatabasePersistence.Oracle.Setup.ConfigureOracle(builder, connectionString);
		}
	}
}