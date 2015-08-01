package org.revenj.patterns;

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

	default <T> T deserialize(Class<T> manifest, Bytes data, String accept) throws IOException {
		return (T) deserialize((Type)manifest, data, accept);
	}

	default <T> T deserialize(Class<T> manifest, byte[] data, String accept) throws IOException {
		return (T) deserialize((Type)manifest, new Bytes(data, data.length), accept);
	}

	default <T> T deserialize(Class<T> manifest, byte[] data, int length, String accept) throws IOException {
		return (T) deserialize((Type)manifest, new Bytes(data, length), accept);
	}

	default <T> T deserialize(Class<T> manifest, InputStream stream, String accept) throws IOException {
		return (T) deserialize((Type)manifest, stream, accept);
	}

	<TFormat> Optional<Serialization<TFormat>> find(Class<TFormat> format);
}
