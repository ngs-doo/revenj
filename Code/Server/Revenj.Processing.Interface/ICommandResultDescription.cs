namespace Revenj.Processing
{
	/// <summary>
	/// Executed command result paired with request id.
	/// </summary>
	/// <typeparam name="TFormat">result format</typeparam>
	public interface ICommandResultDescription<TFormat>
	{
		/// <summary>
		/// Request id from command description
		/// </summary>
		string RequestID { get; }
		/// <summary>
		/// Execution result from server command
		/// </summary>
		ICommandResult<TFormat> Result { get; }
	}
}
