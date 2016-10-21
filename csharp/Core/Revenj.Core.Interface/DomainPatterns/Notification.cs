using System;
using System.Collections.Generic;

namespace Revenj.DomainPatterns
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
		/// Source of notification
		/// </summary>
		public readonly SourceEnum Source;
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
		/// Where did the notification originate from
		/// </summary>
		public enum SourceEnum
		{
			/// <summary>
			/// Notification originated from the database
			/// </summary>
			Database,
			/// <summary>
			/// Notification originated from the local server
			/// </summary>
			Local
		};
		/// <summary>
		/// Create notification information
		/// </summary>
		/// <param name="name">domain object name</param>
		/// <param name="operation">operation type</param>
		/// <param name="source">notification source</param>
		/// <param name="uri">identifiers</param>
		public NotifyInfo(string name, OperationEnum operation, SourceEnum source, string[] uri)
		{
			this.Name = name;
			this.Operation = operation;
			this.Source = source;
			this.URI = uri;
		}
		/// <summary>
		/// Create notification information originating from local server
		/// </summary>
		/// <param name="name">domain object name</param>
		/// <param name="operation">operation type</param>
		/// <param name="source">notification source</param>
		/// <param name="uri">identifiers</param>
		public NotifyInfo(string name, OperationEnum operation, string[] uri)
			: this(name, operation, SourceEnum.Local, uri) { }
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
		/// <returns>pair of domain object identifiers, with lazily reified instance</returns>
		IObservable<KeyValuePair<string[], Lazy<T[]>>> Track<T>();
	}
}
