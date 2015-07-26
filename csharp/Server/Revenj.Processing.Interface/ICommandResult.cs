using System.Net;

namespace Revenj.Processing
{
	/// <summary>
	/// Server command execution result.
	/// Http status from 200 to 300 are considered valid responses.
	/// Invalid response will trigger transaction rollback.
	/// </summary>
	/// <typeparam name="TFormat">result type</typeparam>
	public interface ICommandResult<TFormat>
	{
		/// <summary>
		/// Result formatted in result type.
		/// </summary>
		TFormat Data { get; }
		/// <summary>
		/// Command response message
		/// </summary>
		string Message { get; }
		/// <summary>
		/// Command execution status.
		/// </summary>
		HttpStatusCode Status { get; }
	}
}
