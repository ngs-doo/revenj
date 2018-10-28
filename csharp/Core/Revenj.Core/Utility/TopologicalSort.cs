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
			var graph = nodes.ToDictionary(it => it, _ => new HashSet<T>());
			var inverse = nodes.ToDictionary(it => it, _ => new HashSet<T>());
			foreach (var dep in dependencies)
			{
				graph[dep.Key].Add(dep.Value);
				inverse[dep.Value].Add(dep.Key);
			}

			return TopologicalSort(graph, inverse);
		}

		private static List<T> TopologicalSort<T>(Dictionary<T, HashSet<T>> graph, Dictionary<T, HashSet<T>> inverse)
		{
			var result = new List<T>(graph.Count);
			int position = 0;

			while (graph.Count > 0)
			{
				foreach (var kv in graph)
					if (kv.Value.Count == 0)
						result.Add(kv.Key);

				for (int i = position; i < result.Count; i++)
				{
					var node = result[i];
					graph.Remove(node);
					foreach (var dep in inverse[node])
						graph[dep].Remove(node);
				}

				if (result.Count == position)
					throw new ArgumentException("Provided graph has circular dependency. Topological sort can't be performed on graph with circular dependency.");
				position = result.Count;
			}

			return result;
		}
	}
}
