package org.revenj.patterns;

import java.io.Serializable;

public interface Specification<T> extends Serializable {

    boolean test(T item);
}
