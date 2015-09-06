using System;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Runtime.Caching;

namespace Revenj.DomainPatterns
{
	public class LookupCache<TValue> : IDataCache<TValue>, IDisposable
		where TValue : class, IIdentifiable
	{
		private static readonly MemoryCache Cache;
		private static readonly CacheItemPolicy CachePolicy;
		private static readonly string Suffix;
		private static readonly string Name = typeof(TValue).FullName;

		static LookupCache()
		{
			if (ConfigurationManager.AppSettings["Revenj.MemoryCache.UseDefault"] == "false")
			{
				Cache = new MemoryCache(typeof(TValue).FullName);
				Suffix = string.Empty;
			}
			else
			{
				Cache = MemoryCache.Default;
				Suffix = ":" + typeof(TValue).FullName;
			}
			CachePolicy = new CacheItemPolicy
			{
				AbsoluteExpiration = MemoryCache.InfiniteAbsoluteExpiration,
				SlidingExpiration = MemoryCache.NoSlidingExpiration
			};
		}

		private readonly IRepository<TValue> Repository;
		private readonly IDisposable Subscription;

		public LookupCache(
			IRepository<TValue> repository,
			IDataChangeNotification notifications)
		{
			Contract.Requires(repository != null);
			Contract.Requires(notifications != null);

			this.Repository = repository;
			Subscription = notifications.Notifications.Subscribe(CheckInvalidate);
		}

		private void CheckInvalidate(NotifyInfo info)
		{
			if (info.Name == Name && info.Operation != NotifyInfo.OperationEnum.Insert)
			{
				foreach (var uri in info.URI)
					Cache.Remove(uri + Suffix);
			}
		}

		public void Invalidate(IEnumerable<string> uris)
		{
			if (uris != null)
			{
				foreach (var uri in uris)
					Cache.Remove(uri + Suffix);
			}
		}

		public TValue Find(string uri)
		{
			TValue item;
			item = Cache.Get(uri + Suffix) as TValue;
			if (item == null)
			{
				var found = Repository.Find(uri);
				if (found != null)
					Cache.Set(uri + Suffix, found, CachePolicy);
				return found;
			}
			return item;
		}

		public TValue[] Find(IEnumerable<string> uris)
		{
			var list = (uris ?? new string[0]).Where(it => it != null).ToList();
			TValue item;
			if (list.Count == 1)
			{
				item = Cache.Get(list[0] + Suffix) as TValue;
				if (item == null)
				{
					var found = Repository.Find(list);
					if (found.Length == 1)
						Cache.Set(list[0] + Suffix, found[0], CachePolicy);
					return found;
				}
				return new[] { item };
			}
			var cached = Cache.GetValues(list);
			if (list.Count != cached.Count)
			{
				var missing = Repository.Find(list.Except(cached.Keys)).ToDictionary(it => it.URI, it => it);
				foreach (var kv in missing)
					Cache.Set(kv.Key + Suffix, kv.Value, CachePolicy);
				var result = new TValue[cached.Count + missing.Count];
				object tmp;
				int cur = 0;
				for (int i = 0; i < list.Count; i++)
				{
					var uri = list[i];
					if (cached.TryGetValue(uri, out tmp))
						result[cur++] = tmp as TValue;
					else if (missing.TryGetValue(uri, out item))
						result[cur++] = item;
				}
				return result;
			}
			else
			{
				var result = new TValue[list.Count];
				cached.Values.CopyTo(result, 0);
				return result;
			}
		}

		public void Dispose()
		{
			Subscription.Dispose();
		}
	}
}
