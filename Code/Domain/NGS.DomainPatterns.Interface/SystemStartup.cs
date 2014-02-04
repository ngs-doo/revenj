namespace NGS.DomainPatterns
{
	/// <summary>
	/// Service which will be called during system startup.
	/// </summary>
	public interface ISystemStartup
	{
		/// <summary>
		/// Configure system behavior.
		/// </summary>
		/// <param name="locator">dynamic locator</param>
		void Configure(IServiceLocator locator);
	}
}
