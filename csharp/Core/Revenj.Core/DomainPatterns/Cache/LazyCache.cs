using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;

namespace Revenj.DomainPatterns
{
	public class LazyCache<TValue> : IDataSourceCache<TValue>
		where TValue : class, IIdentifiable
	{
		private static readonly string Name = typeof(TValue).FullName;
		private readonly IQueryableRepository<TValue> Repository;
		private readonly IDisposable Subscription;
		private Dictionary<string, TValue> Data;
		private bool Invalid;

		public LazyCache(
			IQueryableRepository<TValue> repository,
			IDataChangeNotification notifications)
		{
			Contract.Requires(repository != null);
			Contract.Requires(notifications != null);

			this.Repository = repository;

			Subscription = notifications.Notifications.Subscribe(Synchronize);
			Data = new Dictionary<string, TValue>();
			Invalid = true;
		}

		private void Synchronize(NotifyInfo info)
		{
			if (info.Name == Name)
			{
				Invalid = true;
			}
		}

		public void Invalidate(IEnumerable<string> uris)
		{
			Invalid = true;
		}

		private void CheckInvalid()
		{
			if (Invalid)
			{
				Invalid = false;
				Data = Repository.Query().ToList().ToDictionary(it => it.URI, it => it);
			}
		}

		public TValue Find(string uri)
		{
			if (uri == null)
				return null;
			CheckInvalid();
			TValue item;
			Data.TryGetValue(uri, out item);
			return item;
		}

		public TValue[] Find(IEnumerable<string> uris)
		{
			if (uris != null)
			{
				CheckInvalid();
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
			Invalid = true;
		}

		public IQueryable<TValue> Query<TCondition>(ISpecification<TCondition> specification)
		{
			CheckInvalid();
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
			CheckInvalid();
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
			CheckInvalid();
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
			CheckInvalid();
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
