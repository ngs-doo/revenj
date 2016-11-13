using System;
using System.Collections.Generic;
using System.Linq;

namespace Revenj
{
	internal class DictionaryServiceLocator : IServiceProvider
	{
		private readonly Dictionary<Type, object> Registrations = new Dictionary<Type, object>();
		private const bool CacheResult = true;

		public object GetService(Type service)
		{
			try
			{
				return Resolve(service, true);
			}
			catch
			{
				return null;
			}
		}

		private void CacheIf(Type target, object service)
		{
			if (CacheResult && service != null && !(service is IDisposable))
				Register(target, service);
		}

		private object Resolve(Type target, bool checkErrors)
		{
			object found;
			if (Registrations.TryGetValue(target, out found))
			{
				var type = found as Type;
				if (type != null)
				{
					var resolved = TryResolveType(type);
					if (checkErrors && resolved == null)
						throw new KeyNotFoundException("Can't resolve dependencies for service: " + target.FullName);
					return resolved;
				}
				return found;
			}
			if (target.IsGenericType)
			{
				var generic = target.GetGenericTypeDefinition();
				if (Registrations.TryGetValue(generic, out found))
				{
					var args = target.GetGenericArguments();
					var type = found as Type;
					if (type != null)
					{
						var resolved = TryResolveType(type.MakeGenericType(args));
						if (checkErrors && resolved == null)
							throw new KeyNotFoundException("Can't resolve dependencies for service: " + target.FullName);
						CacheIf(target, resolved);
						return resolved;
					}
					return found;
				}
			}
#if PORTABLE
			if (target.IsClass && !target.IsAbstract && (target.IsPublic || target.IsNestedPublic))
#else
			if (target.IsClass && !target.IsAbstract)
#endif
			{
				var resolved = TryResolveType(target);
				if (checkErrors && resolved == null)
					throw new KeyNotFoundException("Can't resolve dependencies for service: " + target.FullName);
				CacheIf(target, resolved);
				return resolved;
			}
			if (checkErrors)
				throw new KeyNotFoundException("Can't find service: " + target.FullName);
			return null;
		}

		private object TryResolveType(Type target)
		{
			var ctors =
				(from ctr in target.GetConstructors()
				 let args = ctr.GetParameters()
				 orderby args.Length descending
				 select new { args, ctr });
			foreach (var it in ctors)
			{
				var args = new List<object>();
				foreach (var a in it.args)
					args.Add(Resolve(a.ParameterType, false));
				if (args.Contains(null))
					continue;
				var instance = it.ctr.Invoke(args.ToArray());
				CacheIf(target, instance);
				return instance;
			}
			return null;
		}

		public void Register(Type target, object service)
		{
			lock (Registrations)
				Registrations[target] = service;
		}
	}
}
