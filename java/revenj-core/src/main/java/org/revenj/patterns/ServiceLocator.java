package org.revenj.patterns;

import org.revenj.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Optional;

public interface ServiceLocator {
	Object resolve(Type type) throws ReflectiveOperationException;

	@SuppressWarnings("unchecked")
	default <T> Optional<T> tryResolve(Class<T> manifest) {
		try {
			Object instance = resolve((Type) manifest);
			return Optional.ofNullable((T) instance);
		} catch (ReflectiveOperationException e) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	default <T> T resolve(Class<T> manifest) {
		try {
			return (T) resolve((Type) manifest);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	default <T> T resolve(Class<T> container, Type argument, Type... arguments) throws ReflectiveOperationException {
		return (T) resolve(Utils.makeGenericType(container, argument, arguments));
	}

	default <T> T create(Constructor<T> ctor) throws ReflectiveOperationException {
		Type[] types = ctor.getGenericParameterTypes();
		Object[] dependencies = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			dependencies[i] = resolve(types[i]);
		}
		return ctor.newInstance(dependencies);
	}
}
