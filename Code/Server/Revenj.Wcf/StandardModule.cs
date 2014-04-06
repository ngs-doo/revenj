using System;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Xml.Linq;
using Autofac;
using Autofac.Builder;
using NGS.DatabasePersistence;
using NGS.DatabasePersistence.Postgres;
using NGS.DomainPatterns;
using NGS.Extensibility;
using NGS.Logging;
using NGS.Logging.NLog;
using NGS.Security;
using NGS.Serialization;
using Revenj.Api;
using Revenj.Processing;

namespace Revenj.Wcf
{
	public class StandardModuleNoDatabase : StandardModule
	{
		public StandardModuleNoDatabase() : base(false) { }
	}

	public class StandardModule : Autofac.Module
	{
		private readonly bool WithDatabase;

		public StandardModule() : this(true) { }
		public StandardModule(bool withDatabase)
		{
			this.WithDatabase = withDatabase;
		}

		protected override void Load(Autofac.ContainerBuilder builder)
		{
			builder.RegisterType<RestApplication>().As<RestApplication, IRestApplication>();
			builder.RegisterType<SoapApplication>().As<SoapApplication, ISoapApplication>();
			builder.RegisterType<CommandConverter>().As<ICommandConverter>();

			SetupExtensibility(builder);
			if (WithDatabase)
				SetupPostgres(builder);
			SetupPatterns(builder);
			SetupSerialization(builder);

			builder.RegisterType<RepositoryAuthentication>().As<IAuthentication>();
			builder.RegisterType<RepositoryPrincipalFactory>().As<IPrincipalFactory>();
			builder.RegisterType<PermissionManager>().As<IPermissionManager>().SingleInstance();

			builder.RegisterType<LogFactory>().As<ILogFactory>().SingleInstance();
			builder.RegisterType<NLogLogger>().As<ILogger>();

			builder.RegisterType(typeof(ProcessingEngine)).As(typeof(IProcessingEngine)).SingleInstance();

			builder.RegisterType<OnContainerBuild>().As<IStartable>();

			base.Load(builder);
		}

		private static void SetupExtensibility(Autofac.ContainerBuilder builder)
		{
			var dynamicProxy = new CastleDynamicProxyProvider();
			var aopRepository = new AspectRepository(dynamicProxy);

			var dllPlugins =
				(from key in ConfigurationManager.AppSettings.AllKeys
				 where key.StartsWith("PluginsPath", StringComparison.OrdinalIgnoreCase)
				 let path = ConfigurationManager.AppSettings[key]
				 let pathRelative = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path)
				 let chosenPath = Directory.Exists(pathRelative) ? pathRelative : path
				 select chosenPath)
				.ToList();
			builder.RegisterInstance(new PluginsConfiguration { Directories = dllPlugins });

			builder.RegisterType<SystemInitialization>();
			builder.RegisterType<AutofacObjectFactory>().As<IObjectFactory>().SingleInstance();
			builder.RegisterType<AutofacMefProvider>().As<IExtensibilityProvider>().SingleInstance();
			builder.RegisterGeneric(typeof(PluginRepository<>)).As(typeof(IPluginRepository<>)).SingleInstance();

			builder.RegisterInstance(aopRepository).As<IAspectRegistrator, IAspectComposer, IInterceptorRegistrator>();
			builder.RegisterInstance(dynamicProxy).As<IMixinProvider, IDynamicProxyProvider>();
		}

		private static void SetupPatterns(Autofac.ContainerBuilder builder)
		{
			var serverModels =
				(from key in ConfigurationManager.AppSettings.AllKeys
				 where key.StartsWith("ServerAssembly", StringComparison.OrdinalIgnoreCase)
				 select LoadAssembly(ConfigurationManager.AppSettings[key]))
				.ToList();

			if (serverModels.Count == 0)
			{
				serverModels =
					(from asm in NGS.Utility.AssemblyScanner.GetAssemblies()
					 let type = asm.GetType("SystemBoot.Configuration")
					 where type != null && type.GetMethod("Initialize") != null
					 select asm)
					.ToList();
				if (serverModels.Count == 0)
					throw new ConfigurationErrorsException(@"Server assemblies not found. When running in compiled mode, server assemblies must be deployed with other assemblies.
Alternatively, explicitly specify sever assembly in the config file.
Example: <add key=""ServerAssembly_Domain"" value=""AppDomainModel.dll"" />");
			}

			builder.RegisterGeneratedFactory<NGS.DomainPatterns.DomainModel.Factory>();
			builder.RegisterType<NGS.DomainPatterns.DomainModel>();
			builder.Register(c => c.Resolve<NGS.DomainPatterns.DomainModel.Factory>()(serverModels)).As<IDomainModel>().SingleInstance();
			builder.RegisterType<DomainTypeResolver>().As<ITypeResolver>().SingleInstance();
			builder.RegisterType<ServiceLocator>().As<IServiceLocator, IServiceProvider>().InstancePerLifetimeScope();
			builder.RegisterGeneric(typeof(WeakCache<>)).As(typeof(IDataCache<>)).InstancePerLifetimeScope();
			builder.RegisterType<DomainEventSource>().As<IDomainEventSource>().InstancePerLifetimeScope();
			builder.RegisterType<DomainEventStore>().As<IDomainEventStore>().InstancePerLifetimeScope();
			builder.RegisterGeneric(typeof(SingleDomainEventSource<>)).As(typeof(IDomainEventSource<>)).InstancePerLifetimeScope();
			builder.RegisterGeneric(typeof(RegisterChangeNotifications<>)).As(typeof(IObservable<>)).SingleInstance();
			builder.RegisterType<DataContext>().As<IDataContext>().InstancePerLifetimeScope();
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

		private static void SetupPostgres(Autofac.ContainerBuilder builder)
		{
			var cs = ConfigurationManager.AppSettings["ConnectionString"];
			if (string.IsNullOrEmpty(cs))
				throw new ConfigurationErrorsException(@"ConnectionString is missing from configuration. Add ConnectionString to <appSettings>
Example: <add key=""ConnectionString"" value=""server=postgres.localhost;port=5432;database=MyDatabase;user=postgres;password=123456;encoding=unicode"" />");

			builder.RegisterInstance(new NGS.DatabasePersistence.Postgres.ConnectionInfo(cs));
			builder.RegisterType<PostgresQueryManager>().As<IDatabaseQueryManager>().InstancePerLifetimeScope();
			builder.RegisterType<PostgresDatabaseQuery>().As<IPostgresDatabaseQuery>();
			builder.Register(c => c.Resolve<IDatabaseQueryManager>().CreateQuery()).As<IDatabaseQuery>().InstancePerLifetimeScope();
			builder.RegisterType<PostgresDatabaseNotification>().As<IDataChangeNotification, IEagerNotification>().SingleInstance();

			builder.RegisterType<PostgresObjectFactory>().As<IPostgresConverterRepository, IPostgresConverterFactory>().SingleInstance();

			builder.RegisterType<NGS.DatabasePersistence.Postgres.QueryGeneration.QueryExecutor>();
		}

		private static void SetupSerialization(Autofac.ContainerBuilder builder)
		{
			builder.RegisterType<GenericDataContractResolver>().SingleInstance();
			builder.RegisterType<XmlSerialization>().As<ISerialization<XElement>>().SingleInstance();
			builder.RegisterType<GenericDeserializationBinder>().SingleInstance();
			builder.RegisterType<BinarySerialization>().As<ISerialization<byte[]>>().SingleInstance();
			builder.RegisterType<JsonSerialization>().As<ISerialization<string>, ISerialization<StringBuilder>, ISerialization<StreamReader>>().SingleInstance();
			builder.RegisterType<ProtobufSerialization>().As<ISerialization<MemoryStream>, ISerialization<Stream>>().SingleInstance();
			builder.RegisterType<PassThroughSerialization>().As<ISerialization<object>>().SingleInstance();
			builder.RegisterType<WireSerialization>().As<IWireSerialization>().SingleInstance();
		}

		class OnContainerBuild : Autofac.IStartable
		{
			private readonly IObjectFactory Factory;

			public OnContainerBuild(IObjectFactory factory)
			{
				this.Factory = factory;
			}

			public void Start()
			{
				var init = Factory.Resolve<SystemInitialization>();
				init.Initialize(false);
				//TODO change domain model boot. export to ISystemAspect to avoid explicit initialization
				Factory.Resolve<IDomainModel>();
			}
		}
	}
}