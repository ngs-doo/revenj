using System;
using System.Diagnostics.Contracts;
using System.Linq;
using Revenj.Extensibility;

namespace Revenj.DomainPatterns
{
	public class ServiceLocator : IServiceProvider
	{
		private readonly IObjectFactory ObjectFactory;

		public ServiceLocator(IObjectFactory objectFactory)
		{
			Contract.Requires(objectFactory != null);

			this.ObjectFactory = objectFactory;
		}

		public object GetService(Type type)
		{
			if (ObjectFactory.IsRegistered(type))
				return ObjectFactory.Resolve(type, null);
			if (type.IsClass)
			{
				var ctor = type.GetConstructors().FirstOrDefault(it => it.IsPublic);
				if (ctor != null)
				{
					var ctorParams = ctor.GetParameters();
					if (ctorParams.Length == 0)
						return Activator.CreateInstance(type);
					if (ctorParams.Length == 1)
					{
						if (ctorParams[0].ParameterType == typeof(IServiceProvider))
							return Activator.CreateInstance(type, this);
						if (ctorParams[0].ParameterType == typeof(IObjectFactory))
							return Activator.CreateInstance(type, ObjectFactory);
					}
					var ctorArguments = new object[ctorParams.Length];
					for (int i = 0; i < ctorParams.Length; i++)
					{
						var arg = GetService(ctorParams[i].ParameterType);
						if (arg == null)
							return null;
						ctorArguments[i] = arg;
					}
					return Activator.CreateInstance(type, ctorArguments);
				}
			}
			return null;
		}
	}
}
