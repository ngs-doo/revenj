package org.revenj;

import org.revenj.patterns.*;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class LocatorDataContext implements DataContext {
	private final ServiceLocator locator;
	private ConcurrentHashMap<Class<?>, SearchableRepository> repositories;
	private ConcurrentHashMap<Class<?>, DomainEventStore> eventStores;

	public LocatorDataContext(ServiceLocator locator) {
		this.locator = locator;
	}

	private static class GenericType implements ParameterizedType {

		private final Type raw;
		private final Type[] arguments;

		public GenericType(Type raw, Class<?> argument) {
			this.raw = raw;
			this.arguments = new Type[]{argument};
		}

		@Override
		public Type[] getActualTypeArguments() {
			return arguments;
		}

		@Override
		public Type getRawType() {
			return raw;
		}

		@Override
		public Type getOwnerType() {
			return null;
		}

		@Override
		public String toString() {
			return raw.getTypeName() + "<" + arguments[0].getTypeName() + ">";
		}
	}

	private SearchableRepository getRepository(Class<?> manifest) {
		if (repositories == null) repositories = new ConcurrentHashMap<>();
		return repositories.computeIfAbsent(manifest, clazz ->
		{
			try {
				return (SearchableRepository) locator.resolve(new GenericType(SearchableRepository.class, manifest));
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
				return (DomainEventStore) locator.resolve(new GenericType(SearchableRepository.class, manifest));
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Domain event store is not registered for: " + manifest, ex);
			}
		});
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
}
