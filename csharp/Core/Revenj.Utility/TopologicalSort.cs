using System;
using System.Collections.Generic;
using System.Linq;

namespace Revenj.Utility
{
	/// <summary>
	/// Sorting algorithms
	/// </summary>
	public static partial class Sorting
	{
		/// <summary>
		/// Topological ordering of directed graph.
		/// </summary>
		/// <typeparam name="T">Node type</typeparam>
		/// <param name="nodes">Graph nodes</param>
		/// <param name="dependencies">Node dependencies</param>
		/// <returns>Sorted nodes</returns>
		public static IEnumerable<T> TopologicalSort<T>(IEnumerable<T> nodes, IEnumerable<KeyValuePair<T, T>> dependencies)
		{
			var dictionary = nodes.ToDictionary(it => it, _ => new HashSet<T>());
			foreach (var dep in dependencies)
				dictionary[dep.Key].Add(dep.Value);

			return TopologicalSort(dictionary);
		}

		private static List<T> TopologicalSort<T>(IDictionary<T, HashSet<T>> graph)
		{
			var result = new List<T>();

			while (graph.Count > 0)
			{
				var emptyNodes =
					(from it in graph
					 where it.Value.Count == 0
					 select it.Key).ToList();

				foreach (var node in emptyNodes)
				{
					graph.Remove(node);
					foreach (var list in graph.Values)
						list.Remove(node);
				}

				result.AddRange(emptyNodes);
				if (emptyNodes.Count == 0)
					throw new ArgumentException("Provided graph has circular dependency. Topological sort can't be performed on graph with circular dependency.");
			}

			return result;
		}
	}
}
