using System;
using System.Collections.Generic;
using System.Linq;
using Castle.DynamicProxy;
using Revenj.Common;

namespace Revenj.Extensibility
{
	public class CastleDynamicProxyProvider : IMixinProvider, IDynamicProxyProvider
	{
		private static readonly ProxyGenerator Generator = new ProxyGenerator(new PersistentProxyBuilder());

		public object Create(Type mixinType, object[] args, IEnumerable<object> implementations)
		{
			var options = new ProxyGenerationOptions();

			if (implementations != null)
				foreach (var impl in implementations)
					options.AddMixinInstance(impl);

			try
			{
				if (!options.HasMixins && !mixinType.IsAbstract)
					return Activator.CreateInstance(mixinType, args);

				return Generator.CreateClassProxy(mixinType, options, args);
			}
			catch (MissingMethodException mme)
			{
				throw new FrameworkException(
					"Can't create instance of an {0}. Arguments: ({1})".With(
						mixinType.Name,
						string.Join(", ", args.Select(it => "{0}: {1}".With(
							it != null ? it.GetType().Name : "<unknown>",
							it != null ? it.ToString() : "<null>")))),
					mme);
			}
		}

		private static Type ProxyTargetType = typeof(IProxyTargetAccessor);

		public TProxyType CreateInterfaceProxy<TProxyType>(
			Type interfaceToProxy,
			TProxyType instance,
			Type[] typesToProxy,
			IInterceptorSelector selector,
			IInterceptor[] interceptors)
		{
			var options = new ProxyGenerationOptions();
			if (selector != null)
				options.Selector = selector;

			return (TProxyType)Generator.CreateInterfaceProxyWithTarget(
				interfaceToProxy,
				typesToProxy = typesToProxy != null ? typesToProxy.Where(it => it != ProxyTargetType).ToArray() : null,
				instance,
				options,
				interceptors);
		}

		public object CreateClassProxy(
			Type classToProxy,
			Type[] typesToProxy,
			object[] arguments,
			IInterceptorSelector selector,
			IInterceptor[] interceptors)
		{
			var options = new ProxyGenerationOptions();
			if (selector != null)
				options.Selector = selector;

			return Generator.CreateClassProxy(
				classToProxy,
				typesToProxy = typesToProxy != null ? typesToProxy.Where(it => it != ProxyTargetType).ToArray() : null,
				options,
				arguments,
				interceptors);
		}
	}
}
