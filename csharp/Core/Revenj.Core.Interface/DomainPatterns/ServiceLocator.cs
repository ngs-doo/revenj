using System;
using System.Diagnostics.Contracts;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Utility for service locator resolution
	/// </summary>
	public static class ServiceProviderHelper
	{
		/// <summary>
		/// Resolve service using provided type.
		/// </summary>
		/// <typeparam name="T">service type</typeparam>
		/// <param name="provider">service locator</param>
		/// <returns>resolved service</returns>
		public static T Resolve<T>(this IServiceProvider provider)
		{
			Contract.Requires(provider != null);

			var instance = provider.GetService(typeof(T));
			if (instance == null) throw new NotSupportedException(@"Requested type not found in services: " + typeof(T).FullName + @"
Use GetService API to avoid this exception and get null value instead.
Check if service should be registered or it's dependencies satisfied");

			return (T)instance;
		}
		/// <summary>
		/// Resolve service using provided type and cast it to appropriate result.
		/// </summary>
		/// <typeparam name="T">result type</typeparam>
		/// <param name="provider">service locator</param>
		/// <param name="type">service type</param>
		/// <returns>casted resolved service</returns>
		public static T Resolve<T>(this IServiceProvider provider, Type type)
		{
			Contract.Requires(provider != null);

			var instance = provider.GetService(type);
			if (instance == null) throw new NotSupportedException(@"Requested type not found in services: " + type.FullName + @"
Use GetService API to avoid this exception and get null value instead.
Check if service should be registered or it's dependencies satisfied");

			return (T)instance;
		}
	}
}
