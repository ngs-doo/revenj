using System;
using System.Diagnostics.Contracts;

namespace Revenj.Extensibility
{
	/// <summary>
	/// Type resolver service.
	/// Domain types and other system types can be resolved from this service.
	/// This enables access to dynamic types which are not available at compile time.
	/// </summary>
	[ContractClass(typeof(TypeResolverContract))]
	public interface ITypeResolver
	{
		/// <summary>
		/// Try to find type by its name.
		/// If type is not found null will be returned.
		/// </summary>
		/// <param name="name">type name</param>
		/// <returns>found type</returns>
		Type Resolve(string name);
	}

	internal sealed class TypeResolverContract : ITypeResolver
	{
		public Type Resolve(string name)
		{
			Contract.Requires(!string.IsNullOrWhiteSpace(name));
			return null;
		}
	}
}
