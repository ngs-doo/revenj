using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Security;
using System.Security.Principal;
using System.Xml.Linq;
using Revenj.Core;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace DSL
{
	public static class Core
	{
		public static IServiceProvider Setup(
			bool withAspects = false,
			bool externalConfiguration = false,
			Action<IObjectFactoryBuilder> setupDatabase = null)
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
			var builder = Revenj.Extensibility.Setup.UseAutofac(assemblies, dllPlugins, externalConfiguration, false, withAspects);
			return Setup(builder, withAspects, externalConfiguration, setupDatabase != null ? new[] { setupDatabase } : new Action<IObjectFactoryBuilder>[0]);
		}

		public static IServiceProvider Setup(
			Revenj.Extensibility.Setup.IContainerBuilder builder,
			bool withAspects,
			bool externalConfiguration,
			params Action<IObjectFactoryBuilder>[] setupExternal)
		{
			var state = new SystemState();
			builder.RegisterSingleton<ISystemState>(state);
			foreach (var se in setupExternal)
				se(builder);
			var serverModels =
				(from asm in Revenj.Utility.AssemblyScanner.GetAssemblies()
				 let type = asm.GetType("SystemBoot.Configuration")
				 where type != null && type.GetMethod("Initialize") != null
				 select asm)
				.ToList();
			builder.ConfigurePatterns(_ => serverModels);
			builder.ConfigureSerialization();
			builder.ConfigureSecurity(false);
			builder.RegisterFunc<IEnumerable<Assembly>>(f => AssemblyScanner.GetAssemblies());
			builder.RegisterFunc<IEnumerable<Type>>(f => AssemblyScanner.GetAllTypes());

			var factory = builder.Build();
			factory.Resolve<IDomainModel>();//TODO: explicit model initialization
			state.IsBooting = false;
			state.Started(factory);
			return factory;
		}

		public static void ConfigureSecurity(this IObjectFactoryBuilder builder, bool withAuth)
		{
			if (withAuth)
			{
				builder.RegisterType(typeof(RepositoryAuthentication), InstanceScope.Singleton, false,
					typeof(IAuthentication<SecureString>),
					typeof(IAuthentication<string>),
					typeof(IAuthentication<byte[]>));
				builder.RegisterType<RepositoryPrincipalFactory, IPrincipalFactory>();
			}
			builder.RegisterType<PermissionManager, IPermissionManager>(InstanceScope.Singleton);
			builder.RegisterFunc<IPrincipal>(_ => System.Threading.Thread.CurrentPrincipal, InstanceScope.Context);
		}

		public static void ConfigurePatterns(this IObjectFactoryBuilder builder, Func<IObjectFactory, IEnumerable<Assembly>> getDomainModels)
		{
			builder.RegisterFunc<IDomainModel>(c => new DomainModel(getDomainModels(c), c), InstanceScope.Singleton);
			builder.RegisterType<DomainTypeResolver, ITypeResolver>(InstanceScope.Singleton);
			builder.RegisterType(typeof(WeakCache<>), InstanceScope.Context, true, typeof(WeakCache<>), typeof(IDataCache<>));
			builder.RegisterType<DomainEventSource, IDomainEventSource>(InstanceScope.Context);
			builder.RegisterType<DomainEventStore, IDomainEventStore>(InstanceScope.Context);
			builder.RegisterType<GlobalEventStore>(InstanceScope.Context);
			builder.RegisterType(typeof(SingleDomainEventSource<>), InstanceScope.Context, true, typeof(IDomainEventSource<>));
			builder.RegisterType(typeof(RegisterChangeNotifications<>), InstanceScope.Singleton, true, typeof(IObservable<>));
			builder.RegisterType<DataContext, IDataContext>(InstanceScope.Context);
		}

		public static void ConfigureSerialization(this IObjectFactoryBuilder builder)
		{
			builder.RegisterType<GenericDataContractResolver>(InstanceScope.Singleton);
			builder.RegisterType(typeof(XmlSerialization), InstanceScope.Singleton, false, typeof(ISerialization<XElement>));
			builder.RegisterType<GenericDeserializationBinder, GenericDeserializationBinder, SerializationBinder>(InstanceScope.Singleton);
			builder.RegisterType(typeof(BinarySerialization), InstanceScope.Singleton, false, typeof(ISerialization<byte[]>));
			builder.RegisterType(typeof(JsonSerialization), InstanceScope.Singleton, false, typeof(ISerialization<string>), typeof(ISerialization<TextReader>));
			builder.RegisterType(typeof(ProtobufSerialization), InstanceScope.Singleton, false, typeof(ISerialization<MemoryStream>), typeof(ISerialization<Stream>));
			builder.RegisterType(typeof(PassThroughSerialization), InstanceScope.Singleton, false, typeof(ISerialization<object>));
			builder.RegisterType<WireSerialization, IWireSerialization>(InstanceScope.Singleton);
		}
	}
}
namespace Revenj
{
	public static class Postgres
	{
		public static IServiceProvider Setup(
			string connectionString,
			bool withAspects = false,
			bool externalConfiguration = false)
		{
			return DSL.Core.Setup(withAspects, externalConfiguration, b => Revenj.DatabasePersistence.Postgres.Setup.ConfigurePostgres(b, connectionString));
		}
	}
}