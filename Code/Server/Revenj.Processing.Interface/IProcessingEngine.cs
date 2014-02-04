namespace Revenj.Processing
{
	/// <summary>
	/// Service for processing server commands.
	/// Server commands are loaded from description information.
	/// Commands are executed and result is transformed to requested output type.
	/// </summary>
	public interface IProcessingEngine
	{
		/// <summary>
		/// Execute server commands.
		/// Command arguments are defined with input format type.
		/// Result is expected in specified output format type.
		/// Commands are executed in a single transaction.
		/// </summary>
		/// <typeparam name="TInput">command argument type</typeparam>
		/// <typeparam name="TOutput">executed result type</typeparam>
		/// <param name="commands">commands description</param>
		/// <returns>aggregated execution result</returns>
		IProcessingResult<TOutput> Execute<TInput, TOutput>(IServerCommandDescription<TInput>[] commands);
	}
}
