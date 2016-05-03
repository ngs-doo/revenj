using System.Security.Principal;
using System.Threading;

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
		/// <param name="principal">security context</param>
		/// <returns>aggregated execution result</returns>
		IProcessingResult<TOutput> Execute<TInput, TOutput>(IServerCommandDescription<TInput>[] commands, IPrincipal principal);
	}
	/// <summary>
	/// Utility class for processing engine
	/// </summary>
	public static class ProcessingEngineHelper
	{
		/// <summary>
		/// Execute API with commands through params arguments. Thread bounded principal will be passed.
		/// Command arguments are defined with input format type.
		/// Result is expected in specified output format type.
		/// Commands are executed in a single transaction.
		/// </summary>
		/// <typeparam name="TInput">command argument type</typeparam>
		/// <typeparam name="TOutput">executed result type</typeparam>
		/// <param name="engine">processing engine</param>
		/// <param name="commands">commands description</param>
		/// <returns>aggregated execution result</returns>
		public static IProcessingResult<TOutput> Execute<TInput, TOutput>(
			this IProcessingEngine engine,
			params IServerCommandDescription<TInput>[] commands)
		{
			return engine.Execute<TInput, TOutput>(commands, Thread.CurrentPrincipal);
		}
	}
}
