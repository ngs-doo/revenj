using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;

namespace Revenj.DomainPatterns
{
	public class EagerCache<TValue> : IDataSourceCache<TValue>
		where TValue : class, IIdentifiable
	{
		private static readonly string Name = typeof(TValue).FullName;
		private readonly IRepository<TValue> Lookup;
		private readonly IQueryableRepository<TValue> Repository;
		private readonly IDisposable Subscription;
		private ConcurrentDictionary<string, TValue> Data;

		public EagerCache(
			IRepository<TValue> lookup,
			IQueryableRepository<TValue> repository,
			IDataChangeNotification notifications)
		{
			Contract.Requires(lookup != null);
			Contract.Requires(repository != null);
			Contract.Requires(notifications != null);

			this.Lookup = lookup;
			this.Repository = repository;

			Subscription = notifications.Notifications.Subscribe(Synchronize);
			var found = repository.Search();
			Data = new ConcurrentDictionary<string, TValue>(1, found.Length);
			foreach (var f in found)
				Data.TryAdd(f.URI, f);
		}

		private void Synchronize(NotifyInfo info)
		{
			if (info.Name == Name)
			{
				switch (info.Operation)
				{
					case NotifyInfo.OperationEnum.Insert:
					case NotifyInfo.OperationEnum.Change:
						var found = Lookup.Find(info.URI);
						foreach (var f in found)
							Data.TryAdd(f.URI, f);
						break;
					case NotifyInfo.OperationEnum.Delete:
						TValue value;
						foreach (var u in info.URI)
							Data.TryRemove(u, out value);
						break;
					case NotifyInfo.OperationEnum.Update:
						var remaining = Lookup.Find(info.URI);
						foreach (var r in remaining)
							Data.AddOrUpdate(r.URI, r, (_, __) => r);
						break;
				}
			}
		}

		public void Invalidate(IEnumerable<string> uris)
		{
			if (uris != null)
			{
				var list = (uris ?? new string[0]).Where(it => it != null).ToList();
				var found = Lookup.Find(list);
				foreach (var r in found)
					Data.AddOrUpdate(r.URI, r, (_, __) => r);
				TValue value;
				foreach (var u in list.Except(found.Select(it => it.URI)))
					Data.TryRemove(u, out value);
			}
		}

		public TValue Find(string uri)
		{
			if (uri == null)
				return null;
			TValue item;
			Data.TryGetValue(uri, out item);
			return item;
		}

		public TValue[] Find(IEnumerable<string> uris)
		{
			if (uris != null)
			{
				var result = new List<TValue>(uris.Count());
				TValue item;
				foreach (var u in uris)
					if (u != null && Data.TryGetValue(u, out item))
						result.Add(item);
				return result.ToArray();
			}
			return new TValue[0];
		}

		public void InvalidateAll()
		{
			var found = Repository.Search();
			var data = new ConcurrentDictionary<string, TValue>(1, found.Length);
			foreach (var f in found)
				data.TryAdd(f.URI, f);
			var prev = Data;
			Data = data;
			prev.Clear();
		}

		public IQueryable<TValue> Query<TCondition>(ISpecification<TCondition> specification)
		{
			var queryable = Data.Values.AsQueryable();
			var specNative = specification as ISpecification<TValue>;
			if (specNative != null && specNative.IsSatisfied != null)
				return queryable.Where(specNative.IsSatisfied);
			if (specification != null && specification.IsSatisfied != null)
				return queryable.Cast<TCondition>().Where(specification.IsSatisfied).Cast<TValue>();
			return queryable;
		}

		public TValue[] Search<TCondition>(ISpecification<TCondition> specification, int? limit, int? offset)
		{
			IEnumerable<TValue> values = Data.Values;
			var specNative = specification as ISpecification<TValue>;
			if (specNative != null && specNative.IsSatisfied != null)
				values = values.Where(specNative.IsSatisfied.Compile());
			if (specification != null && specification.IsSatisfied != null)
				values = values.OfType<TCondition>().Where(specification.IsSatisfied.Compile()).Cast<TValue>();
			if (offset != null)
				values = values.Skip(offset.Value);
			if (limit != null)
				values = values.Take(limit.Value);
			return values.ToArray();
		}

		public long Count<TCondition>(ISpecification<TCondition> specification)
		{
			IEnumerable<TValue> values = Data.Values;
			var specNative = specification as ISpecification<TValue>;
			if (specNative != null && specNative.IsSatisfied != null)
				values = values.Where(specNative.IsSatisfied.Compile());
			if (specification != null && specification.IsSatisfied != null)
				values = values.OfType<TCondition>().Where(specification.IsSatisfied.Compile()).Cast<TValue>();
			return values.LongCount();
		}

		public bool Exists<TCondition>(ISpecification<TCondition> specification)
		{
			IEnumerable<TValue> values = Data.Values;
			var specNative = specification as ISpecification<TValue>;
			if (specNative != null && specNative.IsSatisfied != null)
				values = values.Where(specNative.IsSatisfied.Compile());
			if (specification != null && specification.IsSatisfied != null)
				values = values.OfType<TCondition>().Where(specification.IsSatisfied.Compile()).Cast<TValue>();
			return values.Any();
		}

		public void Dispose()
		{
			Subscription.Dispose();
		}
	}
}
