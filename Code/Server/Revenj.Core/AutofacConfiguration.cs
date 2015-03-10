using System;
using System.IO;
using System.Linq;
using System.Xml.Linq;
using Revenj.DatabasePersistence;
using Revenj.DatabasePersistence.Postgres;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Extensibility.Autofac;
using Revenj.Extensibility.Autofac.Builder;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Core
{
	internal static class AutofacConfiguration
	{
		static AutofacConfiguration()
		{
			//TODO force plugin initialization
			new Revenj.Plugins.DatabasePersistence.Postgres.ExpressionSupport.CommonMembers();
		}

		public static IServiceLocator Configure(Database database, string connectionString, bool withAspects)
		{
			var state = new SystemState();
			var builder = new ContainerBuilder();
			builder.RegisterInstance(state).As<ISystemState>();
			SetupExtensibility(builder, withAspects);
			if (database == Core.Database.Postgres)
				SetupPostgres(builder, connectionString);
			//else
			//SetupOracle(builder, connectionString);
			SetupPatterns(builder);
			SetupSerialization(builder);

			builder.RegisterType<PermissionManager>().As<IPermissionManager>().SingleInstance();

			builder.RegisterType<OnContainerBuild>().As<IStartable>();

			var factory = builder.Build().Resolve<IObjectFactory>();
			state.IsBooting = false;
			state.Started(factory);
			return factory.Resolve<IServiceLocator>();
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

		class AspectsModule : Revenj.Extensibility.Autofac.Module
		{
			private readonly AspectRepository Repository;

			public AspectsModule(AspectRepository repository)
			{
				this.Repository = repository;
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

		private static void SetupExtensibility(Revenj.Extensibility.Autofac.ContainerBuilder builder, bool withAspects)
		{
			var dynamicProxy = new CastleDynamicProxyProvider();
			var aopRepository = new AspectRepository(dynamicProxy);

			var assemblies = Revenj.Utility.AssemblyScanner.GetAssemblies().Where(it => it.FullName.StartsWith("Revenj."));
			builder.RegisterInstance(new PluginsConfiguration { Assemblies = assemblies });

			builder.RegisterType<SystemInitialization>();
			builder.RegisterType<AutofacObjectFactory>().As<IObjectFactory>().SingleInstance();
			builder.RegisterType<AutofacMefProvider>().As<IExtensibilityProvider>().SingleInstance();
			builder.RegisterGeneric(typeof(PluginRepository<>)).As(typeof(IPluginRepository<>)).SingleInstance();

			builder.RegisterInstance(aopRepository).As<IAspectRegistrator, IAspectComposer, IInterceptorRegistrator>();
			builder.RegisterInstance(dynamicProxy).As<IMixinProvider, IDynamicProxyProvider>();

			if (withAspects)
			{
				foreach (var m in AssemblyScanner.GetAllTypes())
				{
					if (m.IsPublic && typeof(Revenj.Extensibility.Autofac.Module).IsAssignableFrom(m) && m.GetConstructor(new Type[0]) != null)
						builder.RegisterModule((Revenj.Extensibility.Autofac.Module)Activator.CreateInstance(m));
				}
				builder.RegisterModule(new AspectsModule(aopRepository));
			}
		}

		private static void SetupPatterns(Revenj.Extensibility.Autofac.ContainerBuilder builder)
		{
			var serverModels =
				(from asm in Revenj.Utility.AssemblyScanner.GetAssemblies()
				 let type = asm.GetType("SystemBoot.Configuration")
				 where type != null && type.GetMethod("Initialize") != null
				 select asm)
				.ToList();

			builder.RegisterGeneratedFactory<Revenj.DomainPatterns.DomainModel.Factory>();
			builder.RegisterType<Revenj.DomainPatterns.DomainModel>();
			builder.Register(c => c.Resolve<Revenj.DomainPatterns.DomainModel.Factory>()(serverModels)).As<IDomainModel>().SingleInstance();
			builder.RegisterType<DomainTypeResolver>().As<ITypeResolver>().SingleInstance();
			builder.RegisterType<ServiceLocator>().As<IServiceLocator, IServiceProvider>().InstancePerLifetimeScope();
			builder.RegisterGeneric(typeof(WeakCache<>)).As(typeof(WeakCache<>), typeof(IDataCache<>)).InstancePerLifetimeScope();
			builder.RegisterType<DomainEventSource>().As<IDomainEventSource>().InstancePerLifetimeScope();
			builder.RegisterType<DomainEventStore>().As<IDomainEventStore>().InstancePerLifetimeScope();
			builder.RegisterGeneric(typeof(SingleDomainEventSource<>)).As(typeof(IDomainEventSource<>)).InstancePerLifetimeScope();
			builder.RegisterGeneric(typeof(RegisterChangeNotifications<>)).As(typeof(IObservable<>)).SingleInstance();
			builder.RegisterType<DataContext>().As<IDataContext>().InstancePerLifetimeScope();
		}

		private static void SetupPostgres(Revenj.Extensibility.Autofac.ContainerBuilder builder, string cs)
		{
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
	}
}