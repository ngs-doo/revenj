package org.revenj.patterns;

import java.util.List;

public interface SearchableRepository<T extends DataSource> {

	Query<T> query(Specification<T> specification);

	default Query<T> query() {
		return query(null);
	}

	List<T> search(
			Specification<T> specification,
			Integer limit,
			Integer offset);

	default List<T> search() {
		return search(null, null, null);
	}

	default List<T> search(int limit) {
		return search(null, limit, null);
	}

	default List<T> search(Specification<T> specification) {
		return search(specification, null, null);
	}

	default List<T> search(Specification<T> specification, int limit) {
		return search(specification, limit, null);
	}

	long count(Specification<T> specification);

	default long count() {
		return count(null);
	}

	boolean exists(Specification<T> specification);

	default boolean exists() {
		return exists(null);
	}
}
