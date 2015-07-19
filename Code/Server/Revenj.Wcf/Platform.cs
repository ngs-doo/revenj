using System;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Extensibility.Autofac;
using Revenj.Extensibility.Autofac.Configuration;
using Revenj.Wcf;

namespace DSL
{
	public static class Platform
	{
		public static IServiceProvider Start()
		{
			return Start<IServiceProvider>();
		}

		public static TService Start<TService>(params Type[] types)
		{
			var state = new ServerState();
			var builder = new ContainerBuilder();
			builder.RegisterInstance(state).As<ISystemState>();
			builder.RegisterModule(new ConfigurationSettingsReader("autofacConfiguration"));
			foreach (var t in types)
			{
				builder.RegisterType(t);
				foreach (var i in t.GetInterfaces())
					builder.RegisterType(t).As(i);
			}
			if (types == null || types.Length == 0 && typeof(TService).IsClass)
				builder.RegisterType(typeof(TService));
			var container = builder.Build();
			state.IsBooting = false;
			var objectFactory = container.Resolve<IObjectFactory>();
			var locator = objectFactory.Resolve<IServiceProvider>();
			ContainerWcfHost.Resolver = s => locator.GetService(s);
			state.Started(objectFactory);
			return locator.Resolve<TService>();
		}
	}
}
