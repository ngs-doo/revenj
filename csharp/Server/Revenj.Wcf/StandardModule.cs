using System;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Reflection;
using Revenj.Api;
using Revenj.DatabasePersistence.Postgres;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Security;
using Revenj.Serialization;

namespace Revenj.Wcf
{
	public class StandardModuleNoDatabase : StandardModule
	{
		public StandardModuleNoDatabase() : base(Database.None) { }
	}

	public class StandardModuleOracle : StandardModule
	{
		public StandardModuleOracle() : base(Database.Oracle) { }
	}

	public class StandardModule : Revenj.Extensibility.Autofac.Module
	{
		public enum Database
		{
			Postgres,
			Oracle,
			None
		}

		private readonly Database DB;

		public StandardModule() : this(Database.Postgres) { }
		protected StandardModule(Database database)
		{
			this.DB = database;
		}

		public static void Configure(IObjectFactoryBuilder builder, Database db)
		{
			//TODO: register applications as implementation only
			builder.RegisterType<RestApplication>();
			builder.RegisterType<RestApplication, IRestApplication>();
			builder.RegisterType<SoapApplication>();
			builder.RegisterType<SoapApplication, ISoapApplication>();
			builder.RegisterType<CommandConverter, ICommandConverter>();

			var cs = ConfigurationManager.AppSettings["Revenj.ConnectionString"] ?? ConfigurationManager.AppSettings["ConnectionString"];
			if (db == Database.Postgres)
			{
				if (string.IsNullOrEmpty(cs))
					throw new ConfigurationErrorsException(@"ConnectionString is missing from configuration. Add ConnectionString to <appSettings>
Example: <add key=""ConnectionString"" value=""server=postgres.localhost;port=5432;database=MyDatabase;user=postgres;password=123456;encoding=unicode"" />");

				builder.ConfigurePostgres(cs);
			}
			else if (db == Database.Oracle)
			{
				if (string.IsNullOrEmpty(cs))
					throw new ConfigurationErrorsException(@"ConnectionString is missing from configuration. Add ConnectionString to <appSettings>
Example: <add key=""ConnectionString"" value=""Data Source=(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=MyOracleHost)(PORT=1521)))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=xe)));User Id=oracle;Password=123456;"" />");

				builder.ConfigurePostgres(cs);
			}
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
					throw new ConfigurationErrorsException(@"Server assemblies not found. When running in compiled mode, server assemblies must be deployed with other assemblies.
Alternatively, explicitly specify sever assembly in the configuration file.
Example: <add key=""ServerAssembly_Domain"" value=""AppDomainModel.dll"" />");
			}

			builder.ConfigurePatterns(_ => serverModels);
			builder.ConfigureSerialization();
			builder.ConfigureSecurity(true);
			builder.ConfigureProcessing();
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