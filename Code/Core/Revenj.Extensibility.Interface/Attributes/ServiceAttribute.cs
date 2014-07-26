using System;

namespace Revenj.Extensibility
{
	/// <summary>
	/// Attribute for registering class into the container.
	/// When some service is not registered using standard methods,
	/// this alternative method can be used for registering service where it was declared.
	/// </summary>
	[AttributeUsage(AttributeTargets.Class, AllowMultiple = false, Inherited = true)]
	public class ServiceAttribute : Attribute
	{
		/// <summary>
		/// Registered service scope
		/// </summary>
		public readonly InstanceScope Scope;

		/// <summary>
		/// Service will have transient scope by default
		/// </summary>
		public ServiceAttribute()
			: this(InstanceScope.Transient) { }

		/// <summary>
		/// Register service using specified scope
		/// </summary>
		/// <param name="scope">instance scope</param>
		public ServiceAttribute(InstanceScope scope)
		{
			this.Scope = scope;
		}
	}
}
