package org.revenj.serialization;

import org.revenj.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Optional;

public interface WireSerialization {
	void serialize(Object value, OutputStream stream, String contentType) throws IOException;

	default ByteArrayOutputStream serialize(Object value, String contentType) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serialize(value, os, contentType);
		return os;
	}

	Object deserialize(Type type, InputStream stream, String accept) throws IOException;

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
