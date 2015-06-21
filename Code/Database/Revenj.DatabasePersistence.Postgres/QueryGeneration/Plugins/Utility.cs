using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace Revenj.DatabasePersistence.Postgres.Plugins
{
	internal static class Utility
	{
		public static bool IsSupportedCollectionType(this Type type)
		{
			if (type.IsArray)
				return true;
			if (!type.IsGenericType)
				return false;
			var genType = type.GetGenericTypeDefinition();
			return genType == typeof(List<>)
				|| genType == typeof(HashSet<>)
				|| genType == typeof(ReadOnlyCollection<>)
				|| genType == typeof(Queue<>)
				|| genType == typeof(Stack<>)
				|| genType == typeof(LinkedList<>);
		}
	}
}
