using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;

namespace Revenj.Utility
{
	/// <summary>
	/// Utility for working with reflection
	/// </summary>
	public static class TypeUtility
	{
		private static readonly ConcurrentDictionary<Type, List<Type>> Cache = new ConcurrentDictionary<Type, List<Type>>(1, 127);
		/// <summary>
		/// Get type hierarchy for specified type.
		/// All interfaces and base types will be returned.
		/// Result will be cached.
		/// </summary>
		/// <param name="startType">specified type</param>
		/// <returns>all implemented interfaces and base types</returns>
		public static List<Type> GetTypeHierarchy(this Type startType)
		{
			List<Type> result;
			if (Cache.TryGetValue(startType, out result))
				return result;

			result = new List<Type>();
			do
			{
				result.Add(startType);
				result.AddRange(startType.GetInterfaces().Except(result));
				startType = startType.BaseType;
			} while (startType != typeof(object) && startType != null);
			result.Reverse();
			Cache.TryAdd(startType, result);
			return result;
		}
	}
}
