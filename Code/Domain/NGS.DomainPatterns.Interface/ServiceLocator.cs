using System;
using System.Diagnostics.Contracts;

namespace NGS.DomainPatterns
{
	/// <summary>
	/// Service for resolving other services.
	/// Unregistered services will be resolved too.
	/// </summary>
	public interface IServiceLocator
	{
		/// <summary>
		/// Resolve service registered as type using provided arguments.
		/// Interfaces can be resolved only if registered into the container.
		/// </summary>
		/// <param name="type">service class/interface</param>
		/// <param name="args">service class arguments</param>
		/// <returns>resolved service</returns>
		object Resolve(Type type);
	}

	/// <summary>
	/// Utility for service locator resolution
	/// </summary>
	public static class ServiceLocatorHelper
	{
		/// <summary>
		/// Resolve service using provided type.
		/// </summary>
		/// <typeparam name="T">service type</typeparam>
		/// <param name="locator">service locator</param>
		/// <returns>resolved service</returns>
		public static T Resolve<T>(this IServiceLocator locator)
		{
			Contract.Requires(locator != null);

			return (T)locator.Resolve(typeof(T));
		}
		/// <summary>
		/// Resolve service using provided type and cast it to appropriate result.
		/// </summary>
		/// <typeparam name="T">result type</typeparam>
		/// <param name="locator">service locator</param>
		/// <param name="type">service type</param>
		/// <returns>casted resolved service</returns>
		public static T Resolve<T>(this IServiceLocator locator, Type type)
		{
			Contract.Requires(locator != null);

			return (T)locator.Resolve(type);
		}
	}
}
