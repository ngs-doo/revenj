using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;

namespace Revenj.DomainPatterns
{
	public class WeakCache<TValue> : IDataCache<TValue>
		where TValue : class, IAggregateRoot
	{
		private readonly Lazy<IRepository<TValue>> Repository;
		private readonly WeakReference Cache = new WeakReference(null);

		public WeakCache(Lazy<IRepository<TValue>> repository)
		{
			Contract.Requires(repository != null);

			this.Repository = repository;
		}

		public void Invalidate(IEnumerable<string> uris)
		{
			var dict = Cache.Target as ConcurrentDictionary<string, TValue>;
			if (dict != null && uris != null)
			{
				TValue v;
				foreach (var uri in uris)
					if (uri != null)
						dict.TryRemove(uri, out v);
			}
		}

		public TValue Find(string uri)
		{
			if (uri == null)
				return null;
			var dict = Cache.Target as ConcurrentDictionary<string, TValue>;
			if (dict == null)
			{
				dict = new ConcurrentDictionary<string, TValue>(1, 17);
				Cache.Target = dict;
			}
			else
			{
				TValue item;
				if (dict.TryGetValue(uri, out item))
					return item;
			}
			var found = Repository.Value.Find(uri);
			if (found != null)
				dict.TryAdd(uri, found);
			return found;
		}

		public TValue[] Find(IEnumerable<string> uris)
		{
			var list = (uris ?? new string[0]).Where(it => it != null).ToList();
			if (list.Count == 1)
				return FindOne(list);
			var result = new List<TValue>();
			var dict = Cache.Target as ConcurrentDictionary<string, TValue>;
			if (dict == null)
			{
				dict = new ConcurrentDictionary<string, TValue>(1, 17);
				Cache.Target = dict;
			}
			else
			{
				foreach (var uri in list)
				{
					TValue item;
					if (dict.TryGetValue(uri, out item))
						result.Add(item);
				}
			}
			if (list.Count != result.Count)
			{
				var values = Repository.Value.Find(list.Except(result.Select(it => it.URI)));
				foreach (var item in values)
				{
					dict.TryAdd(item.URI, item);
					result.Add(item);
				}
			}

			return result.OrderBy(it => list.IndexOf(it.URI)).ToArray();
		}

		private TValue[] FindOne(List<string> uri)
		{
			var dict = Cache.Target as ConcurrentDictionary<string, TValue>;
			if (dict == null)
			{
				dict = new ConcurrentDictionary<string, TValue>(1, 17);
				Cache.Target = dict;
			}
			else
			{
				TValue item;
				if (dict.TryGetValue(uri[0], out item))
					return new[] { item };
			}
			var found = Repository.Value.Find(uri);
			if (found.Length == 1)
				dict.TryAdd(uri[0], found[0]);
			return found;
		}
	}
}
