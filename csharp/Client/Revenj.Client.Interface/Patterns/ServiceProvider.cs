using System;

namespace Revenj
{
	public static class ServiceProviderHelper
	{
		public static T Resolve<T>(this IServiceProvider locator)
		{
			var value = (T)locator.GetService(typeof(T));
			if (value == null)
				throw new InvalidOperationException("Unable to resolve: " + typeof(T));
			return value;
		}
	}
}
