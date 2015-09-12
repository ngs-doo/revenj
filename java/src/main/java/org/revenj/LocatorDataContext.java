package org.revenj;

import org.revenj.patterns.*;
import rx.Observable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class LocatorDataContext implements DataContext {
	private final ServiceLocator locator;
	private ConcurrentHashMap<Class<?>, SearchableRepository> repositories;
	private ConcurrentHashMap<Class<?>, DomainEventStore> eventStores;
	private DataChangeNotification changes;

	public LocatorDataContext(ServiceLocator locator) {
		this.locator = locator;
	}

	private SearchableRepository getRepository(Class<?> manifest) {
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
		return (getRepository(manifest)).query(filter);
	}

	@Override
	public <T extends AggregateRoot> void create(Collection<T> aggregates) throws IOException {
		if (aggregates.size() == 0) {
			return;
		}
		Class<?> manifest = aggregates.iterator().next().getClass();
		((PersistableRepository) getRepository(manifest)).insert(aggregates);
	}

	@Override
	public <T extends AggregateRoot> void update(Collection<Map.Entry<T, T>> pairs) throws IOException {
		if (pairs.size() == 0) {
			return;
		}
		Class<?> manifest = pairs.iterator().next().getValue().getClass();
		((PersistableRepository) getRepository(manifest)).update(pairs);
	}

	@Override
	public <T extends AggregateRoot> void delete(Collection<T> aggregates) throws IOException {
		if (aggregates.size() == 0) {
			return;
		}
		Class<?> manifest = aggregates.iterator().next().getClass();
		((PersistableRepository) getRepository(manifest)).insert(aggregates);
	}

	@Override
	public <T extends DomainEvent> void submit(Collection<T> events) {
		if (events.size() == 0) {
			return;
		}
		Class<?> manifest = events.iterator().next().getClass();
		(getEventStore(manifest)).submit(events);
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
}
