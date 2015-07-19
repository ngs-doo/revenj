using System;
using DryIoc;
using Revenj.Extensibility;
using Revenj.Http;

namespace DSL
{
	[Obsolete("Not functional yet. DryIoc still has some issues.")]
	internal static class PlatformDry
	{
		public static IServiceProvider Start()
		{
			return Start<IServiceProvider>();
		}

		public static TService Start<TService>(params Type[] types)
		{
			var container = new Container(DryIocConfiguration.Load);
			if (!container.IsRegistered<TService>() && typeof(TService).IsClass)
				container.Register(new ReflectionFactory(typeof(TService)), typeof(TService), null);
			foreach (var t in types)
				container.Register(new ReflectionFactory(t), t, null);
			container.RegisterInstance(container);
			var objectFactory = container.Resolve<IObjectFactory>();
			DryIocConfiguration.Start(objectFactory);
			return objectFactory.Resolve<TService>();
		}
	}
}
