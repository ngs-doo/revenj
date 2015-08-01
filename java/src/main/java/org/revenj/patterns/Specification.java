package org.revenj.patterns;

import java.io.Serializable;

@FunctionalInterface
public interface Specification<T extends Serializable> extends Serializable {

	boolean test(T item);
}
