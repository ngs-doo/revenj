package org.revenj.patterns;

import java.io.Serializable;
import java.util.function.Predicate;

@FunctionalInterface
public interface Specification<T> extends Predicate<T>, Serializable {
	//default Specification<T> negate() {
	//	return t -> !test(t);
	//}
}
