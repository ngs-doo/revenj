using System;
using System.Collections.Generic;

namespace NGS.DomainPatterns
{
	/// <summary>
	/// Notification information.
	/// </summary>
	public class NotifyInfo
	{
		/// <summary>
		/// Domain object name
		/// </summary>
		public readonly string Name;
		/// <summary>
		/// Type of operation
		/// </summary>
		public readonly OperationEnum Operation;
		/// <summary>
		/// Object identifiers
		/// </summary>
		public readonly string[] URI;
		/// <summary>
		/// Operation types
		/// </summary>
		public enum OperationEnum
		{
			/// <summary>
			/// Aggregate root or event insert
			/// </summary>
			Insert,
			/// <summary>
			/// Aggregate root was changed, but URI remained the same
			/// </summary>
			Update,
			/// <summary>
			/// Aggregate root was changed and URI is changed also
			/// </summary>
			Change,
			/// <summary>
			/// Aggregate root was deleted
			/// </summary>
			Delete
			//TODO: mark and submit for events!?
		};
		/// <summary>
		/// Create notification information
		/// </summary>
		/// <param name="name">domain object name</param>
		/// <param name="operation">operation type</param>
		/// <param name="uri">identifiers</param>
		public NotifyInfo(string name, OperationEnum operation, string[] uri)
		{
			this.Name = name;
			this.Operation = operation;
			this.URI = uri;
		}
	}
	/// <summary>
	/// Change notification service.
	/// When aggregate root are persisted or events are submitted,
	/// notification will be available through this service.
	/// </summary>
	public interface IDataChangeNotification
	{
		/// <summary>
		/// Observe notifications
		/// </summary>
		IObservable<NotifyInfo> Notifications { get; }
		//TODO specialized signature!?
		/// <summary>
		/// Specify interest in only subset of notifications.
		/// </summary>
		/// <typeparam name="T">domain object type</typeparam>
		/// <returns>pair of domain object identifiers, with lazily reified instanced</returns>
		IObservable<KeyValuePair<string[], Lazy<T[]>>> Track<T>()
			where T : IIdentifiable;
	}
}
