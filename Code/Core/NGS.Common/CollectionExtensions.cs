using System.Collections.Generic;
using System.Linq;

namespace NGS
{
	/// <summary>
	/// Helper methods for working with collections
	/// </summary>
	public static class CollectionExtensions
	{
		/// <summary>
		/// Add UnionAll so it is visible in Intellisense since often Concat
		/// should be used instead of Union.
		/// Union will remove duplicate elements, while UnionAll will not.
		/// </summary>
		/// <typeparam name="T">collection type</typeparam>
		/// <param name="first">starting collection</param>
		/// <param name="second">collection to be appended</param>
		/// <returns>concatenated collection</returns>
		public static IEnumerable<T> UnionAll<T>(this IEnumerable<T> first, IEnumerable<T> second)
		{
			return first.Concat(second);
		}
	}
}
