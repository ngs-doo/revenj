using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

namespace NGS.DomainPatterns
{
	/// <summary>
	/// Data cache service. 
	/// Aggregates resolved from this service will be cached in its scope.
	/// Invalidate uris on aggregate change.
	/// </summary>
	/// <typeparam name="TValue">aggregate root type</typeparam>
	public interface IDataCache<TValue> : IRepository<TValue>
		where TValue : IAggregateRoot
	{
		/// <summary>
		/// Changed aggregate should be removed from cache.
		/// </summary>
		/// <param name="uris">changed uris</param>
		void Invalidate(IEnumerable<string> uris);
	}
	/// <summary>
	/// Cachable domain objects.
	/// Dependencies can be tracked through relationship,
	/// so invalidation can be detected.
	/// </summary>
	public interface ICacheable
	{
		/// <summary>
		/// Get relationship for this domain object.
		/// </summary>
		/// <returns>collection of referenced types and uris</returns>
		Dictionary<Type, IEnumerable<string>> GetRelationships();
	}
	/// <summary>
	/// Utility for data cache
	/// </summary>
	public static class DataCacheHelper
	{
		/// <summary>
		/// Invalidate single aggregate root.
		/// </summary>
		/// <typeparam name="TValue">aggregate root type</typeparam>
		/// <param name="cache">cache service</param>
		/// <param name="uri">uri to invalidate</param>
		public static void Invalidate<TValue>(this IDataCache<TValue> cache, string uri)
			where TValue : IAggregateRoot
		{
			Contract.Requires(cache != null);
			Contract.Requires(uri != null);

			if (uri != null)
				cache.Invalidate(new[] { uri });
		}
		/// <summary>
		/// Build dependency collection information.
		/// Collect invalid types and uris for provided domain objects.
		/// </summary>
		/// <typeparam name="TValue">domain object type</typeparam>
		/// <param name="values">domain objects</param>
		/// <returns>dependecy collection information</returns>
		public static Dictionary<Type, HashSet<string>> GetInvalidValues<TValue>(this IEnumerable<TValue> values)
			where TValue : ICacheable
		{
			var result = new Dictionary<Type, HashSet<string>>();

			foreach (var item in values ?? new TValue[0])
			{
				var rels = item.GetRelationships();
				foreach (var kv in rels)
				{
					HashSet<string> set;
					if (!result.TryGetValue(kv.Key, out set))
						result[kv.Key] = set = new HashSet<string>();
					foreach (var uri in kv.Value)
						set.Add(uri);
				}
			}

			return result;
		}
	}
}
