using System;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Change tracking on an object.
	/// When object implements change tracking it will maintain original 
	/// version of an object at the time of reconstruction.
	/// </summary>
	/// <typeparam name="T">object type</typeparam>
	public interface IChangeTracking<T> : IEquatable<T>
	{
		/// <summary>
		/// Change tracking state for this instance
		/// </summary>
		bool TrackChanges { get; set; }
		/// <summary>
		/// Get original value of a tracked object.
		/// Original value is initial value after reconstruction.
		/// </summary>
		/// <returns>original value</returns>
		T GetOriginalValue();
	}
	/// <summary>
	/// Utility for change tracking
	/// </summary>
	public static class ChangeTrackingHelper
	{
		/// <summary>
		/// Is state of the object changed.
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="tracking">object which implements change tracking</param>
		/// <returns>is object changed</returns>
		public static bool IsChanged<T>(this IChangeTracking<T> tracking)
		{
			var original = tracking.GetOriginalValue();
			return original == null
				|| tracking.Equals(original);
		}
	}
}
