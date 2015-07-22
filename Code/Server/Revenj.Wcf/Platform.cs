using System;
using System.Configuration;
using System.Linq;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Wcf;

namespace DSL
{
	public static class Platform
	{
		public enum Container
		{
			Autofac,
			DryIoc
		}

		public static IServiceProvider Start(Container container = Container.Autofac)
		{
			return Start<IServiceProvider>(container);
		}

		public static TService Start<TService>(Container container, params Type[] types)
		{
			var state = new ServerState();
			var withAspects = ConfigurationManager.AppSettings["Revenj.AllowAspects"] == "true";
			var builder = container == Container.Autofac ? Revenj.Extensibility.Setup.UseAutofac(true, false, withAspects) : Revenj.Extensibility.Setup.UseDryIoc();
			builder.RegisterSingleton<ISystemState>(state);
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
