package org.revenj.patterns;

public interface Report<T> {
	T populate(ServiceLocator locator);
}
