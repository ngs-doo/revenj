using System;
using System.Diagnostics.Contracts;
using System.Linq;
using Revenj.Extensibility;

namespace Revenj.DomainPatterns
{
	public class ServiceLocator : IServiceLocator, IServiceProvider
	{
		private readonly IObjectFactory ObjectFactory;

		public ServiceLocator(IObjectFactory objectFactory)
		{
			Contract.Requires(objectFactory != null);

			this.ObjectFactory = objectFactory;
		}

		public object Resolve(Type type)
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
						if (ctorParams[0].ParameterType == typeof(IServiceLocator))
							return Activator.CreateInstance(type, this);
						if (ctorParams[0].ParameterType == typeof(IObjectFactory))
							return Activator.CreateInstance(type, ObjectFactory);
					}
					var ctorArguments = new object[ctorParams.Length];
					for (int i = 0; i < ctorParams.Length; i++)
					{
						var ca = ctorParams[i].ParameterType;
						try
						{
							ctorArguments[i] = Resolve(ca);
						}
						catch (Exception ex)
						{
							throw new ArgumentException("Can't resolve {0}. Dependency {1} can't be created.".With(type.FullName, ca.FullName), ex);
						}
					}
					return Activator.CreateInstance(type, ctorArguments);
				}
				throw new ArgumentException(@"Can't resolve {0}. Failed to find public constructor for type {0}.
Please register {0} to container or use another method to construct it's instance.".With(type.FullName));
			}
			throw new ArgumentException(@"Can't resolve {0}.
Please register {0} to container or use another method to construct it's instance.".With(type.FullName));
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
						if (ctorParams[0].ParameterType == typeof(IServiceLocator))
							return Activator.CreateInstance(type, this);
						if (ctorParams[0].ParameterType == typeof(IObjectFactory))
							return Activator.CreateInstance(type, ObjectFactory);
					}
					var ctorArguments = new object[ctorParams.Length];
					for (int i = 0; i < ctorParams.Length; i++)
					{
						var arg = Resolve(ctorParams[i].ParameterType);
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
