package org.revenj.serialization;

import org.revenj.Utils;

import java.io.IOException;
import java.lang.reflect.Type;

public interface Serialization<TFormat> {
	TFormat serialize(Type type, Object value) throws IOException;

	default TFormat serialize(Object value) throws IOException {
		return serialize(null, value);
	}

	Object deserialize(Type type, TFormat data) throws IOException;

	@SuppressWarnings("unchecked")
	default <T> T deserialize(TFormat data, Class<T> manifest) throws IOException {
		return (T) deserialize(manifest, data);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(TFormat data, Class<T> container, Type argument, Type... arguments) throws IOException {
		return (T) deserialize(Utils.makeGenericType(container, argument, arguments), data);
	}
}
