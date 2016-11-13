using System;

namespace Revenj.DomainPatterns
{
	public interface IChangeTracking<T> : IEquatable<T>
	{
		T GetOriginalValue();
	}

	public static class ChangeTrackingHelper
	{
		public static bool IsChanged<T>(this IChangeTracking<T> tracking)
		{
			var original = tracking.GetOriginalValue();
			return original == null
				|| tracking.Equals(original);
		}
	}
}
