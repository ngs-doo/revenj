package org.revenj;

import org.revenj.extensibility.Container;
import org.revenj.patterns.*;
import org.revenj.patterns.DataSource;
import rx.Observable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class LocatorDataContext implements UnitOfWork {
	private final Container scope;
	private ConcurrentHashMap<Class<?>, SearchableRepository> searchRepositories;
	private ConcurrentHashMap<Class<?>, Repository> lookupRepositories;
	private ConcurrentHashMap<Class<?>, PersistableRepository> persistableRepositories;
	private ConcurrentHashMap<Class<?>, Repository> historyRepositories;
	private ConcurrentHashMap<Class<?>, DomainEventStore> eventStores;
	private GlobalEventStore globalEventStore;
	private DataChangeNotification changes;
	private final Connection connection;
	private boolean hasChanges;
	private boolean closed;

	LocatorDataContext(Container scope, Connection connection) {
		this.scope = scope;
		this.connection = connection;
	}

	static DataContext asDataContext(Container container) {
		return new LocatorDataContext(container, null);
	}

	static DataContext asDataContext(Container container, Connection connection) {
		Container scope = container.createScope();
		scope.registerInstance(Connection.class, connection, false);
		return new LocatorDataContext(scope, connection);
	}

	static UnitOfWork asUnitOfWork(Container container) {
		javax.sql.DataSource dataSource = container.resolve(javax.sql.DataSource.class);
		java.sql.Connection connection = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			try {
				if (connection != null) connection.close();
			} catch (SQLException ignore) {
			}
			try {
				connection = dataSource.getConnection();
				connection.setAutoCommit(false);
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
		}
		Container scope = container.createScope();
		scope.registerInstance(Connection.class, connection, false);
		return new LocatorDataContext(scope, connection);
	}

	private <T extends DataSource> SearchableRepository<T> getSearchableRepository(Class<T> manifest) {
		if (closed) throw new RuntimeException("Unit of work has been closed");
		if (searchRepositories == null) searchRepositories = new ConcurrentHashMap<>();
		return searchRepositories.computeIfAbsent(manifest, clazz ->
		{
			if (persistableRepositories != null) {
				PersistableRepository repository = persistableRepositories.get(manifest);
				if (repository != null) return repository;
			}
			try {
				return (SearchableRepository) scope.resolve(Utils.makeGenericType(SearchableRepository.class, manifest));
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Repository is not registered for: " + manifest, ex);
			}
		});
	}

	private Repository getLookupRepository(Class<?> manifest) {
		if (closed) throw new RuntimeException("Unit of work has been closed");
		if (lookupRepositories == null) lookupRepositories = new ConcurrentHashMap<>();
		return lookupRepositories.computeIfAbsent(manifest, clazz ->
		{
			if (persistableRepositories != null) {
				PersistableRepository repository = persistableRepositories.get(manifest);
				if (repository != null) return repository;
			}
			try {
				return (Repository) scope.resolve(Utils.makeGenericType(Repository.class, manifest));
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Repository is not registered for: " + manifest, ex);
			}
		});
	}

	private Repository getHistoryRepository(Class<?> manifest) {
		if (closed) throw new RuntimeException("Unit of work has been closed");
		if (historyRepositories == null) historyRepositories = new ConcurrentHashMap<>();
		return historyRepositories.computeIfAbsent(manifest, clazz ->
		{
			try {
				return (Repository) scope.resolve(Utils.makeGenericType(Repository.class, Utils.makeGenericType(History.class, manifest)));
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Repository is not registered for: " + manifest, ex);
			}
		});
	}

	private PersistableRepository getPersistableRepository(Class<?> manifest) {
		if (closed) throw new RuntimeException("Unit of work has been closed");
		if (persistableRepositories == null) persistableRepositories = new ConcurrentHashMap<>();
		return persistableRepositories.computeIfAbsent(manifest, clazz ->
		{
			try {
				return (PersistableRepository) scope.resolve(Utils.makeGenericType(PersistableRepository.class, manifest));
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Repository is not registered for: " + manifest, ex);
			}
		});
	}

	private DomainEventStore getEventStore(Class<?> manifest) {
		if (closed) throw new RuntimeException("Unit of work has been closed");
		if (eventStores == null) eventStores = new ConcurrentHashMap<>();
		return eventStores.computeIfAbsent(manifest, clazz ->
		{
			try {
				return (DomainEventStore) scope.resolve(Utils.makeGenericType(SearchableRepository.class, manifest));
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Domain event store is not registered for: " + manifest, ex);
			}
		});
	}

	@Override
	public <T extends Identifiable> Optional<T> find(Class<T> manifest, String uri) {
		return getLookupRepository(manifest).find(uri);
	}

	@Override
	public <T extends Identifiable> List<T> find(Class<T> manifest, Collection<String> uris) {
		return getLookupRepository(manifest).find(uris);
	}

	@Override
	public <T extends DataSource> Query<T> query(Class<T> manifest, Specification<T> filter) {
		return getSearchableRepository(manifest).query(filter);
	}

	@Override
	public <T extends DataSource> List<T> search(Class<T> manifest, Specification<T> filter, Integer limit, Integer offset) {
		return getSearchableRepository(manifest).search(filter, limit, offset);
	}

	@Override
	public <T extends DataSource> long count(Class<T> manifest, Specification<T> filter) {
		return getSearchableRepository(manifest).count(filter);
	}

	@Override
	public <T extends DataSource> boolean exists(Class<T> manifest, Specification<T> filter) {
		return getSearchableRepository(manifest).exists(filter);
	}

	@Override
	public <T extends AggregateRoot> void create(Collection<T> aggregates) throws IOException {
		if (aggregates.size() == 0) {
			return;
		}
		Class<?> manifest = aggregates.iterator().next().getClass();
		getPersistableRepository(manifest).insert(aggregates);
		hasChanges = true;
	}

	@Override
	public <T extends AggregateRoot> void updatePairs(Collection<Map.Entry<T, T>> pairs) throws IOException {
		if (pairs.size() == 0) {
			return;
		}
		Class<?> manifest = pairs.iterator().next().getValue().getClass();
		getPersistableRepository(manifest).persist(null, pairs, null);
		hasChanges = true;
	}

	@Override
	public <T extends AggregateRoot> void delete(Collection<T> aggregates) throws IOException {
		if (aggregates.size() == 0) {
			return;
		}
		Class<?> manifest = aggregates.iterator().next().getClass();
		getPersistableRepository(manifest).delete(aggregates);
		hasChanges = true;
	}

	@Override
	public <T extends DomainEvent> void submit(Collection<T> events) {
		if (events.size() == 0) {
			return;
		}
		Class<?> manifest = events.iterator().next().getClass();
		getEventStore(manifest).submit(events);
		hasChanges = true;
	}

	@Override
	public <T extends DomainEvent> void queue(Collection<T> events) {
		if (globalEventStore == null) {
			globalEventStore = scope.resolve(GlobalEventStore.class);
		}
		for (T e : events) {
			globalEventStore.queue(e);
		}
	}

	@Override
	public <T> T populate(Report<T> report) {
		return report.populate(scope);
	}

	@Override
	public <T extends ObjectHistory> List<History<T>> history(Class<T> manifest, Collection<String> uris) {
		return getHistoryRepository(manifest).find(uris);
	}

	@Override
	public <T extends Identifiable> Observable<DataChangeNotification.TrackInfo<T>> track(Class<T> manifest) {
		if (changes == null) changes = scope.resolve(DataChangeNotification.class);
		return changes.track(manifest);
	}

	@Override
	public void commit() {
		if (hasChanges) {
			try {
				connection.commit();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		hasChanges = false;
	}

	@Override
	public void rollback() {
		try {
			connection.rollback();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		hasChanges = false;
	}

	@Override
	public void close() throws IOException {
		if (closed) {
			return;
		}
		if (connection != null) {
			if (hasChanges) {
				rollback();
			}
			try {
				connection.setAutoCommit(true);
				connection.close();
			} catch (SQLException e) {
				throw new IOException(e);
			}
			try {
				scope.close();
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		closed = true;
	}
}
