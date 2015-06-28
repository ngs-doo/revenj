package org.revenj.patterns;

import java.sql.SQLException;
import java.util.*;

public interface PersistableRepository<T/* extends AggregateRoot*/> extends Repository<T> {
	List<String> persist(List<T> insert, List<Map.Entry<T, T>> update, List<T> delete) throws SQLException;

	default List<String> insert(List<T> items) throws SQLException {
		return persist(items, null, null);
	}

	default String insert(T item) throws SQLException {
		return persist(Collections.singletonList(item), null, null).get(0);
	}

	default void update(List<T> items) throws SQLException {
		List<Map.Entry<T, T>> pairs = new ArrayList<>(items.size());
		for (T item : items) {
			pairs.add(new AbstractMap.SimpleEntry<T, T>(null, item));
		}
		persist(null, pairs, null);
	}

	default void update(T old, T current) throws SQLException {
		persist(null, Collections.singletonList(new AbstractMap.SimpleEntry<T, T>(old, current)), null);
	}

	default void update(T item) throws SQLException {
		persist(null, Collections.singletonList(new AbstractMap.SimpleEntry<T, T>(null, item)), null);
	}

	default void delete(List<T> items) throws SQLException {
		persist(null, null, items);
	}

	default void delete(T item) throws SQLException {
		persist(null, null, Collections.singletonList(item));
	}
}
