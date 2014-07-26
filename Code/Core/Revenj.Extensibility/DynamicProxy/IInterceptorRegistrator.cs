using System;
using Castle.DynamicProxy;

namespace Revenj.Extensibility
{
	public interface IInterceptorRegistrator
	{
		void Intercept(Type type, IInterceptor interceptor);
		void Intercept(Func<Type, bool> rule, IInterceptor interceptor);
	}

	public static class InterceptorRegistratorHelper
	{
		public static void Intercept<TWhat>(this IInterceptorRegistrator repository, IInterceptor interceptor)
		{
			repository.Intercept(typeof(TWhat), interceptor);
		}
	}
}
