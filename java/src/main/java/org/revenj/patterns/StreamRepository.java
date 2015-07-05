package org.revenj.patterns;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface StreamRepository<TSource> {
	Stream<TSource> stream(Optional<Specification<TSource>> specification);

	default Stream<TSource> stream() {
		return stream(Optional.<Specification<TSource>>empty());
	}

	List<TSource> search(
			Optional<Specification<TSource>> specification,
			Optional<Integer> limit,
			Optional<Integer> offset);

	default List<TSource> search() {
		return search(Optional.<Specification<TSource>>empty(), Optional.<Integer>empty(), Optional.<Integer>empty());
	}

	default List<TSource> search(int limit) {
		return search(Optional.<Specification<TSource>>empty(), Optional.of(limit), Optional.<Integer>empty());
	}

	default List<TSource> search(Specification<TSource> specification) {
		return search(Optional.of(specification), Optional.<Integer>empty(), Optional.<Integer>empty());
	}

	default List<TSource> search(Specification<TSource> specification, int limit) {
		return search(Optional.of(specification), Optional.of(limit), Optional.<Integer>empty());
	}
}
