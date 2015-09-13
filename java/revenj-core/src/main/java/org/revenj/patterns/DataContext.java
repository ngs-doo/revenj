package org.revenj.patterns;

import rx.Observable;

import java.io.IOException;
import java.util.*;

public interface DataContext {
	<T extends Identifiable> List<T> find(Class<T> manifest, Collection<String> uris);

	default <T extends Identifiable> Optional<T> find(Class<T> manifest, String uri) {
		List<T> found = find(manifest, Collections.singletonList(uri));
		return found.size() == 1 ? Optional.of(found.get(0)) : Optional.<T>empty();
	}

	default <T extends Identifiable> List<T> find(Class<T> manifest, String[] uris) {
		return find(manifest, Arrays.asList(uris));
	}

	<T extends DataSource> Query<T> query(Class<T> manifest, Specification<T> filter);

	default <T extends DataSource> Query<T> query(Class<T> manifest) {
		return query(manifest, null);
	}

	<T extends AggregateRoot> void create(Collection<T> aggregates) throws IOException;

	default <T extends AggregateRoot> void create(T aggregate) throws IOException {
		create(Collections.singletonList(aggregate));
	}

	<T extends AggregateRoot> void update(Collection<Map.Entry<T, T>> pairs) throws IOException;

	default <T extends AggregateRoot> void update(T oldAggregate, T newAggregate) throws IOException {
		update(Collections.singletonList(new HashMap.SimpleEntry<>(oldAggregate, newAggregate)));
	}

	default <T extends AggregateRoot> void update(T aggregate) throws IOException {
		update(Collections.singletonList(new HashMap.SimpleEntry<>(null, aggregate)));
	}

	default <T extends AggregateRoot> void update(List<T> aggregates) throws IOException {
		Collection<Map.Entry<T, T>> collection = new ArrayList<>(aggregates.size());
		for (T item : aggregates) {
			collection.add(new AbstractMap.SimpleEntry<>(null, item));
		}
		update(collection);
	}

	<T extends AggregateRoot> void delete(Collection<T> aggregates) throws IOException;

	default <T extends AggregateRoot> void delete(T aggregate) throws IOException {
		delete(Collections.singletonList(aggregate));
	}

	<T extends DomainEvent> void submit(Collection<T> events);

	default <T extends DomainEvent> void submit(T event) {
		submit(Collections.singletonList(event));
	}

	<T> T populate(Report<T> report);

	<T extends Identifiable> Observable<DataChangeNotification.TrackInfo<T>> track(Class<T> manifest);
}
