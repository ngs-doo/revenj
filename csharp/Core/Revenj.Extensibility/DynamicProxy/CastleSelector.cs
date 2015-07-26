using System;
using System.Reflection;
using Castle.DynamicProxy;

namespace Revenj.Extensibility
{
	internal class CastleSelector : IInterceptorSelector
	{
		public IInterceptor[] SelectInterceptors(Type type, MethodInfo method, IInterceptor[] interceptors)
		{
			return interceptors;
		}
	}
}
