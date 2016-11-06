using System;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Reflection;
using DSL;
using Revenj.Api;
using Revenj.Extensibility;
using Revenj.Processing;

namespace Revenj.Wcf
{
	public class StandardModuleNoDatabase : StandardModule
	{
		public StandardModuleNoDatabase() : base(_ => { }) { }
	}

	public class StandardModule : Revenj.Extensibility.Autofac.Module
	{
		private readonly Action<IObjectFactoryBuilder> DB;

		public StandardModule() : this(SetupPostgres) { }
		protected StandardModule(Action<IObjectFactoryBuilder> db)
		{
			this.DB = db;
		}

		public static void Configure(IObjectFactoryBuilder builder, Action<IObjectFactoryBuilder> db)
		{
			//TODO: register applications as implementation only
			builder.RegisterType<RestApplication>();
			builder.RegisterType<RestApplication, IRestApplication>();
			builder.RegisterType<SoapApplication>();
			builder.RegisterType<SoapApplication, ISoapApplication>();
			builder.RegisterType<CommandConverter, ICommandConverter>();

			if (db != null)
				db(builder);
			var serverModels =
				(from key in ConfigurationManager.AppSettings.AllKeys
				 where key.StartsWith("ServerAssembly", StringComparison.OrdinalIgnoreCase)
				 select LoadAssembly(ConfigurationManager.AppSettings[key]))
				.ToList();

			if (serverModels.Count == 0)
			{
				serverModels =
					(from asm in Revenj.Utility.AssemblyScanner.GetAssemblies()
					 let type = asm.GetType("SystemBoot.Configuration")
					 where type != null && type.GetMethod("Initialize") != null
					 select asm)
					.ToList();
				if (serverModels.Count == 0)
				{
					if (ConfigurationManager.AppSettings["ServerModel"] != "None")
					{
						throw new ConfigurationErrorsException(@"Server assemblies not found. When running in compiled mode, server assemblies must be deployed with other assemblies.
Alternatively, explicitly specify sever assembly in the configuration file.
Example: <add key=""ServerAssembly_Domain"" value=""AppDomainModel.dll"" />
If you wish to run Revenj without server assemblies specify ServerModel = None as:
<add key=""ServerModel"" value=""None"" />");
					}
				}
			}

			builder.ConfigurePatterns(_ => serverModels);
			builder.ConfigureSerialization();
			builder.ConfigureSecurity(true);
			builder.ConfigureProcessing();
		}

		internal static void SetupPostgres(IObjectFactoryBuilder builder)
		{
			var cs = ConfigurationManager.AppSettings["Revenj.ConnectionString"] ?? ConfigurationManager.AppSettings["ConnectionString"];
			if (string.IsNullOrEmpty(cs))
				throw new ConfigurationErrorsException(@"ConnectionString is missing from configuration. Add ConnectionString to <appSettings>
Example: <add key=""ConnectionString"" value=""server=postgres.localhost;port=5432;database=MyDatabase;user=postgres;password=123456;encoding=unicode"" />");

			Revenj.DatabasePersistence.Postgres.Setup.ConfigurePostgres(builder, cs);
		}

		protected override void Load(Revenj.Extensibility.Autofac.ContainerBuilder builder)
		{
			Configure(builder, DB);

			base.Load(builder);
		}

		private static Assembly LoadAssembly(string name)
		{
			var file1 = Path.Combine(AppDomain.CurrentDomain.BaseDirectory ?? string.Empty, name);
			var file2 = Path.Combine(AppDomain.CurrentDomain.RelativeSearchPath ?? string.Empty, name);
			var file = File.Exists(file1) ? file1 : file2;
			if (File.Exists(file))
				return Assembly.LoadFrom(file);
			throw new ConfigurationErrorsException("Can't find assembly " + name + ". Check your configuration");
		}
	}
}