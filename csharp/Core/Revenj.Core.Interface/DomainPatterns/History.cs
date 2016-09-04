using System;
using System.Collections.Generic;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Domain object which provides access to it's history
	/// </summary>
	public interface IObjectHistory : IIdentifiable { }

	/// <summary>
	/// Collection of aggregate root snapshots.
	/// When aggregate root history is enabled, on each persist, 
	/// snapshot will be saved too.
	/// </summary>
	/// <typeparam name="TRoot"></typeparam>
	public interface IHistory<out TRoot> : IIdentifiable
		where TRoot : IObjectHistory
	{
		/// <summary>
		/// Collection of snapshots for this aggregate root
		/// </summary>
		IEnumerable<ISnapshot<TRoot>> Snapshots { get; }
	}
	/// <summary>
	/// Point in time version of an aggregate root.
	/// </summary>
	/// <typeparam name="TRoot">aggregate root type</typeparam>
	public interface ISnapshot<out TRoot> : IIdentifiable, INestedValue<TRoot>
		where TRoot : IObjectHistory
	{
		/// <summary>
		/// When was this snapshot created
		/// </summary>
		DateTime At { get; }
		/// <summary>
		/// Type of action by which this snapshot was created
		/// </summary>
		string Action { get; }
	}
}
