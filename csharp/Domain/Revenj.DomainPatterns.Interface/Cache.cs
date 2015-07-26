using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Data cache service using uri lookup.
	/// Identifiable object resolved from this service will be cached.
	/// Invalidate uris on object change.
	/// </summary>
	/// <typeparam name="TValue">domain object type</typeparam>
	public interface IDataCache<TValue> : IRepository<TValue>
		where TValue : IIdentifiable
	{
		/// <summary>
		/// Changed objects should be removed from cache.
		/// </summary>
		/// <param name="uris">changed uris</param>
		void Invalidate(IEnumerable<string> uris);
	}
	/// <summary>
	/// Entire data source cache service.
	/// When entire class of objects is kept in memory, data source cache can be used.
	/// </summary>
	/// <typeparam name="TValue"></typeparam>
	public interface IDataSourceCache<TValue> : IDataCache<TValue>, IQueryableRepository<TValue>
		where TValue : IIdentifiable
	{
		/// <summary>
		/// Invalidate entire cache.
		/// </summary>
		void InvalidateAll();
	}
	/// <summary>
	/// Cacheable domain objects.
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
	/// Cache utility
	/// </summary>
	public static class CacheHelper
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
		/// <returns>dependency collection information</returns>
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
