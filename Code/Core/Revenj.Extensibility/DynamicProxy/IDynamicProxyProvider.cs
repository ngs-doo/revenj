using System;
using Castle.DynamicProxy;

namespace Revenj.Extensibility
{
	public interface IDynamicProxyProvider
	{
		TProxyType CreateInterfaceProxy<TProxyType>(
			Type interfaceToProxy,
			TProxyType instance,
			Type[] typesToProxy,
			IInterceptorSelector selector,
			IInterceptor[] interceptors);

		object CreateClassProxy(
			Type classToProxy,
			Type[] typesToProxy,
			object[] arguments,
			IInterceptorSelector selector,
			IInterceptor[] interceptors);
	}
}
