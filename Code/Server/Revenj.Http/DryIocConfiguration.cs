using System;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Xml.Linq;
using DryIoc;
using Revenj.Api;
using Revenj.DatabasePersistence;
using Revenj.DatabasePersistence.Postgres;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Wcf;

namespace Revenj.Http
{
	//TODO: move to wcf
	[Obsolete("Not working yet")]
	public static class DryIocConfiguration
	{
		internal class ServerState : ISystemState
		{
			public ServerState()
			{
				IsBooting = true;
			}

			public bool IsBooting { get; internal set; }
			public bool IsReady { get; private set; }
			public event Action<IObjectFactory> Ready = f => { };
			public void Started(IObjectFactory factory)
			{
				IsBooting = false;
				IsReady = true;
				Ready(factory);
			}
		}

		public static void Load(IRegistry registry)
		{
			registry.RegisterInstance<ISystemState>(new ServerState());
			registry.RegisterInstance<IPermissionManager>(new NoAuth());

			registry.Register<IRestApplication, RestApplication>(Reuse.Singleton);
			registry.Register<RestApplication>(Reuse.Singleton);
			//builder.RegisterType<SoapApplication>().As<SoapApplication, ISoapApplication>();
			registry.Register<ICommandConverter, CommandConverter>(Reuse.Singleton);

			SetupExtensibility(registry);
			SetupPostgres(registry);
			SetupPatterns(registry);
			SetupSerialization(registry);

			//builder.RegisterType<RepositoryAuthentication>().As<IAuthentication>();
			//builder.RegisterType<RepositoryPrincipalFactory>().As<IPrincipalFactory>();
			//registry.Register<IPermissionManager, PermissionManager>(Reuse.Singleton);

			registry.Register<IProcessingEngine, ProcessingEngine>(Reuse.Singleton);
			registry.Register<IScopePool, ScopePool>(Reuse.Singleton);
		}

		private static void SetupExtensibility(IRegistry registry)
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
			registry.RegisterInstance(new PluginsConfiguration { Directories = dllPlugins });

			registry.Register<SystemInitialization>();
			registry.Register<IObjectFactory, DryIocObjectFactory>(Reuse.Singleton);
			registry.Register<IExtensibilityProvider, DryIocMefProvider>(Reuse.Singleton);
			registry.Register(typeof(IPluginRepository<>), typeof(PluginRepository<>), Reuse.Singleton);

			registry.RegisterInstance<IAspectRegistrator>(aopRepository);
			registry.RegisterInstance<IAspectComposer>(aopRepository);
			//registry.RegisterInstance<IInterceptorRegistrator>(aopRepository);
			registry.RegisterInstance<IMixinProvider>(dynamicProxy);
			registry.RegisterInstance<IDynamicProxyProvider>(dynamicProxy);
		}

		private static void SetupPatterns(IRegistry registry)
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

			registry.RegisterDelegate<IDomainModel>(c => new Revenj.DomainPatterns.DomainModel(serverModels, c.Resolve<IObjectFactory>()), Reuse.Singleton);
			registry.Register<ITypeResolver, DomainTypeResolver>(Reuse.Singleton);
			registry.Register<IServiceProvider, ServiceLocator>(Reuse.InCurrentScope);
			registry.Register(typeof(IDataCache<>), typeof(WeakCache<>), Reuse.InCurrentScope);
			registry.Register(typeof(WeakCache<>), typeof(WeakCache<>), Reuse.InCurrentScope);
			registry.Register<IDomainEventSource, DomainEventSource>(Reuse.InCurrentScope);
			registry.Register<IDomainEventStore, DomainEventStore>(Reuse.InCurrentScope);
			registry.Register<GlobalEventStore>(Reuse.Singleton);
			registry.Register(typeof(IDomainEventSource<>), typeof(SingleDomainEventSource<>), Reuse.InCurrentScope);
			registry.Register(typeof(IObservable<>), typeof(RegisterChangeNotifications<>), Reuse.Singleton);
			registry.Register<IDataContext, DataContext>(Reuse.InCurrentScope);
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

		private static void SetupPostgres(IRegistry registry)
		{
			var cs = ConfigurationManager.AppSettings["Revenj.ConnectionString"] ?? ConfigurationManager.AppSettings["ConnectionString"];
			if (string.IsNullOrEmpty(cs))
				throw new ConfigurationErrorsException(@"ConnectionString is missing from configuration. Add ConnectionString to <appSettings>
Example: <add key=""ConnectionString"" value=""server=postgres.localhost;port=5432;database=MyDatabase;user=postgres;password=123456;encoding=unicode"" />");

			registry.RegisterInstance(new Revenj.DatabasePersistence.Postgres.ConnectionInfo(cs));
			registry.Register<IConnectionPool, PostgresConnectionPool>(Reuse.Singleton);
			registry.Register<IDatabaseQueryManager, PostgresQueryManager>(Reuse.InCurrentScope);
			registry.Register<IPostgresDatabaseQuery, PostgresDatabaseQuery>();
			registry.RegisterDelegate(r => r.Resolve<IDatabaseQueryManager>().CreateQuery(), Reuse.InCurrentScope);
			registry.Register<IDataChangeNotification, PostgresDatabaseNotification>(Reuse.Singleton);
			registry.Register<IEagerNotification, PostgresDatabaseNotification>(Reuse.Singleton);

			registry.Register<IPostgresConverterRepository, PostgresObjectFactory>(Reuse.Singleton);
			registry.Register<IPostgresConverterFactory, PostgresObjectFactory>(Reuse.Singleton);

			registry.Register<Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryExecutor>();
		}

		private static void SetupSerialization(IRegistry registry)
		{
			registry.Register<GenericDataContractResolver>(Reuse.Singleton);
			registry.Register<ISerialization<XElement>, XmlSerialization>(Reuse.Singleton);
			registry.Register<GenericDeserializationBinder>(Reuse.Singleton);
			registry.Register<ISerialization<byte[]>, BinarySerialization>(Reuse.Singleton);
			registry.Register<ISerialization<string>, JsonSerialization>(Reuse.Singleton);
			registry.Register<ISerialization<TextReader>, JsonSerialization>(Reuse.Singleton);
			registry.Register<ISerialization<MemoryStream>, ProtobufSerialization>(Reuse.Singleton);
			registry.Register<ISerialization<Stream>, ProtobufSerialization>(Reuse.Singleton);
			registry.Register<ISerialization<object>, PassThroughSerialization>(Reuse.Singleton);
			registry.Register<IWireSerialization, WireSerialization>(Reuse.Singleton);
		}

		public static IDomainModel Start(IObjectFactory factory)
		{
			var init = factory.Resolve<SystemInitialization>();
			init.Initialize(false);
			//TODO change domain model boot. export to ISystemAspect to avoid explicit initialization
			return factory.Resolve<IDomainModel>();
		}
	}
}