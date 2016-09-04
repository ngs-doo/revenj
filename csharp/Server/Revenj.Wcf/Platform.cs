using System;
using System.Configuration;
using System.IO;
using System.Linq;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Wcf;

namespace DSL
{
	public static class Platform
	{
		internal enum Container
		{
			Autofac,
			DryIoc
		}

		public static IServiceProvider Start()
		{
			return Start<IServiceProvider>();
		}

		public static TService Start<TService>(params Type[] types)
		{
			var state = new ServerState();
			var withAspects = ConfigurationManager.AppSettings["Revenj.AllowAspects"] == "true";
			var dllPlugins =
				(from key in ConfigurationManager.AppSettings.AllKeys
				 where key.StartsWith("PluginsPath", StringComparison.OrdinalIgnoreCase)
				 let path = ConfigurationManager.AppSettings[key]
				 let pathRelative = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path)
				 let chosenPath = Directory.Exists(pathRelative) ? pathRelative : path
				 select chosenPath)
				.ToList();
			var container = Container.Autofac;
			var builder = container == Container.Autofac
				? Revenj.Extensibility.Setup.UseAutofac(null, dllPlugins, true, false, withAspects)
				: Revenj.Extensibility.Setup.UseDryIoc(null, dllPlugins);
			builder.RegisterSingleton<ISystemState>(state);
			if (container == Container.DryIoc)
				StandardModule.Configure(builder, StandardModule.SetupPostgres);
			foreach (var t in types)
				builder.RegisterType(t, InstanceScope.Transient, false, new[] { t }.Union(t.GetInterfaces()).ToArray());
			if (types.Length == 0 && typeof(TService).IsClass)
				builder.RegisterType(typeof(TService), InstanceScope.Transient, false);
			var factory = builder.Build();
			state.IsBooting = false;
			factory.Resolve<IDomainModel>();
			ContainerWcfHost.Resolver = s => factory.GetService(s);
			state.Started(factory);
			return factory.Resolve<TService>();
		}
	}
}
