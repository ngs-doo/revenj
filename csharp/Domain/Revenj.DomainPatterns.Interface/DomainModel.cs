using System;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Access to domain model. 
	/// Domain model can be dynamic, so access to them is available through this API.
	/// </summary>
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
}
