using System;
using System.Diagnostics.Contracts;

namespace NGS.DomainPatterns
{
	/// <summary>
	/// Access to domain model. 
	/// Domain model can be dynamic, so access to them is available through this API.
	/// </summary>
	[ContractClass(typeof(DomainModelContract))]
	public interface IDomainModel
	{
		/// <summary>
		/// Find domain object type by its name.
		/// If domain object is not found, null will be returned.
		/// </summary>
		/// <param name="name">domain object name</param>
		/// <returns>found domain object type</returns>
		Type Find(string name);
	}

	[ContractClassFor(typeof(IDomainModel))]
	internal sealed class DomainModelContract : IDomainModel
	{
		public Type Find(string name)
		{
			Contract.Requires(!string.IsNullOrWhiteSpace(name));

			return null;
		}
	}
}
