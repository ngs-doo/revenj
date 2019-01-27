using Microsoft.Extensions.DependencyInjection;
using Revenj.Common;
using Revenj.Extensibility;
using System;

namespace Revenj.AspNetCore
{
	internal class AspNetContainer : IObjectFactory, IServiceScope, IServiceScopeFactory, ISupportRequiredService
	{
		private readonly IObjectFactory Container;

		public AspNetContainer(IObjectFactory container)
		{
			this.Container = container;
			Container.RegisterInstance<IServiceProvider>(this);
			Container.RegisterInstance<IObjectFactory>(this);
			Container.RegisterInstance<IServiceScope>(this);
			Container.RegisterInstance<IServiceScopeFactory>(this);
			Container.RegisterInstance<ISupportRequiredService>(this);
		}

		public IServiceProvider ServiceProvider => Container;

		public IServiceScope CreateScope()
		{
			return new AspNetContainer(Container.CreateScope(null));
		}

		public IObjectFactory CreateScope(string id)
		{
			return new AspNetContainer(Container.CreateScope(id));
		}

		public IServiceProvider CreateServiceProvider(IObjectFactory container)
		{
			return container;
		}

		public void Dispose()
		{
			Container.Dispose();
		}

		public IObjectFactory FindScope(string id)
		{
			return Container.FindScope(id);
		}

		public object GetRequiredService(Type serviceType)
		{
			var result = GetService(serviceType);
			if (result == null) throw new FrameworkException("Unable to resolve " + serviceType);
			return result;
		}

		public object GetService(Type serviceType)
		{
			return Container.GetService(serviceType);
		}

		public bool IsRegistered(Type type)
		{
			return Container.IsRegistered(type);
		}

		public void Register(IObjectFactoryBuilder builder)
		{
			Container.Register(builder);
		}

		public object Resolve(Type type, object[] args)
		{
			return Container.Resolve(type, args);
		}
	}
}
