using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using NGS.DomainPatterns;
using NGS.Utility;

namespace NGS.DatabasePersistence.Postgres
{
	public class PostgresDataContext : IDataContext
	{
		private readonly IServiceLocator Locator;
		private readonly IPostgresDatabaseQuery Query;
		private readonly Lazy<IPostgresConverterFactory> Converter;
		private readonly Dictionary<Type, object> Repositories = new Dictionary<Type, object>();
		private readonly Dictionary<Type, object> QueryableRepositories = new Dictionary<Type, object>();
		private readonly Dictionary<Type, object> PersistableRepositories = new Dictionary<Type, object>();
		private readonly List<IAggregateRoot> RootChanges = new List<IAggregateRoot>();
		private readonly List<IDomainEvent> EventChanges = new List<IDomainEvent>();

		public PostgresDataContext(
			IServiceLocator locator,
			IDatabaseQuery query,
			Lazy<IPostgresConverterFactory> converter)
		{
			Contract.Requires(locator != null);
			Contract.Requires(query != null);
			Contract.Requires(converter != null);

			this.Locator = locator;
			this.Query = query as IPostgresDatabaseQuery;
			this.Converter = converter;
		}

		private TService GetOrCreate<TService>(Dictionary<Type, object> cache, Type domainType, Type serviceType)
		{
			object service;
			if (!cache.TryGetValue(domainType, out service))
			{
				service = Locator.Resolve<TService>(serviceType);
				cache[domainType] = service;
			}
			return (TService)service;
		}

		public T[] Find<T>(string[] uris) where T : IIdentifiable
		{
			var isRoot = typeof(IAggregateRoot).IsAssignableFrom(typeof(T));
			var rep =
				GetOrCreate<IRepository<T>>(
					Repositories,
					typeof(T),
					isRoot ? typeof(IDataCache<>).MakeGenericType(typeof(T)) : typeof(IRepository<T>));
			var result = rep.Find(uris);
			//todo security permission
			if (isRoot)
				RootChanges.AddRange(result as IAggregateRoot[]);
			return result;
		}

		public TResult[] Search<TResult, TFilter>(ISpecification<TFilter> specification, int? limit, int? offset, IEnumerable<KeyValuePair<string, bool>> order)
		{
			TResult[] found;
			if (Query != null)
			{
				if (typeof(TResult) == typeof(TFilter) || specification == null)
				{
					var instancer = Converter.Value.GetInstanceFactory(typeof(TResult));
					if (instancer != null)
					{
						found = Query.Search<TResult>((ISpecification<TResult>)specification, limit, offset, order, dr => (TResult)instancer(dr.GetValue(0), Locator));
						//todo security
						if (typeof(IAggregateRoot).IsAssignableFrom(typeof(TResult)))
							RootChanges.AddRange(found as IAggregateRoot[]);
						return found;
					}
				}
			}
			var rep = GetOrCreate<IQueryableRepository<TResult>>(QueryableRepositories, typeof(TResult), typeof(IQueryableRepository<TResult>));
			var result = rep.Query(specification);
			if (offset != null)
				result = result.Skip(offset.Value);
			if (limit != null)
				result = result.Take(limit.Value);
			if (order != null)
				result = DynamicOrderBy.OrderBy(result, order.ToDictionary(it => it.Key, it => it.Value));
			found = result.ToArray();
			if (typeof(IAggregateRoot).IsAssignableFrom(typeof(TResult)))
				RootChanges.AddRange(found as IAggregateRoot[]);
			return found;
		}

		public long Count<TTarget, TFilter>(ISpecification<TFilter> specification)
		{
			if (Query != null)
			{
				if (typeof(TFilter) == typeof(TFilter) || specification == null)
					return Query.Count<TTarget>((ISpecification<TTarget>)specification);
			}
			var rep = GetOrCreate<IQueryableRepository<TTarget>>(QueryableRepositories, typeof(TTarget), typeof(IQueryableRepository<TTarget>));
			return rep.Query(specification).LongCount();
		}

		public void Submit<T>(T root) where T : IAggregateRoot
		{
			RootChanges.Add(root);
		}

		public void Delete<T>(T root) where T : IAggregateRoot
		{
			RootChanges.Add(root);
		}

		public void Raise<T>(T domainEvent) where T : IDomainEvent
		{
			EventChanges.Add(domainEvent);
		}

		public T Populate<T>(IReport<T> report)
		{
			return report.Populate(Locator);
		}

		public void SaveChanges()
		{
			RootChanges.Clear();
			EventChanges.Clear();
		}

		public void Dispose()
		{
			RootChanges.Clear();
			EventChanges.Clear();
			Repositories.Clear();
			QueryableRepositories.Clear();
			PersistableRepositories.Clear();
		}
	}
}
