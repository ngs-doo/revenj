namespace Revenj.Logging
{
	/// <summary>
	/// Factory for creating logger.
	/// </summary>
	public interface ILogFactory
	{
		/// <summary>
		/// Create logger using provided name.
		/// Logs will be attributed with provided name, so they
		/// can be differentiated.
		/// </summary>
		/// <param name="name">name to differentiate logs</param>
		/// <returns>logger service for logging</returns>
		ILogger Create(string name);
	}
}
