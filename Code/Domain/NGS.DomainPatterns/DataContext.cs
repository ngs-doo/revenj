using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Reactive.Linq;

namespace NGS.DomainPatterns
{
	public class DataContext : IDataContext
	{
		private readonly IServiceLocator Locator;
		private ConcurrentDictionary<Type, object> Lookups;
		private ConcurrentDictionary<Type, object> QuerySources;
		private ConcurrentDictionary<Type, object> Repositories;
		private ConcurrentDictionary<Type, object> EventStores;
		private ConcurrentDictionary<Type, object> Histories;
		private IDataChangeNotification Changes;

		public DataContext(IServiceLocator locator)
		{
			this.Locator = locator;
		}

		private Func<IEnumerable<string>, T[]> GetLookup<T>()
		{
			object lookup;
			if (Lookups == null) Lookups = new ConcurrentDictionary<Type, object>(1, 7);
			if (!Lookups.TryGetValue(typeof(T), out lookup))
			{
				lookup = Locator.Resolve<Func<IEnumerable<string>, T[]>>();
				Lookups.TryAdd(typeof(T), lookup);
			}
			return (Func<IEnumerable<string>, T[]>)lookup;
		}

		public T[] Find<T>(IEnumerable<string> uris) where T : IIdentifiable
		{
			var lookup = GetLookup<T>();
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

		public IQueryable<T> Query<T>() where T : IDataSource
		{
			return GetQuerySource<T>();
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

		public void Create<T>(T[] aggregates) where T : IAggregateRoot
		{
			var repository = GetRepository<T>();
			repository.Persist(aggregates, null, null);
		}

		public void Update<T>(KeyValuePair<T, T>[] pairs) where T : IAggregateRoot
		{
			var repository = GetRepository<T>();
			repository.Persist(null, pairs, null);
		}

		public void Delete<T>(T[] aggregates) where T : IAggregateRoot
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

		public void Submit<T>(T[] events) where T : IDomainEvent
		{
			var store = GetStore<T>();
			store.Submit(events);
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

		public IHistory<T>[] History<T>(string[] uris) where T : IObjectHistory
		{
			var repository = GetHistory<T>();
			return repository.Find(uris);
		}

		public OlapCubeQueryBuilder CubeBuilder<T>() where T : IOlapCubeQuery
		{
			var query = Locator.Resolve<IOlapCubeQuery>(typeof(T));
			return new OlapCubeQueryBuilder(query);
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
