using System;
using Autofac;
using Autofac.Configuration;
using NGS.DomainPatterns;
using NGS.Extensibility;
using Revenj.Wcf;

namespace DSL
{
	public static class Platform
	{
		public static IServiceLocator Start()
		{
			return Start<IServiceLocator>();
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
			var locator = objectFactory.Resolve<IServiceLocator>();
			ContainerWcfHost.Resolver = s => locator.Resolve(s, null);
			state.Started(objectFactory);
			return locator.Resolve<TService>();
		}
	}
}
