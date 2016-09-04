using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

namespace Revenj.Extensibility
{
	/// <summary>
	/// Service for creating mixins.
	/// </summary>
	[ContractClass(typeof(MixinProviderContract))]
	public interface IMixinProvider
	{
		/// <summary>
		/// Create mixin instance for target type using provided constructor arguments and combining provided instances.
		/// </summary>
		/// <param name="mixinType">target class type</param>
		/// <param name="args">constructor arguments</param>
		/// <param name="implementations">additional mixins</param>
		/// <returns>object instance with merged mixins</returns>
		object Create(Type mixinType, object[] args, IEnumerable<object> implementations);
	}

	[ContractClassFor(typeof(IMixinProvider))]
	internal sealed class MixinProviderContract : IMixinProvider
	{
		public object Create(Type mixinType, object[] args, IEnumerable<object> implementations)
		{
			Contract.Requires(mixinType != null);
			Contract.Ensures(Contract.Result<object>() != null);

			return null;
		}
	}

	/// <summary>
	/// Helper method for creating mixins
	/// </summary>
	public static class MixinProviderExtension
	{
		/// <summary>
		/// Create mixin for target class using provided instances and constructor arguments.
		/// </summary>
		/// <typeparam name="TMixin">target class</typeparam>
		/// <param name="provider">mixin service</param>
		/// <param name="implementations">additional mixins</param>
		/// <param name="args">constructor arguments</param>
		/// <returns>object instance with merged mixins</returns>
		public static TMixin Create<TMixin>(this IMixinProvider provider, IEnumerable<object> implementations, params object[] args)
		{
			Contract.Requires(provider != null);

			return (TMixin)provider.Create(typeof(TMixin), args, implementations);
		}
		/// <summary>
		/// Create mixin for target class using constructor arguments.
		/// Usefull for abstract classes.
		/// </summary>
		/// <typeparam name="TMixin">target instance</typeparam>
		/// <param name="provider">mixin service</param>
		/// <param name="type">target class</param>
		/// <param name="args">constructor arguments</param>
		/// <returns>object instance</returns>
		public static TMixin Create<TMixin>(this IMixinProvider provider, Type type, object[] args)
		{
			Contract.Requires(provider != null);

			return (TMixin)provider.Create(type, args, new object[0]);
		}
	}
}
