package org.revenj.patterns;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface Query<T extends DataSource> {

	Query<T> filter(Specification<T> predicate);

	Query<T> skip(long n);

	Query<T> limit(long n);

	@FunctionalInterface
	interface Compare<U, V extends Comparable<V>> extends Serializable {
		V compare(U item);
	}

	<V extends Comparable<V>> Query<T> sortedBy(Compare<T, V> order);

	<V extends Comparable<V>> Query<T> sortedDescendingBy(Compare<T, V> order);

	long count() throws SQLException;

	boolean anyMatch(Specification<? super T> predicate) throws SQLException;

	boolean allMatch(Specification<? super T> predicate) throws SQLException;

	boolean noneMatch(Specification<? super T> predicate) throws SQLException;

	Optional<T> findFirst() throws SQLException;

	Optional<T> findAny() throws SQLException;

	List<T> list() throws SQLException;

	default Stream<T> stream() throws SQLException {
		return list().stream();
	}
}
