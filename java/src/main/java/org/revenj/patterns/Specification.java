package org.revenj.patterns;

import java.util.function.Predicate;

public interface Specification<T> {
	Predicate<T> getPredicate();
}
