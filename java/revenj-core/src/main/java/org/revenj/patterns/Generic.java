package org.revenj.patterns;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class Generic<T> {

	public final Type type;

	protected Generic() {
		Type superclass = getClass().getGenericSuperclass();
		if (superclass instanceof Class) {
			throw new RuntimeException("Missing type parameter.");
		}
		this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	public T resolve(ServiceLocator locator) {
		try {
			return (T) locator.resolve(type);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}