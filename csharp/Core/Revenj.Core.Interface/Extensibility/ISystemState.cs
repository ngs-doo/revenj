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
		/// <summary>
		/// Register for events
		/// </summary>
		event Action<SystemEvent> Change;
		/// <summary>
		/// Notify system about an system event
		/// </summary>
		/// <param name="value">event data</param>
		void Notify(SystemEvent value);
	}
	/// <summary>
	/// Notification information about system event, such as migration
	/// </summary>
	public class SystemEvent
	{
		/// <summary>
		/// System event info
		/// </summary>
		/// <param name="id">event id</param>
		/// <param name="detail">details</param>
		public SystemEvent(string id, string detail)
		{
			this.ID = id;
			this.Detail = detail;
		}

		/// <summary>
		/// Event ID (eg. migration)
		/// </summary>
		public string ID { get; private set; }
		/// <summary>
		/// Event Details (eg. new)
		/// </summary>
		public string Detail { get; private set; }
	}
}
