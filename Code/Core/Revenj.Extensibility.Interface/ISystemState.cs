using System;

namespace Revenj.Extensibility
{
	/// <summary>
	/// Services which depend on boot state should use this to check system state.
	/// </summary>
	public interface ISystemState
	{
		/// <summary>
		/// Is system still booting
		/// </summary>
		bool IsBooting { get; }
		/// <summary>
		/// Is system ready for use
		/// </summary>
		bool IsReady { get; }
		/// <summary>
		/// Event will fire when system is ready for use
		/// </summary>
		event Action<IObjectFactory> Ready;
	}
}
