package org.revenj.serialization;

import org.revenj.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Optional;

public interface WireSerialization {
	String serialize(Object value, OutputStream stream, String accept) throws IOException;

	default ByteArrayOutputStream serialize(Object value, String accept) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serialize(value, os, accept);
		return os;
	}

	Object deserialize(Type type, byte[] content, int length, String contentType) throws IOException;

	Object deserialize(Type type, InputStream stream, String contentType) throws IOException;

	@SuppressWarnings("unchecked")
	default <T> T deserialize(byte[] content, int length, String contentType, Class<T> manifest) throws IOException {
		return (T) deserialize(manifest, content, length, contentType);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(byte[] content, String contentType, Class<T> manifest) throws IOException {
		return (T) deserialize(manifest, content, content.length, contentType);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(InputStream stream, String contentType, Class<T> manifest) throws IOException {
		return (T) deserialize(manifest, stream, contentType);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(byte[] content, int length, String contentType, Class<T> container, Type argument, Type... arguments) throws IOException {
		return (T) deserialize(Utils.makeGenericType(container, argument, arguments), content, length, contentType);
	}

	@SuppressWarnings("unchecked")
	default <T> T deserialize(InputStream stream, String contentType, Class<T> container, Type argument, Type... arguments) throws IOException {
		return (T) deserialize(Utils.makeGenericType(container, argument, arguments), stream, contentType);
	}

	<TFormat> Optional<Serialization<TFormat>> find(Class<TFormat> format);
}
