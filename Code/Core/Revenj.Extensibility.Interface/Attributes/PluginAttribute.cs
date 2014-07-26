using System;

namespace Revenj.Extensibility
{
	/// <summary>
	/// Attribute for registering plugins into the container.
	/// When plugins are not registered using standard methods,
	/// this alternative method can be used for registering all plugins implementing this interface.
	/// </summary>
	[AttributeUsage(AttributeTargets.Interface, AllowMultiple = false, Inherited = true)]
	public class PluginAttribute : Attribute
	{
		/// <summary>
		/// Registered plugins scope
		/// </summary>
		public readonly InstanceScope Scope;

		/// <summary>
		/// Plugins will have transient scope by default
		/// </summary>
		public PluginAttribute()
			: this(InstanceScope.Transient) { }

		/// <summary>
		/// Register plugins using specified scope
		/// </summary>
		/// <param name="scope">instance scope</param>
		public PluginAttribute(InstanceScope scope)
		{
			this.Scope = scope;
		}
	}
}
