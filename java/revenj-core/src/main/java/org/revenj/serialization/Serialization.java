package org.revenj.serialization;

import org.revenj.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface Serialization<TFormat> {
	TFormat serialize(Object value) throws IOException;

	void serialize(Object value, OutputStream stream) throws IOException;

	Object deserialize(Type type, TFormat data) throws IOException;

	Object deserialize(Type type, InputStream stream) throws IOException;

	@SuppressWarnings("unchecked")
	default <T> T deserialize(TFormat data, Class<T> manifest) throws IOException {
		return (T) deserialize(manifest, data);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(InputStream stream, Class<T> manifest) throws IOException {
		return (T) deserialize(manifest, stream);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(TFormat data, Class<T> container, Type argument, Type... arguments) throws IOException {
		return (T) deserialize(Utils.makeGenericType(container, argument, arguments), data);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(InputStream stream, Class<T> container, Type argument, Type... arguments) throws IOException {
		return (T) deserialize(Utils.makeGenericType(container, argument, arguments), stream);
	}
}
