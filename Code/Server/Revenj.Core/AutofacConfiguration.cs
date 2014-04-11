using System;
using System.IO;
using System.Linq;
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

namespace Revenj.Core
{
	internal static class AutofacConfiguration
	{
		static AutofacConfiguration()
		{
			//TODO force plugin initialization
			new NGS.Plugins.DatabasePersistence.Postgres.ExpressionSupport.CommonMembers();
		}

		public static IServiceLocator Configure(Database database, string connectionString)
		{
			var state = new SystemState();
			var builder = new ContainerBuilder();
			builder.RegisterInstance(state).As<ISystemState>();
			SetupExtensibility(builder);
			if (database == Core.Database.Postgres)
				SetupPostgres(builder, connectionString);
			//else
			//SetupOracle(builder, connectionString);
			SetupPatterns(builder);
			SetupSerialization(builder);

			builder.RegisterType<PermissionManager>().As<IPermissionManager>().SingleInstance();

			builder.RegisterType<LogFactory>().As<ILogFactory>().SingleInstance();
			builder.RegisterType<NLogLogger>().As<ILogger>();

			var container = builder.Build();
			var init = container.Resolve<SystemInitialization>();
			init.Initialize(false);
			var factory = container.Resolve<IObjectFactory>();
			//TODO init model
			factory.Resolve<IDomainModel>();
			state.IsBooting = false;
			state.Started(factory);

			return factory.Resolve<IServiceLocator>();
		}

		private static void SetupExtensibility(Autofac.ContainerBuilder builder)
		{
			var dynamicProxy = new CastleDynamicProxyProvider();
			var aopRepository = new AspectRepository(dynamicProxy);

			var assemblies = NGS.Utility.AssemblyScanner.GetAssemblies().Where(it => it.FullName.StartsWith("NGS."));
			builder.RegisterInstance(new PluginsConfiguration { Assemblies = assemblies });

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
				(from asm in NGS.Utility.AssemblyScanner.GetAssemblies()
				 let type = asm.GetType("SystemBoot.Configuration")
				 where type != null && type.GetMethod("Initialize") != null
				 select asm)
				.ToList();

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

		private static void SetupPostgres(Autofac.ContainerBuilder builder, string cs)
		{
			builder.RegisterInstance(new NGS.DatabasePersistence.Postgres.ConnectionInfo(cs));
			builder.RegisterType<PostgresQueryManager>().As<IDatabaseQueryManager>().InstancePerLifetimeScope();
			builder.RegisterType<PostgresDatabaseQuery>().As<IPostgresDatabaseQuery>();
			builder.Register(c => c.Resolve<IDatabaseQueryManager>().CreateQuery()).As<IDatabaseQuery>().InstancePerLifetimeScope();
			builder.RegisterType<PostgresDatabaseNotification>().As<IDataChangeNotification, IEagerNotification>().SingleInstance();

			builder.RegisterType<PostgresObjectFactory>().As<IPostgresConverterRepository, IPostgresConverterFactory>().SingleInstance();

			builder.RegisterType<NGS.DatabasePersistence.Postgres.QueryGeneration.QueryExecutor>();
		}
		/*
		private static void SetupOracle(Autofac.ContainerBuilder builder, string cs)
		{
			builder.RegisterInstance(new NGS.DatabasePersistence.Oracle.ConnectionInfo(cs));
			builder.RegisterType<OracleQueryManager>().As<IDatabaseQueryManager>().InstancePerLifetimeScope();
			builder.RegisterType<OracleDatabaseQuery>().As<IOracleDatabaseQuery>();
			builder.Register(c => c.Resolve<IDatabaseQueryManager>().CreateQuery()).As<IDatabaseQuery>().InstancePerLifetimeScope();
			builder.RegisterType<OracleAdvancedQueueNotification>().As<IDataChangeNotification>().SingleInstance();

			builder.RegisterType<OracleObjectFactory>().As<IOracleConverterRepository, IOracleConverterFactory>().SingleInstance();

			builder.RegisterType<NGS.DatabasePersistence.Oracle.QueryGeneration.QueryExecutor>();
		}
		*/
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
	}
}