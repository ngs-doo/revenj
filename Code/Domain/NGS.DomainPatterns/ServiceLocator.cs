using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using NGS.Extensibility;

namespace NGS.DomainPatterns
{
	public class ServiceLocator : IServiceLocator
	{
		private readonly IObjectFactory ObjectFactory;

		public ServiceLocator(IObjectFactory objectFactory)
		{
			Contract.Requires(objectFactory != null);

			this.ObjectFactory = objectFactory;
		}

		public object Resolve(Type type, object[] args)
		{
			if ((args == null || args.Length == 0) && ObjectFactory.IsRegistered(type))
				return ObjectFactory.Resolve(type, null);
			if (args != null && args.Length > 0)
				return ObjectFactory.Resolve(type, args);
			if (type.IsClass)
			{
				var ctors = type.GetConstructors();
				if (ctors.Length == 1)
				{
					var ctorParams = ctors[0].GetParameters();
					if (ctorParams.Length == 0)
						return Activator.CreateInstance(type);
					if (ctorParams.Length == 1)
					{
						if (ctorParams[0].ParameterType == typeof(IServiceLocator))
							return Activator.CreateInstance(type, this);
						if (ctorParams[0].ParameterType == typeof(IServiceLocator))
							return Activator.CreateInstance(type, ObjectFactory);
					}
					var ctorArguments = new List<object>();
					foreach (var ca in ctorParams.Select(it => it.ParameterType))
					{
						try
						{
							ctorArguments.Add(Resolve(ca, null));
						}
						catch (Exception ex)
						{
							throw new ArgumentException("Can't resolve {0}. Dependency {1} can't be created.".With(type.FullName, ca.FullName), ex);
						}
					}
					return Activator.CreateInstance(type, ctorArguments.ToArray());
				}
				throw new ArgumentException(@"Can't resolve {0}. Only types with single constructor which are not registered into container can be automatically resolved.
Please register {0} to container or use another method to construct it's instance.".With(type.FullName));
			}
			return ObjectFactory.Resolve(type, args);
		}
	}
}
