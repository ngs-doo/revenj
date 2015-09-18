package org.revenj.patterns;

import java.util.List;
import java.util.Optional;

public interface SearchableRepository<T extends DataSource> {

	Query<T> query(Specification<T> specification);

	default Query<T> query() {
		return query(null);
	}

	List<T> search(
			Optional<Specification<T>> specification,
			Optional<Integer> limit,
			Optional<Integer> offset);

	default List<T> search() {
		return search(Optional.<Specification<T>>empty(), Optional.<Integer>empty(), Optional.<Integer>empty());
	}

	default List<T> search(int limit) {
		return search(Optional.<Specification<T>>empty(), Optional.of(limit), Optional.<Integer>empty());
	}

	default List<T> search(Specification<T> specification) {
		return search(Optional.ofNullable(specification), Optional.<Integer>empty(), Optional.<Integer>empty());
	}

	default List<T> search(Specification<T> specification, int limit) {
		return search(Optional.ofNullable(specification), Optional.of(limit), Optional.<Integer>empty());
	}

	long count(Optional<Specification<T>> specification);

	default long count(Specification<T> specification) {
		return count(Optional.ofNullable(specification));
	}

	default long count() {
		return count(Optional.empty());
	}

	boolean exists(Optional<Specification<T>> specification);

	default boolean exists(Specification<T> specification) {
		return exists(Optional.ofNullable(specification));
	}

	default boolean exists() {
		return exists(Optional.empty());
	}
}
