package org.revenj.patterns;

import java.io.Serializable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface Query<T> {

	Query<T> filter(Specification<T> predicate);

	Query<T> skip(long n);

	Query<T> limit(long n);

	@FunctionalInterface
	interface Compare<U, V> extends Serializable {
		V compare(U item);
	}

	<V> Query<T> sortedBy(Compare<T, V> order);

	<V> Query<T> sortedDescendingBy(Compare<T, V> order);

	long count() throws IOException;

	boolean anyMatch(Specification<? super T> predicate) throws IOException;

	default boolean any() throws IOException {
		return anyMatch(null);
	}

	boolean allMatch(Specification<? super T> predicate) throws IOException;

	boolean noneMatch(Specification<? super T> predicate) throws IOException;

	Optional<T> findFirst() throws IOException;

	Optional<T> findAny() throws IOException;

	List<T> list() throws IOException;

	default Stream<T> stream() throws IOException {
		return list().stream();
	}
}
