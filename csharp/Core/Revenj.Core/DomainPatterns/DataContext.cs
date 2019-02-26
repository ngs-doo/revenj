using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Reactive.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace Revenj.DomainPatterns
{
	internal class DataContext : IDataContext
	{
		private readonly IServiceProvider Locator;
		private GlobalEventStore GlobalEventStore;
		private ConcurrentDictionary<Type, object> LookupRepositories;
		private ConcurrentDictionary<Type, object> LookupRepositoriesAsync;
		private ConcurrentDictionary<Type, object> QuerySources;
		private ConcurrentDictionary<Type, object> SearchSources;
		private ConcurrentDictionary<Type, object> SearchSourcesAsync;
		private ConcurrentDictionary<Type, object> PersistRepositories;
		private ConcurrentDictionary<Type, object> PersistRepositoriesAsync;
		private ConcurrentDictionary<Type, object> EventStores;
		private ConcurrentDictionary<Type, object> Histories;
		private ConcurrentDictionary<Type, object> HistoriesAsync;
		private IDataChangeNotification Changes;

		public DataContext(IServiceProvider locator)
		{
			this.Locator = locator;
		}

		private IRepository<T> GetLookupRepository<T>() where T : IIdentifiable
		{
			object repository;
			if (LookupRepositories == null) LookupRepositories = new ConcurrentDictionary<Type, object>(1, 7);
			if (!LookupRepositories.TryGetValue(typeof(T), out repository))
			{
				repository = Locator.Resolve<IRepository<T>>();
				LookupRepositories.TryAdd(typeof(T), repository);
			}
			return (IRepository<T>)repository;
		}

		private IRepositoryAsync<T> GetLookupRepositoryAsync<T>() where T : IIdentifiable
		{
			object repository;
			if (LookupRepositoriesAsync == null) LookupRepositoriesAsync = new ConcurrentDictionary<Type, object>(1, 7);
			if (!LookupRepositoriesAsync.TryGetValue(typeof(T), out repository))
			{
				repository = Locator.Resolve<IRepositoryAsync<T>>();
				LookupRepositoriesAsync.TryAdd(typeof(T), repository);
			}
			return (IRepositoryAsync<T>)repository;
		}

		public T Find<T>(string uri) where T : IIdentifiable
		{
			var repository = GetLookupRepository<T>();
			return repository.Find(uri);
		}

		public Task<T> FindAsync<T>(string uri, CancellationToken cancellationToken) where T : IIdentifiable
		{
			var repository = GetLookupRepositoryAsync<T>();
			return repository.FindAsync(uri, cancellationToken);
		}

		public T[] Find<T>(IEnumerable<string> uris) where T : IIdentifiable
		{
			var repository = GetLookupRepository<T>();
			return repository.Find(uris);
		}

		public Task<IEnumerable<T>> FindAsync<T>(IEnumerable<string> uris, CancellationToken cancellationToken)
			where T : IIdentifiable
		{
			var repository = GetLookupRepositoryAsync<T>();
			return repository.FindAsync(uris, cancellationToken);
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

		private IQueryableRepositoryAsync<T> GetQueryRepositoryAsync<T>() where T : IDataSource
		{
			object searchable;
			if (SearchSources == null) SearchSources = new ConcurrentDictionary<Type, object>(1, 7);
			if (!SearchSources.TryGetValue(typeof(T), out searchable))
			{
				searchable = Locator.Resolve<IQueryableRepositoryAsync<T>>();
				SearchSources.TryAdd(typeof(T), searchable);
			}
			return (IQueryableRepositoryAsync<T>)searchable;
		}

		public IQueryable<T> Query<T>() where T : IDataSource
		{
			return GetQuerySource<T>();
		}

		public T[] Search<T>(ISpecification<T> filter, int? limit, int? offset) where T : IDataSource
		{
			return GetQueryRepository<T>().Search(filter, limit, offset);
		}

		public Task<IEnumerable<T>> SearchAsync<T>(ISpecification<T> filter, int? limit, int? offset, CancellationToken cancellationToken)
			where T : IDataSource
		{
			return GetQueryRepositoryAsync<T>().SearchAsync(filter, limit, offset, cancellationToken);
		}

		public long Count<T>(ISpecification<T> filter) where T : IDataSource
		{
			return GetQueryRepository<T>().Count(filter);
		}

		public Task<long> CountAsync<T>(ISpecification<T> filter, CancellationToken cancellationToken) where T : IDataSource
		{
			return GetQueryRepositoryAsync<T>().CountAsync(filter, cancellationToken);
		}

		public bool Exists<T>(ISpecification<T> filter) where T : IDataSource
		{
			return GetQueryRepository<T>().Exists(filter);
		}

		public Task<bool> ExistsAsync<T>(ISpecification<T> filter, CancellationToken cancellationToken) where T : IDataSource
		{
			return GetQueryRepositoryAsync<T>().ExistsAsync(filter, cancellationToken);
		}

		private IPersistableRepository<T> GetPersistableRepository<T>() where T : IAggregateRoot
		{
			object repository;
			if (PersistRepositories == null) PersistRepositories = new ConcurrentDictionary<Type, object>(1, 7);
			if (!PersistRepositories.TryGetValue(typeof(T), out repository))
			{
				repository = Locator.Resolve<IPersistableRepository<T>>();
				PersistRepositories.TryAdd(typeof(T), repository);
			}
			return (IPersistableRepository<T>)repository;
		}

		private IPersistableRepositoryAsync<T> GetPersistableRepositoryAsync<T>() where T : IAggregateRoot
		{
			object repository;
			if (PersistRepositoriesAsync == null) PersistRepositoriesAsync = new ConcurrentDictionary<Type, object>(1, 7);
			if (!PersistRepositoriesAsync.TryGetValue(typeof(T), out repository))
			{
				repository = Locator.Resolve<IPersistableRepositoryAsync<T>>();
				PersistRepositoriesAsync.TryAdd(typeof(T), repository);
			}
			return (IPersistableRepositoryAsync<T>)repository;
		}

		public void Create<T>(IEnumerable<T> aggregates) where T : IAggregateRoot
		{
			var repository = GetPersistableRepository<T>();
			repository.Persist(aggregates, null, null);
		}

		public Task CreateAsync<T>(IEnumerable<T> aggregates, CancellationToken cancellationToken) where T : IAggregateRoot
		{
			var repository = GetPersistableRepositoryAsync<T>();
			return repository.PersistAsync(aggregates, null, null, cancellationToken);
		}

		public void Update<T>(IEnumerable<KeyValuePair<T, T>> pairs) where T : IAggregateRoot
		{
			var repository = GetPersistableRepository<T>();
			repository.Persist(null, pairs, null);
		}

		public Task UpdateAsync<T>(IEnumerable<KeyValuePair<T, T>> pairs, CancellationToken cancellationToken) where T : IAggregateRoot
		{
			var repository = GetPersistableRepositoryAsync<T>();
			return repository.PersistAsync(null, pairs, null, cancellationToken);
		}

		public void Delete<T>(IEnumerable<T> aggregates) where T : IAggregateRoot
		{
			var repository = GetPersistableRepository<T>();
			repository.Persist(null, null, aggregates);
		}

		public Task DeleteAsync<T>(IEnumerable<T> aggregates, CancellationToken cancellationToken) where T : IAggregateRoot
		{
			var repository = GetPersistableRepositoryAsync<T>();
			return repository.PersistAsync(null, null, aggregates, cancellationToken);
		}

		private IEventStore<T> GetStore<T>() where T : IEvent
		{
			object store;
			if (EventStores == null) EventStores = new ConcurrentDictionary<Type, object>(1, 7);
			if (!EventStores.TryGetValue(typeof(T), out store))
			{
				store = Locator.Resolve<IEventStore<T>>();
				EventStores.TryAdd(typeof(T), store);
			}
			return (IEventStore<T>)store;
		}

		public string[] Submit<T>(IEnumerable<T> events) where T : IEvent
		{
			var store = GetStore<T>();
			return store.Submit(events);
		}

		public Task<string[]> SubmitAsync<T>(IEnumerable<T> events, CancellationToken cancellationToken) where T : IEvent
		{
			var store = GetStore<T>();
			return store.SubmitAsync(events, cancellationToken);
		}

		public void Queue<T>(IEnumerable<T> events) where T : IEvent
		{
			if (GlobalEventStore == null)
				GlobalEventStore = Locator.Resolve<GlobalEventStore>();
			foreach (var e in events)
				GlobalEventStore.Queue(e);
		}

		public T Populate<T>(IReport<T> report)
		{
			if (report == null) throw new ArgumentNullException("report can't be null");
			return report.Populate(Locator);
		}

		public Task<T> PopulateAsync<T>(IReport<T> report, CancellationToken cancellationToken)
		{
			if (report == null) throw new ArgumentNullException("report can't be null");
			return report.PopulateAsync(Locator, cancellationToken);
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

		private IRepositoryAsync<IHistory<T>> GetHistoryAsync<T>() where T : IObjectHistory
		{
			object repository;
			if (HistoriesAsync == null) HistoriesAsync = new ConcurrentDictionary<Type, object>(1, 7);
			if (!HistoriesAsync.TryGetValue(typeof(T), out repository))
			{
				repository = Locator.Resolve<IRepositoryAsync<IHistory<T>>>();
				HistoriesAsync.TryAdd(typeof(T), repository);
			}
			return (IRepositoryAsync<IHistory<T>>)repository;
		}

		public IHistory<T>[] History<T>(IEnumerable<string> uris) where T : IObjectHistory
		{
			var repository = GetHistory<T>();
			return repository.Find(uris);
		}

		public Task<IEnumerable<IHistory<T>>> HistoryAsync<T>(IEnumerable<string> uris, CancellationToken cancellationToken) where T : IObjectHistory
		{
			var repository = GetHistoryAsync<T>();
			return repository.FindAsync(uris, cancellationToken);
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
