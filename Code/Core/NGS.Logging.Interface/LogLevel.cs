namespace NGS.Logging
{
	/// <summary>
	/// Log level.
	/// Debug should be used to log detailed information useful for debugging.
	/// Trace is useful for flow, so event paths can be reconstructed.
	/// Use Info to log standard events which should be logged.
	/// Error should be used to log unexpected exceptions in the system.
	/// Use fatal to log errors on which somebody should react immediately.
	/// </summary>
	public enum LogLevel
	{
		Debug = 0,
		Trace = 1,
		Info = 2,
		Error = 4,
		Fatal = 8
	}
}
