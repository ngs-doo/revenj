package org.revenj.patterns;

import org.revenj.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Optional;

public interface WireSerialization {
	Bytes serialize(Object value, String contentType);

	void serialize(Object value, OutputStream stream, String contentType) throws IOException;

	Object deserialize(Type type, Bytes data, String accept) throws IOException;

	Object deserialize(Type type, InputStream stream, String accept) throws IOException;

	@SuppressWarnings("unchecked")
	default <T> T deserialize(Bytes data, String accept, Class<T> manifest) throws IOException {
		return (T) deserialize(manifest, data, accept);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(Bytes data, String accept, Class<T> container, Type argument, Type... arguments) throws IOException {
		return (T) deserialize(Utils.makeGenericType(container, argument, arguments), data, accept);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(byte[] data, String accept, Class<T> manifest) throws IOException {
		return (T) deserialize(manifest, new Bytes(data, data.length), accept);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(byte[] data, String accept, Class<T> container, Type argument, Type... arguments) throws IOException {
		return (T) deserialize(Utils.makeGenericType(container, argument, arguments), new Bytes(data, data.length), accept);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(byte[] data, int length, String accept, Class<T> manifest) throws IOException {
		return (T) deserialize(manifest, new Bytes(data, length), accept);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(byte[] data, int length, String accept, Class<T> container, Type argument, Type... arguments) throws IOException {
		return (T) deserialize(Utils.makeGenericType(container, argument, arguments), new Bytes(data, length), accept);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(InputStream stream, String accept, Class<T> manifest) throws IOException {
		return (T) deserialize(manifest, stream, accept);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(InputStream stream, String accept, Class<T> container, Type argument, Type... arguments) throws IOException {
		return (T) deserialize(Utils.makeGenericType(container, argument, arguments), stream, accept);
	}

	<TFormat> Optional<Serialization<TFormat>> find(Class<TFormat> format);
}
