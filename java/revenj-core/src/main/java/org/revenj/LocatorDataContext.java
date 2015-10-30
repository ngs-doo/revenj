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
	private final Container locator;
	private ConcurrentHashMap<Class<?>, SearchableRepository> repositories;
	private ConcurrentHashMap<Class<?>, DomainEventStore> eventStores;
	private DataChangeNotification changes;
	private final Connection connection;
	private boolean hasChanges;
	private boolean closed;

	LocatorDataContext(Container locator, Connection connection) {
		this.locator = locator;
		this.connection = connection;
	}

	static DataContext asDataContext(Container container) {
		return new LocatorDataContext(container, null);
	}

	static UnitOfWork asUnitOfWork(Container container) {
		javax.sql.DataSource dataSource = container.resolve(javax.sql.DataSource.class);
		Container locator = container.createScope();
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
		locator.registerInstance(Connection.class, connection, false);
		return new LocatorDataContext(locator, connection);
	}

	private SearchableRepository getRepository(Class<?> manifest) {
		if (closed) throw new RuntimeException("Unit of work has been closed");
		if (repositories == null) repositories = new ConcurrentHashMap<>();
		return repositories.computeIfAbsent(manifest, clazz ->
		{
			try {
				return (SearchableRepository) locator.resolve(Utils.makeGenericType(SearchableRepository.class, manifest));
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
				return (DomainEventStore) locator.resolve(Utils.makeGenericType(SearchableRepository.class, manifest));
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Domain event store is not registered for: " + manifest, ex);
			}
		});
	}

	@Override
	public <T extends Identifiable> Optional<T> find(Class<T> manifest, String uri) {
		return ((Repository) getRepository(manifest)).find(uri);
	}

	@Override
	public <T extends Identifiable> List<T> find(Class<T> manifest, Collection<String> uris) {
		return ((Repository) getRepository(manifest)).find(uris);
	}

	@Override
	public <T extends DataSource> Query<T> query(Class<T> manifest, Specification<T> filter) {
		return getRepository(manifest).query(filter);
	}

	@Override
	public <T extends DataSource> List<T> search(Class<T> manifest, Specification<T> filter, Integer limit, Integer offset) {
		return getRepository(manifest).search(filter, limit, offset);
	}

	@Override
	public <T extends DataSource> long count(Class<T> manifest, Specification<T> filter) {
		return getRepository(manifest).count(filter);
	}

	@Override
	public <T extends DataSource> boolean exists(Class<T> manifest, Specification<T> filter) {
		return getRepository(manifest).exists(filter);
	}

	@Override
	public <T extends AggregateRoot> void create(Collection<T> aggregates) throws IOException {
		if (aggregates.size() == 0) {
			return;
		}
		Class<?> manifest = aggregates.iterator().next().getClass();
		((PersistableRepository) getRepository(manifest)).insert(aggregates);
		hasChanges = true;
	}

	@Override
	public <T extends AggregateRoot> void update(Collection<Map.Entry<T, T>> pairs) throws IOException {
		if (pairs.size() == 0) {
			return;
		}
		Class<?> manifest = pairs.iterator().next().getValue().getClass();
		((PersistableRepository) getRepository(manifest)).persist(null, pairs, null);
		hasChanges = true;
	}

	@Override
	public <T extends AggregateRoot> void delete(Collection<T> aggregates) throws IOException {
		if (aggregates.size() == 0) {
			return;
		}
		Class<?> manifest = aggregates.iterator().next().getClass();
		((PersistableRepository) getRepository(manifest)).insert(aggregates);
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
	public <T> T populate(Report<T> report) {
		return report.populate(locator);
	}

	@Override
	public <T extends Identifiable> Observable<DataChangeNotification.TrackInfo<T>> track(Class<T> manifest) {
		if (changes == null) changes = locator.resolve(DataChangeNotification.class);
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
		}
		try {
			locator.close();
		} catch (Exception e) {
			throw new IOException(e);
		}
		closed = true;
	}
}
