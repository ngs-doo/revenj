using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;

namespace Revenj.Extensibility
{
	/// <summary>
	/// MEF plugins are resolved from extensibility provider.
	/// Plugins are decorated with Export attribute.
	/// </summary>
	[ContractClass(typeof(ExtensibilityProviderContract))]
	public interface IExtensibilityProvider
	{
		/// <summary>
		/// Find plugin types which implement specified interface and satisfy filter predicate.
		/// Filter predicate tests for plugin type and target type (Implements).
		/// Plugin types are topologically sorted based on their dependencies.
		/// </summary>
		/// <typeparam name="TInterface">plugin must implement provided interface</typeparam>
		/// <param name="filter">filter plugins</param>
		/// <returns>found plugins</returns>
		IEnumerable<Type> FindPlugins<TInterface>(Func<Type, Type, bool> filter);
		/// <summary>
		/// Find implementations for concepts for specified interface which satisfy filter predicate.
		/// Result is collection of found concepts and their implementations.
		/// Implementations are topologically sorted based on their dependencies.
		/// </summary>
		/// <typeparam name="TImplementation">plugin must implement provided interface</typeparam>
		/// <param name="filter">filter implementations</param>
		/// <returns>map of concepts and their implementations</returns>
		Dictionary<Type, List<Type>> FindImplementations<TImplementation>(Func<Type, Type, bool> filter);
		/// <summary>
		/// Find extensions for specified interface.
		/// Extensions are MEF plugins which Export target attribute.
		/// </summary>
		/// <typeparam name="TImplementation">extension must export provided interface</typeparam>
		/// <returns>found extensions</returns>
		Dictionary<Type, Func<Type, object[], TImplementation>> FindExtensions<TImplementation>();
	}

	internal sealed class ExtensibilityProviderContract : IExtensibilityProvider
	{
		public IEnumerable<Type> FindPlugins<TInterface>(Func<Type, Type, bool> filter)
		{
			Contract.Ensures(Contract.Result<IEnumerable<Type>>() != null);

			return null;
		}
		public Dictionary<Type, List<Type>> FindImplementations<TImplementation>(Func<Type, Type, bool> filter)
		{
			Contract.Ensures(Contract.Result<Dictionary<Type, List<Type>>>() != null);

			return null;
		}
		public Dictionary<Type, Func<Type, object[], TImplementation>> FindExtensions<TImplementation>()
		{
			Contract.Ensures(Contract.Result<Dictionary<Type, Func<Type, object[], TImplementation>>>() != null);

			return null;
		}
	}

	/// <summary>
	/// Helper for plugin lookup
	/// </summary>
	public static class ExtensibilityProviderHelper
	{
		/// <summary>
		/// Find all plugin types which implement specified interface/service contract.
		/// Plugin types are topologically sorted based on their dependencies.
		/// </summary>
		/// <typeparam name="TService">plugin must have specified service signature</typeparam>
		/// <param name="provider">extensibility service</param>
		/// <returns>found plugins</returns>
		public static IEnumerable<Type> FindPlugins<TService>(this IExtensibilityProvider provider)
		{
			Contract.Requires(provider != null);

			return provider.FindPlugins<TService>((t, i) => true);
		}

		private static readonly ConcurrentDictionary<Type, object> CachedPlugins = new ConcurrentDictionary<Type, object>(1, 127);

		/// <summary>
		/// Resolve all plugins which implement specified interface.
		/// Plugins are cached and same instances are provided on subsequent calls.
		/// </summary>
		/// <typeparam name="TInterface">plugin must implement provided interface</typeparam>
		/// <param name="provider">extensibility service</param>
		/// <returns>resolved plugins</returns>
		public static IEnumerable<TInterface> ResolvePlugins<TInterface>(this IExtensibilityProvider provider)
		{
			Contract.Requires(provider != null);

			object cache;
			if (!CachedPlugins.TryGetValue(typeof(TInterface), out cache))
			{
				var result =
					provider.FindPlugins<TInterface>()
					.Aggregate<Type, List<TInterface>>(
					new List<TInterface>(),
					(list, it) =>
					{
						list.Add((TInterface)Activator.CreateInstance(it));
						return list;
					});
				cache = result;
				CachedPlugins.TryAdd(typeof(TInterface), cache);
			}
			return (List<TInterface>)cache;
		}
		/// <summary>
		/// Find all implementations for concepts for specified interface.
		/// Result is collection of found concepts and their implementations.
		/// Implementations are topologically sorted based on their dependencies.
		/// </summary>
		/// <typeparam name="TImplementation">plugin must implement provided interface</typeparam>
		/// <param name="provider">extensibility service</param>
		/// <returns>map of concepts and their implementations</returns>
		public static Dictionary<Type, List<Type>> FindImplementations<TImplementation>(this IExtensibilityProvider provider)
		{
			Contract.Requires(provider != null);

			return provider.FindImplementations<TImplementation>((t, i) => true);
		}
	}

	/// <summary>
	/// MEF metadata.
	/// For specifying dependencies and additional plugin info.
	/// </summary>
	public static class Metadata
	{
		/// <summary>
		/// Since MEF doesn't know actual type, repeat the target class type using this attribute.
		/// This should be used when class has dependencies so they can be resolved from container.
		/// </summary>
		public const string ClassType = "ClassType";
		/// <summary>
		/// When MEF plugin should be used instead of some other plugin.
		/// </summary>
		public const string InsteadOf = "InsteadOf";
		/// <summary>
		/// Specify concept type which plugin implements.
		/// </summary>
		public const string Implements = "Implements";
		/// <summary>
		/// Define plugin dependency. This plugin should be used after specified plugin.
		/// </summary>
		public const string After = "After";
		/// <summary>
		/// Define plugin dependency. This plugin should be used before specified plugin.
		/// </summary>
		public const string Before = "Before";
		/// <summary>
		/// For aspects on concepts. Define which concept this plugin extends.
		/// </summary>
		public const string Extends = "Extends";
	}
}
