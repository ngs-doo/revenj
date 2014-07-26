using System;

namespace Revenj.Extensibility
{
	/// <summary>
	/// Helper interface for plugins.
	/// Plugins can be resolved by full or short name
	/// </summary>
	/// <typeparam name="TTarget">plugin type</typeparam>
	public interface IPluginRepository<TTarget>
	{
		/// <summary>
		/// Find plugin by it's name.
		/// </summary>
		/// <param name="name">plugin name</param>
		/// <returns>found plugin</returns>
		Type Find(string name);
	}
}
