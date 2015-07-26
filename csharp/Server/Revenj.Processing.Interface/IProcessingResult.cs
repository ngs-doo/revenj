using System.Collections.Generic;
using System.Net;

namespace Revenj.Processing
{
	/// <summary>
	/// Commands execution result.
	/// Aggregated result of command executions.
	/// </summary>
	/// <typeparam name="TFormat">result format</typeparam>
	public interface IProcessingResult<TFormat>
	{
		/// <summary>
		/// Global result message.
		/// </summary>
		string Message { get; }
		/// <summary>
		/// Global result status.
		/// </summary>
		HttpStatusCode Status { get; }
		/// <summary>
		/// Specific results for each commands.
		/// </summary>
		IEnumerable<ICommandResultDescription<TFormat>> ExecutedCommandResults { get; }
		/// <summary>
		/// Total elapsed milliseconds.
		/// </summary>
		long Duration { get; }
	}
}
