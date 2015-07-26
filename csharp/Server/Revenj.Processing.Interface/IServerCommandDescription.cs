using System;

namespace Revenj.Processing
{
	/// <summary>
	/// Server command description in specified format.
	/// </summary>
	/// <typeparam name="TFormat">input argument format</typeparam>
	public interface IServerCommandDescription<TFormat>
	{
		/// <summary>
		/// Request id used for pairing result with request.
		/// </summary>
		string RequestID { get; }
		/// <summary>
		/// Resolved command type from command name.
		/// </summary>
		Type CommandType { get; }
		/// <summary>
		/// Command argument.
		/// </summary>
		TFormat Data { get; set; }
	}
}
