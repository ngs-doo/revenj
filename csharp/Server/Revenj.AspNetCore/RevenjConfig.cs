using System;
using DSL;
using System.Linq;
using System.Collections.Generic;
using Microsoft.Extensions.DependencyInjection;
using Revenj.Extensibility;
using System.IO;
using System.Reflection;
using Revenj.DomainPatterns;
using Revenj.Utility;
using Revenj.Processing;
using System.Security.Principal;
using Revenj.Security;
using System.Linq.Expressions;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;

namespace Revenj.AspNetCore
{
	public interface IRevenjConfig
	{
		IRevenjConfig WithAOP();
		IRevenjConfig UseRevenjServiceProvider();
		IRevenjConfig ImportPlugins(string path);
		IRevenjConfig ImportPlugins(Assembly assembly);
		IRevenjConfig UsingContainer(Extensibility.Setup.IContainerBuilder container);
		IRevenjConfig OnInitialize(ISystemAspect aspect);
		IRevenjConfig SecurityCheck(IPermissionManager permissions);
		IWebHostBuilder Configure(string connectionString);
	}

	internal class RevenjConfig : IRevenjConfig
	{
		private readonly IWebHostBuilder Builder;
		private bool WithAspects;
		private bool ReplaceProvider;
		private readonly List<string> DllPlugins = new List<string>();
		private readonly List<Assembly> AssemblyPlugins = new List<Assembly>();
		private readonly List<ISystemAspect> Aspects = new List<ISystemAspect>();
		private Extensibility.Setup.IContainerBuilder Container;
		private IPermissionManager Permissions = new SkipPermissionChecks();

		public RevenjConfig(IWebHostBuilder builder)
		{
			this.Builder = builder;
		}

		public IRevenjConfig WithAOP()
		{
			WithAspects = true;
			return this;
		}
		public IRevenjConfig UseRevenjServiceProvider()
		{
			ReplaceProvider = true;
			return this;
		}
		public IRevenjConfig ImportPlugins(string path)
		{
			if (path == null) throw new ArgumentNullException("path");
			if (!Directory.Exists(path)) throw new ArgumentException("Invalid plugin folder " + path);
			if (!DllPlugins.Contains(path))
				DllPlugins.Add(path);
			return this;
		}
		public IRevenjConfig ImportPlugins(Assembly assembly)
		{
			if (assembly == null) throw new ArgumentNullException("assembly");
			if (!AssemblyPlugins.Contains(assembly))
				AssemblyPlugins.Add(assembly);
			return this;
		}
		public IRevenjConfig UsingContainer(Extensibility.Setup.IContainerBuilder container)
		{
			Container = container;
			return this;
		}
		public IRevenjConfig OnInitialize(ISystemAspect aspect)
		{
			if (aspect == null) throw new ArgumentNullException("aspect");
			if (!Aspects.Contains(aspect))
				Aspects.Add(aspect);
			return this;
		}
		public IRevenjConfig SecurityCheck(IPermissionManager permissions)
		{
			if (permissions == null) throw new ArgumentNullException("permissions");
			this.Permissions = permissions;
			return this;
		}
		public IWebHostBuilder Configure(string connectionString)
		{
			return Builder.ConfigureServices((context, services) => SetupRevenjWith(connectionString, services));
		}

		private void SetupRevenjWith(string connectionString, IServiceCollection services)
		{
			var provider = new RevenjConfigFactory(connectionString, this);
			if (ReplaceProvider)
			{
				var oldFactory = services.FirstOrDefault(it => it.ServiceType == typeof(IServiceProviderFactory<IServiceCollection>));
				if (oldFactory != null)
					services.Remove(oldFactory);
				services.AddSingleton<IServiceProviderFactory<IServiceCollection>>(provider);
			}
			else services.AddSingleton(provider.CreateFactory(services));
		}

		private class RevenjConfigFactory : IServiceProviderFactory<IServiceCollection>
		{
			private readonly string ConnectionString;
			private readonly RevenjConfig Config;

			public RevenjConfigFactory(string connectionString, RevenjConfig config)
			{
				this.ConnectionString = connectionString;
				this.Config = config;
			}

			public IServiceCollection CreateBuilder(IServiceCollection services)
			{
				return services;
			}

			public IObjectFactory CreateFactory(IServiceCollection services)
			{
				var assemblies =
					(from asm in AssemblyScanner.GetAssemblies()
					 where asm.FullName.StartsWith("Revenj.")
					 select asm)
					 .Union(Config.AssemblyPlugins)
					.ToList();
				var state = new ServerState();
				var builder = Config.Container == null
					? Extensibility.Setup.UseAutofac(assemblies, Config.DllPlugins, withAspects: Config.WithAspects)
					: Extensibility.Setup.UseContainer(Config.Container, assemblies, Config.DllPlugins);
				builder.RegisterSingleton<ISystemState>(state);
				DatabasePersistence.Postgres.Setup.ConfigurePostgres(builder, ConnectionString);
				var serverModels =
					(from asm in AssemblyScanner.GetAssemblies().Union(Config.AssemblyPlugins)
					 let type = asm.GetType("SystemBoot.Configuration")
					 where type != null && type.GetMethod("Initialize") != null
					 select asm)
					.ToList();
				builder.ConfigurePatterns(_ => serverModels);
				builder.ConfigureSerialization();
				//builder.ConfigureSecurity(false);
				builder.RegisterSingleton<IPermissionManager>(Config.Permissions);
				builder.RegisterFunc<IPrincipal>(c => c.Resolve<IHttpContextAccessor>().HttpContext.User, InstanceScope.Context);
				builder.RegisterType<RestApplication, RestApplication>(InstanceScope.Singleton);
				builder.RegisterType<CommandConverter, CommandConverter>(InstanceScope.Singleton);

				builder.ConfigureProcessing();
				builder.RegisterFunc<IEnumerable<Assembly>>(f => AssemblyScanner.GetAssemblies());
				builder.RegisterFunc<IEnumerable<Type>>(f => AssemblyScanner.GetAllTypes());
				foreach (var s in services)
				{
					if (s.ImplementationInstance != null)
						builder.Add(new FactoryBuilderInstance { Instance = s.ImplementationInstance, AsType = s.ServiceType });
					else if (s.ImplementationFactory != null)
						builder.Add(new FactoryBuilderFunc(s));
					else
						builder.RegisterType(s.ImplementationType, Convert(s.Lifetime), s.ServiceType.IsGenericTypeDefinition, s.ServiceType);
				}
				var factory = new AspNetContainer(builder.Build());
				state.IsBooting = false;
				factory.Resolve<IDomainModel>();
				foreach (var ca in Config.Aspects)
					ca.Initialize(factory);
				state.Started(factory);
				return factory;
			}

			public IServiceProvider CreateServiceProvider(IServiceCollection services)
			{
				return CreateFactory(services);
			}
		}

		private class SkipPermissionChecks : IPermissionManager
		{
			public IQueryable<T> ApplyFilters<T>(IPrincipal user, IQueryable<T> data) { return data; }
			public T[] ApplyFilters<T>(IPrincipal user, T[] data) { return data; }
			public bool CanAccess(string identifier, IPrincipal user) { return true; }
			public IDisposable RegisterFilter<T>(Expression<Func<T, bool>> filter, string role, bool inverse) { return null; }
		}

		private class ServerState : ISystemState
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
			public event Action<SystemEvent> Change = f => { };
			public void Notify(SystemEvent value)
			{
				Change(value);
			}
		}
		private class FactoryBuilderInstance : IFactoryBuilderInstance
		{
			public object Instance { get; set; }
			public Type AsType { get; set; }
		}
		private static InstanceScope Convert(ServiceLifetime lifetime)
		{
			if (lifetime == ServiceLifetime.Transient) return InstanceScope.Transient;
			if (lifetime == ServiceLifetime.Singleton) return InstanceScope.Singleton;
			return InstanceScope.Context;
		}
		private class FactoryBuilderFunc : IFactoryBuilderFunc
		{
			public FactoryBuilderFunc(ServiceDescriptor service)
			{
				AsType = new[] { service.ServiceType };
				Func = c => service.ImplementationFactory(c);
				Scope = Convert(service.Lifetime);
			}
			public Func<IObjectFactory, object> Func { get; private set; }
			public InstanceScope Scope { get; private set; }
			public Type[] AsType { get; private set; }
		}
	}
}
