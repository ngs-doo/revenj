using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.ServiceModel;
using DryIoc;
using Revenj.Extensibility.Autofac;
using Revenj.Extensibility.Autofac.Configuration;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Utility;

namespace Revenj.Extensibility
{
	public static class Setup
	{
		public interface IContainerBuilder : IObjectFactoryBuilder
		{
			IObjectFactory Build();
		}

		class AutofacContainerBuilder : IContainerBuilder
		{
			public readonly ContainerBuilder Builder;
			private readonly bool DslAspects;

			public AutofacContainerBuilder(
				IEnumerable<Assembly> pluginAssemblies,
				IEnumerable<string> pluginPaths,
				bool loadModules,
				bool withAspects,
				bool dslAspects)
			{
				this.Builder = new ContainerBuilder();
				this.DslAspects = dslAspects;
				Builder.RegisterType<AutofacObjectFactory, IObjectFactory, IServiceProvider>(InstanceScope.Singleton);
				Builder.RegisterType<AutofacMefProvider, IExtensibilityProvider>(InstanceScope.Singleton);
				var dynamicProxy = new CastleDynamicProxyProvider();
				Builder.RegisterSingleton<IMixinProvider>(dynamicProxy);
				Builder.RegisterSingleton<IDynamicProxyProvider>(dynamicProxy);
				var aopRepository = new AspectRepository(dynamicProxy);
				Builder.RegisterSingleton<IAspectRegistrator>(aopRepository);
				Builder.RegisterSingleton<IAspectComposer>(aopRepository);
				Builder.RegisterSingleton<IInterceptorRegistrator>(aopRepository);
				Builder.RegisterSingleton(new PluginsConfiguration
				{
					Directories = (pluginPaths ?? new string[0]).ToList(),
					Assemblies = (pluginAssemblies ?? new Assembly[0]).ToList()
				});
				Builder.RegisterType<SystemInitialization>();
				Builder.RegisterType(typeof(PluginRepository<>), InstanceScope.Singleton, true, typeof(IPluginRepository<>));
				if (withAspects)
					Builder.RegisterModule(new AspectsModule(aopRepository));
				if (loadModules)
				{
					var types = AssemblyScanner.GetAllTypes();
					foreach (var m in types)
					{
						if (m.Assembly.FullName.StartsWith("Revenj."))
							continue;
						if (m.IsPublic && !m.IsAbstract && typeof(Revenj.Extensibility.Autofac.Module).IsAssignableFrom(m) && m.GetConstructor(new Type[0]) != null)
							Builder.RegisterModule((Revenj.Extensibility.Autofac.Module)Activator.CreateInstance(m));
					}
				}
			}

			public IEnumerable<IFactoryBuilderInstance> Instances { get { return Builder.Instances; } }
			public IEnumerable<IFactoryBuilderType> Types { get { return Builder.Types; } }
			public IEnumerable<IFactoryBuilderFunc> Funcs { get { return Builder.Funcs; } }
			public void Add(IFactoryBuilderInstance item) { Builder.Add(item); }
			public void Add(IFactoryBuilderType item) { Builder.Add(item); }
			public void Add(IFactoryBuilderFunc item) { Builder.Add(item); }

			public IObjectFactory Build()
			{
				var container = Builder.Build();
				var factory = container.Resolve<IObjectFactory>();
				var init = factory.Resolve<SystemInitialization>();
				init.Initialize(DslAspects);
				return factory;
			}
		}

		public static IContainerBuilder UseAutofac(
			IEnumerable<Assembly> pluginAssemblies,
			IEnumerable<string> pluginPaths,
			bool withExternalConfiguration = true,
			bool loadModules = false,
			bool withAspects = false,
			bool dslAspects = false)
		{
			var builder = new AutofacContainerBuilder(pluginAssemblies, pluginPaths, loadModules, withAspects, dslAspects);
			if (withExternalConfiguration)
				builder.Builder.RegisterModule(new ConfigurationSettingsReader("autofacConfiguration"));
			return builder;
		}

		class DryIocContainerBuilder : IContainerBuilder
		{
			public readonly IObjectFactory Factory;
			private readonly bool DslAspects;
			private readonly PluginsConfiguration Plugins;
			private readonly CastleDynamicProxyProvider Proxy;

			public DryIocContainerBuilder(
				IEnumerable<Assembly> pluginAssemblies,
				IEnumerable<string> pluginPaths,
				bool dslAspects = false)
			{
				this.DslAspects = dslAspects;
				var container = new DryIoc.Container(rules => rules.With(FactoryMethod.ConstructorWithResolvableArguments)).OpenScopeWithoutContext();
				Proxy = new CastleDynamicProxyProvider();
				this.RegisterSingleton<IMixinProvider>(Proxy);
				this.RegisterSingleton<IDynamicProxyProvider>(Proxy);
				var aopRepository = new AspectRepository(Proxy);
				this.RegisterSingleton<IAspectRegistrator>(aopRepository);
				this.RegisterSingleton<IAspectComposer>(aopRepository);
				this.RegisterSingleton<IInterceptorRegistrator>(aopRepository);
				Factory = new DryIocObjectFactory(container, aopRepository);
				this.RegisterSingleton<IObjectFactory>(Factory);
				this.RegisterSingleton<IServiceProvider>(Factory);
				Plugins = new PluginsConfiguration
				{
					Directories = (pluginPaths ?? new string[0]).ToList(),
					Assemblies = (pluginAssemblies ?? new Assembly[0]).ToList()
				};
				this.RegisterSingleton(Plugins);
				this.RegisterType<SystemInitialization>();
				this.RegisterType(typeof(PluginRepository<>), InstanceScope.Singleton, true, typeof(IPluginRepository<>));
				this.RegisterSingleton<IExtensibilityProvider>(new DryIocMefProvider(Plugins, Proxy, container));
				DryIocObjectFactory.RegisterToContainer(container, this);
			}

			private List<IFactoryBuilderInstance> instances = new List<IFactoryBuilderInstance>();
			private List<IFactoryBuilderType> types = new List<IFactoryBuilderType>();
			private List<IFactoryBuilderFunc> funcs = new List<IFactoryBuilderFunc>();

			public IEnumerable<IFactoryBuilderInstance> Instances { get { return instances; } }
			public IEnumerable<IFactoryBuilderType> Types { get { return types; } }
			public IEnumerable<IFactoryBuilderFunc> Funcs { get { return funcs; } }
			public void Add(IFactoryBuilderInstance item) { instances.Add(item); }
			public void Add(IFactoryBuilderType item) { types.Add(item); }
			public void Add(IFactoryBuilderFunc item) { funcs.Add(item); }

			public IObjectFactory Build()
			{
				var init = Factory.Resolve<SystemInitialization>();
				init.Initialize(DslAspects);
				return Factory;
			}
		}

		class GenericContainerBuilder<TObjectFactory, TExtensibilityProvider> : GenericContainerBuilder
			where TObjectFactory : IObjectFactory
			where TExtensibilityProvider : IExtensibilityProvider
		{
			public GenericContainerBuilder(
				IContainerBuilder builder,
				IEnumerable<Assembly> pluginAssemblies,
				IEnumerable<string> pluginPaths)
				: base(builder, pluginAssemblies, pluginPaths)
			{
				this.RegisterType<TObjectFactory, IObjectFactory, IServiceProvider>(InstanceScope.Singleton);
				this.RegisterType<TExtensibilityProvider, IExtensibilityProvider>(InstanceScope.Singleton);
			}
		}

		class GenericContainerBuilder : IContainerBuilder
		{
			public readonly IContainerBuilder Builder;

			public GenericContainerBuilder(
				IContainerBuilder builder,
				IEnumerable<Assembly> pluginAssemblies,
				IEnumerable<string> pluginPaths)
			{
				this.Builder = builder;
				var dynamicProxy = new CastleDynamicProxyProvider();
				this.RegisterSingleton<IMixinProvider>(dynamicProxy);
				this.RegisterSingleton<IDynamicProxyProvider>(dynamicProxy);
				var aopRepository = new AspectRepository(dynamicProxy);
				this.RegisterSingleton<IAspectRegistrator>(aopRepository);
				this.RegisterSingleton<IAspectComposer>(aopRepository);
				this.RegisterSingleton<IInterceptorRegistrator>(aopRepository);
				this.RegisterSingleton(new PluginsConfiguration
				{
					Directories = (pluginPaths ?? new string[0]).ToList(),
					Assemblies = (pluginAssemblies ?? new Assembly[0]).ToList()
				});
				this.RegisterType<SystemInitialization>();
				this.RegisterType(typeof(PluginRepository<>), InstanceScope.Singleton, true, typeof(IPluginRepository<>));
			}

			public IEnumerable<IFactoryBuilderInstance> Instances { get { return Builder.Instances; } }
			public IEnumerable<IFactoryBuilderType> Types { get { return Builder.Types; } }
			public IEnumerable<IFactoryBuilderFunc> Funcs { get { return Builder.Funcs; } }
			public void Add(IFactoryBuilderInstance item) { Builder.Add(item); }
			public void Add(IFactoryBuilderType item) { Builder.Add(item); }
			public void Add(IFactoryBuilderFunc item) { Builder.Add(item); }

			public IObjectFactory Build()
			{
				var factory = Builder.Build();
				var init = factory.Resolve<SystemInitialization>();
				init.Initialize(false);
				return factory;
			}
		}

		public static IContainerBuilder UseDryIoc(
			IEnumerable<Assembly> pluginAssemblies,
			IEnumerable<string> pluginPaths,
			bool dslAspects = false)
		{
			return new DryIocContainerBuilder(pluginAssemblies, pluginPaths, dslAspects);
		}

		public static IContainerBuilder UseContainer<TObjectFactory, TExtensibilityProvider>(
			IContainerBuilder builder,
			IEnumerable<Assembly> pluginAssemblies,
			IEnumerable<string> pluginPaths)
			where TObjectFactory : IObjectFactory
			where TExtensibilityProvider : IExtensibilityProvider
		{
			return new GenericContainerBuilder<TObjectFactory, TExtensibilityProvider>(builder, pluginAssemblies, pluginPaths);
		}

		public static IContainerBuilder UseContainer(
			IContainerBuilder builder,
			IEnumerable<Assembly> pluginAssemblies,
			IEnumerable<string> pluginPaths)
		{
			return new GenericContainerBuilder(builder, pluginAssemblies, pluginPaths);
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
	}
}
