package org.revenj.patterns;

import java.io.IOException;
import java.util.*;

public interface PersistableRepository<T extends AggregateRoot> extends Repository<T>, SearchableRepository<T> {

	String[] persist(Collection<T> insert, Collection<Map.Entry<T, T>> update, Collection<T> delete) throws IOException;

	default String[] insert(Collection<T> items) throws IOException {
		return persist(items, null, null);
	}

	default String[] insert(T[] items) throws IOException {
		return insert(Arrays.asList(items));
	}

	default String insert(T item) throws IOException {
		return persist(Collections.singletonList(item), null, null)[0];
	}

	default void update(Collection<T> items) throws IOException {
		List<Map.Entry<T, T>> pairs = new ArrayList<>(items.size());
		for (T item : items) {
			pairs.add(new AbstractMap.SimpleEntry<>(null, item));
		}
		persist(null, pairs, null);
	}

	default void update(T[] items) throws IOException {
		update(Arrays.asList(items));
	}

	default void update(T old, T current) throws IOException {
		persist(null, Collections.singletonList(new AbstractMap.SimpleEntry<T, T>(old, current)), null);
	}

	default void update(T item) throws IOException {
		update(null, item);
	}

	default void delete(Collection<T> items) throws IOException {
		persist(null, null, items);
	}

	default void delete(T[] items) throws IOException {
		delete(Arrays.asList(items));
	}

	default void delete(T item) throws IOException {
		persist(null, null, Collections.singletonList(item));
	}
}
