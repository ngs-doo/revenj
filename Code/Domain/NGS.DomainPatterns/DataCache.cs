using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;

namespace NGS.DomainPatterns
{
	public class DataCache<TValue> : IDataCache<TValue>, IDisposable
		where TValue : class, IAggregateRoot
	{
		private readonly IRepository<TValue> Repository;
		private static int CpuCount = Environment.ProcessorCount;
		private static int InitialCount = Environment.ProcessorCount < 17 ? 17 : Environment.ProcessorCount * 2 - 1;
		private readonly WeakReference Cache = new WeakReference(new ConcurrentDictionary<string, TValue>(CpuCount, InitialCount));
		private readonly IDisposable Subscription;

		public DataCache(
			IRepository<TValue> repository,
			IDataChangeNotification notifications)
		{
			Contract.Requires(repository != null);
			Contract.Requires(notifications != null);

			this.Repository = repository;
			Subscription = notifications.Track<TValue>().Subscribe(kv => Invalidate(kv.Key));
		}

		public void Invalidate(IEnumerable<string> uris)
		{
			var dict = Cache.Target as ConcurrentDictionary<string, TValue>;
			if (uris != null && dict != null)
			{
				TValue v;
				foreach (var uri in uris)
					if (uri != null)
						dict.TryRemove(uri, out v);
			}
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
				dict = new ConcurrentDictionary<string, TValue>(CpuCount, InitialCount);
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
				var values = Repository.Find(list.Except(result.Select(it => it.URI)));
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
				dict = new ConcurrentDictionary<string, TValue>(CpuCount, InitialCount);
				Cache.Target = dict;
			}
			else
			{
				TValue item;
				if (dict.TryGetValue(uri[0], out item))
					return new[] { item };
			}
			var found = Repository.Find(uri);
			if (found.Length == 1)
				dict.TryAdd(uri[0], found[0]);
			return found;
		}

		public void Dispose()
		{
			Subscription.Dispose();
		}
	}
}
