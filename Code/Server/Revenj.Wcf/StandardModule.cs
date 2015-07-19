using System;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Security;
using System.ServiceModel;
using System.Xml.Linq;
using Revenj.Api;
using Revenj.DatabasePersistence;
using Revenj.DatabasePersistence.Postgres;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Extensibility.Autofac;
using Revenj.Extensibility.Autofac.Builder;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Processing;
using Revenj.Security;
using Revenj.Serialization;

namespace Revenj.Wcf
{
	public class StandardModuleNoDatabase : StandardModule
	{
		public StandardModuleNoDatabase() : base(false) { }
	}

	public class StandardModule : Revenj.Extensibility.Autofac.Module
	{
		private readonly bool WithDatabase;

		public StandardModule() : this(true) { }
		public StandardModule(bool withDatabase)
		{
			this.WithDatabase = withDatabase;
		}

		protected override void Load(Revenj.Extensibility.Autofac.ContainerBuilder builder)
		{
			//TODO: register applications as implementation only
			builder.RegisterType<RestApplication>().As<RestApplication, IRestApplication>();
			builder.RegisterType<SoapApplication>().As<SoapApplication, ISoapApplication>();
			builder.RegisterType<CommandConverter>().As<ICommandConverter>();

			SetupExtensibility(builder);
			if (WithDatabase)
				SetupPostgres(builder);
			SetupPatterns(builder);
			SetupSerialization(builder);

			builder.RegisterType<RepositoryAuthentication>().As<IAuthentication<SecureString>, IAuthentication<string>, IAuthentication<byte[]>>();
			builder.RegisterType<RepositoryPrincipalFactory>().As<IPrincipalFactory>();
			builder.RegisterType<PermissionManager>().As<IPermissionManager>().SingleInstance();

			builder.RegisterType(typeof(ProcessingEngine)).As(typeof(IProcessingEngine)).SingleInstance();
			builder.RegisterType(typeof(ScopePool)).As(typeof(IScopePool)).SingleInstance();

			builder.RegisterType<OnContainerBuild>().As<IStartable>();

			base.Load(builder);
		}

		class AspectsModule : Revenj.Extensibility.Autofac.Module
		{
			private readonly AspectRepository Repository;

			public AspectsModule(AspectRepository repository)
			{
				this.Repository = repository;
				//FIX for Castle interception issue
				Castle.DynamicProxy.Generators.AttributesToAvoidReplicating.Add(typeof(ServiceContractAttribute));
			}

			protected override void AttachToComponentRegistration(
				IComponentRegistry componentRegistry,
				IComponentRegistration registration)
			{
				registration.Preparing += (_, pea) => Repository.Preparing(pea);
				registration.Activating += (_, aea) => Repository.Activating(aea);

				base.AttachToComponentRegistration(componentRegistry, registration);
			}
		}

		private static void SetupExtensibility(Revenj.Extensibility.Autofac.ContainerBuilder builder)
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

			if (ConfigurationManager.AppSettings["Revenj.AllowAspects"] == "true")
				builder.RegisterModule(new AspectsModule(aopRepository));
		}

		private static void SetupPatterns(Revenj.Extensibility.Autofac.ContainerBuilder builder)
		{
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
Alternatively, explicitly specify sever assembly in the config file.
Example: <add key=""ServerAssembly_Domain"" value=""AppDomainModel.dll"" />");
			}

			builder.RegisterGeneratedFactory<Revenj.DomainPatterns.DomainModel.Factory>();
			builder.RegisterType<Revenj.DomainPatterns.DomainModel>();
			builder.Register(c => c.Resolve<Revenj.DomainPatterns.DomainModel.Factory>()(serverModels)).As<IDomainModel>().SingleInstance();
			builder.RegisterType<DomainTypeResolver>().As<ITypeResolver>().SingleInstance();
			builder.RegisterType<ServiceLocator>().As<IServiceProvider>().InstancePerLifetimeScope();
			builder.RegisterGeneric(typeof(WeakCache<>)).As(typeof(WeakCache<>), typeof(IDataCache<>)).InstancePerLifetimeScope();
			builder.RegisterType<DomainEventSource>().As<IDomainEventSource>().InstancePerLifetimeScope();
			builder.RegisterType<DomainEventStore>().As<IDomainEventStore>().InstancePerLifetimeScope();
			builder.RegisterType<GlobalEventStore>().SingleInstance();
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

		private static void SetupPostgres(Revenj.Extensibility.Autofac.ContainerBuilder builder)
		{
			var cs = ConfigurationManager.AppSettings["Revenj.ConnectionString"] ?? ConfigurationManager.AppSettings["ConnectionString"];
			if (string.IsNullOrEmpty(cs))
				throw new ConfigurationErrorsException(@"ConnectionString is missing from configuration. Add ConnectionString to <appSettings>
Example: <add key=""ConnectionString"" value=""server=postgres.localhost;port=5432;database=MyDatabase;user=postgres;password=123456;encoding=unicode"" />");

			builder.RegisterInstance(new Revenj.DatabasePersistence.Postgres.ConnectionInfo(cs));
			builder.RegisterType<PostgresConnectionPool>().As<IConnectionPool>().SingleInstance();
			builder.RegisterType<PostgresQueryManager>().As<IDatabaseQueryManager>().InstancePerLifetimeScope();
			builder.RegisterType<PostgresDatabaseQuery>().As<IPostgresDatabaseQuery>();
			builder.Register(c => c.Resolve<IDatabaseQueryManager>().CreateQuery()).As<IDatabaseQuery>().InstancePerLifetimeScope();
			builder.RegisterType<PostgresDatabaseNotification>().As<IDataChangeNotification, IEagerNotification>().SingleInstance();

			builder.RegisterType<PostgresObjectFactory>().As<IPostgresConverterRepository, IPostgresConverterFactory>().SingleInstance();

			builder.RegisterType<Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryExecutor>();
		}

		/*
		//TODO: Oracle has ugly dependencies. Use it only when necessary
		private static void SetupOracle(Revenj.Extensibility.Autofac.ContainerBuilder builder)
		{
			var cs = ConfigurationManager.AppSettings["Revenj.ConnectionString"] ?? ConfigurationManager.AppSettings["ConnectionString"];
			if (string.IsNullOrEmpty(cs))
				throw new ConfigurationErrorsException(@"ConnectionString is missing from configuration. Add ConnectionString to <appSettings>
Example: <add key=""ConnectionString"" value=""server=postgres.localhost;port=5432;database=MyDatabase;user=postgres;password=123456;encoding=unicode"" />");

			builder.RegisterInstance(new Revenj.DatabasePersistence.Oracle.ConnectionInfo(cs));
			builder.RegisterType<OracleQueryManager>().As<IDatabaseQueryManager>().InstancePerLifetimeScope();
			builder.RegisterType<OracleDatabaseQuery>().As<IOracleDatabaseQuery>();
			builder.Register(c => c.Resolve<IDatabaseQueryManager>().CreateQuery()).As<IDatabaseQuery>().InstancePerLifetimeScope();
			builder.RegisterType<OracleAdvancedQueueNotification>().As<IDataChangeNotification>().SingleInstance();

			builder.RegisterType<OracleObjectFactory>().As<IOracleConverterRepository, IOracleConverterFactory>().SingleInstance();

			builder.RegisterType<Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryExecutor>();
		}*/

		private static void SetupSerialization(Revenj.Extensibility.Autofac.ContainerBuilder builder)
		{
			builder.RegisterType<GenericDataContractResolver>().SingleInstance();
			builder.RegisterType<XmlSerialization>().As<ISerialization<XElement>>().SingleInstance();
			builder.RegisterType<GenericDeserializationBinder>().SingleInstance();
			builder.RegisterType<BinarySerialization>().As<ISerialization<byte[]>>().SingleInstance();
			builder.RegisterType<JsonSerialization>().As<ISerialization<string>, ISerialization<TextReader>>().SingleInstance();
			builder.RegisterType<ProtobufSerialization>().As<ISerialization<MemoryStream>, ISerialization<Stream>>().SingleInstance();
			builder.RegisterType<PassThroughSerialization>().As<ISerialization<object>>().SingleInstance();
			builder.RegisterType<WireSerialization>().As<IWireSerialization>().SingleInstance();
		}

		class OnContainerBuild : Revenj.Extensibility.Autofac.IStartable
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