using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Reactive.Linq;

namespace Revenj.DomainPatterns
{
	internal class DataContext : IDataContext
	{
		private readonly IServiceProvider Locator;
		private GlobalEventStore GlobalEventStore;
		private ConcurrentDictionary<Type, object> SingleLookups;
		private ConcurrentDictionary<Type, object> ManyLookups;
		private ConcurrentDictionary<Type, object> QuerySources;
		private ConcurrentDictionary<Type, object> SearchSources;
		private ConcurrentDictionary<Type, object> Repositories;
		private ConcurrentDictionary<Type, object> EventStores;
		private ConcurrentDictionary<Type, object> Histories;
		private IDataChangeNotification Changes;

		public DataContext(IServiceProvider locator)
		{
			this.Locator = locator;
		}

		private Func<string, T> GetSingleLookup<T>()
		{
			object lookup;
			if (SingleLookups == null) SingleLookups = new ConcurrentDictionary<Type, object>(1, 7);
			if (!SingleLookups.TryGetValue(typeof(T), out lookup))
			{
				lookup = Locator.Resolve<Func<string, T>>();
				SingleLookups.TryAdd(typeof(T), lookup);
			}
			return (Func<string, T>)lookup;
		}

		public T Find<T>(string uri) where T : IIdentifiable
		{
			var lookup = GetSingleLookup<T>();
			return lookup(uri);
		}

		private Func<IEnumerable<string>, T[]> GetManyLookup<T>()
		{
			object lookup;
			if (ManyLookups == null) ManyLookups = new ConcurrentDictionary<Type, object>(1, 7);
			if (!ManyLookups.TryGetValue(typeof(T), out lookup))
			{
				lookup = Locator.Resolve<Func<IEnumerable<string>, T[]>>();
				ManyLookups.TryAdd(typeof(T), lookup);
			}
			return (Func<IEnumerable<string>, T[]>)lookup;
		}

		public T[] Find<T>(IEnumerable<string> uris) where T : IIdentifiable
		{
			var lookup = GetManyLookup<T>();
			return lookup(uris);
		}

		private IQueryable<T> GetQuerySource<T>()
		{
			object queryable;
			if (QuerySources == null) QuerySources = new ConcurrentDictionary<Type, object>(1, 7);
			if (!QuerySources.TryGetValue(typeof(T), out queryable))
			{
				queryable = Locator.Resolve<IQueryable<T>>();
				QuerySources.TryAdd(typeof(T), queryable);
			}
			return (IQueryable<T>)queryable;
		}

		private IQueryableRepository<T> GetQueryRepository<T>() where T : IDataSource
		{
			object searchable;
			if (SearchSources == null) SearchSources = new ConcurrentDictionary<Type, object>(1, 7);
			if (!SearchSources.TryGetValue(typeof(T), out searchable))
			{
				searchable = Locator.Resolve<IQueryableRepository<T>>();
				SearchSources.TryAdd(typeof(T), searchable);
			}
			return (IQueryableRepository<T>)searchable;
		}

		public IQueryable<T> Query<T>() where T : IDataSource
		{
			return GetQuerySource<T>();
		}

		public T[] Search<T>(ISpecification<T> filter, int? limit, int? offset) where T : IDataSource
		{
			return GetQueryRepository<T>().Search(filter, limit, offset);
		}

		public long Count<T>(ISpecification<T> filter) where T : IDataSource
		{
			return GetQueryRepository<T>().Count(filter);
		}

		public bool Exists<T>(ISpecification<T> filter) where T : IDataSource
		{
			return GetQueryRepository<T>().Exists(filter);
		}

		private IPersistableRepository<T> GetRepository<T>() where T : IAggregateRoot
		{
			object repository;
			if (Repositories == null) Repositories = new ConcurrentDictionary<Type, object>(1, 7);
			if (!Repositories.TryGetValue(typeof(T), out repository))
			{
				repository = Locator.Resolve<IPersistableRepository<T>>();
				Repositories.TryAdd(typeof(T), repository);
			}
			return (IPersistableRepository<T>)repository;
		}

		public void Create<T>(IEnumerable<T> aggregates) where T : IAggregateRoot
		{
			var repository = GetRepository<T>();
			repository.Persist(aggregates, null, null);
		}

		public void Update<T>(IEnumerable<KeyValuePair<T, T>> pairs) where T : IAggregateRoot
		{
			var repository = GetRepository<T>();
			repository.Persist(null, pairs, null);
		}

		public void Delete<T>(IEnumerable<T> aggregates) where T : IAggregateRoot
		{
			var repository = GetRepository<T>();
			repository.Persist(null, null, aggregates);
		}

		private IDomainEventStore<T> GetStore<T>() where T : IDomainEvent
		{
			object store;
			if (EventStores == null) EventStores = new ConcurrentDictionary<Type, object>(1, 7);
			if (!EventStores.TryGetValue(typeof(T), out store))
			{
				store = Locator.Resolve<IDomainEventStore<T>>();
				EventStores.TryAdd(typeof(T), store);
			}
			return (IDomainEventStore<T>)store;
		}

		public void Submit<T>(IEnumerable<T> events) where T : IDomainEvent
		{
			var store = GetStore<T>();
			store.Submit(events);
		}

		public void Queue<T>(IEnumerable<T> events) where T : IDomainEvent
		{
			if (GlobalEventStore == null)
				GlobalEventStore = Locator.Resolve<GlobalEventStore>();
			foreach (var e in events)
				GlobalEventStore.Queue(e);
		}

		public T Populate<T>(IReport<T> report)
		{
			if (report != null)
				return report.Populate(Locator);
			return default(T);
		}

		public IObservable<NotifyInfo> Track<T>() where T : IIdentifiable
		{
			if (Changes == null) Changes = Locator.Resolve<IDataChangeNotification>();
			var name = typeof(T).FullName;
			return Changes.Notifications.Where(ni => ni.Name == name);
		}

		private IRepository<IHistory<T>> GetHistory<T>() where T : IObjectHistory
		{
			object repository;
			if (Histories == null) Histories = new ConcurrentDictionary<Type, object>(1, 7);
			if (!Histories.TryGetValue(typeof(T), out repository))
			{
				repository = Locator.Resolve<IRepository<IHistory<T>>>();
				Histories.TryAdd(typeof(T), repository);
			}
			return (IRepository<IHistory<T>>)repository;
		}

		public IHistory<T>[] History<T>(IEnumerable<string> uris) where T : IObjectHistory
		{
			var repository = GetHistory<T>();
			return repository.Find(uris);
		}

		public OlapCubeQueryBuilder<TSource> CubeBuilder<TCube, TSource>()
			where TCube : IOlapCubeQuery<TSource>
			where TSource : IDataSource
		{
			var query = Locator.Resolve<IOlapCubeQuery<TSource>>(typeof(TCube));
			return new OlapCubeQueryBuilder<TSource>(query);
		}

		public TResult[] InvalidItems<TValidation, TResult>(ISpecification<TResult> specification)
			where TValidation : IValidation<TResult>
			where TResult : IIdentifiable
		{
			var validation = Locator.Resolve<IValidation<TResult>>(typeof(TValidation));
			var queryable = GetQuerySource<TResult>();
			return validation.FindInvalidItems(queryable).ToArray();
		}
	}
}
