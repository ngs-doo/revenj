namespace Revenj.Processing
{
	/// <summary>
	/// Simpler interface for server command.
	/// Instead of providing serialization object to command,
	/// prepare and convert arguments and result outside of command.
	/// </summary>
	/// <typeparam name="TInput">input argument type</typeparam>
	/// <typeparam name="TOutput">output result type</typeparam>
	public interface IServerService<TInput, TOutput>
	{
		/// <summary>
		/// Execute service with provided argument.
		/// </summary>
		/// <param name="data">service argument</param>
		/// <returns>service result</returns>
		TOutput Execute(TInput data);
	}
}
