package org.revenj.patterns;

import java.io.IOException;
import java.lang.reflect.Type;

public interface Serialization<TFormat> {
	<T> Bytes serialize(T value);

	default <T> TFormat serializeTo(T value) {
		Converter<TFormat> converter = getConverter();
		Bytes bytes = serialize(value);
		return converter.convert(bytes);
	}

	Converter<TFormat> getConverter();

	interface Converter<TFormat> {
		TFormat convert(Bytes input);

		Bytes convert(TFormat input);
	}

	Object deserialize(Type type, Bytes data, ServiceLocator locator) throws IOException;

	default <T> T deserialize(Class<T> manifest, Bytes data, ServiceLocator locator) throws IOException {
		return (T) deserialize((Type) manifest, data, locator);
	}

	default Object deserialize(Type type, TFormat data, ServiceLocator locator) throws IOException {
		Converter<TFormat> converter = getConverter();
		Bytes bytes = converter.convert(data);
		return deserialize(type, bytes, locator);
	}

	default <T> T deserialize(Class<T> manifest, TFormat data, ServiceLocator locator) throws IOException {
		Converter<TFormat> converter = getConverter();
		Bytes bytes = converter.convert(data);
		return deserialize(manifest, bytes, locator);
	}

	default <T> T deserialize(Class<T> manifest, TFormat data) throws IOException {
		Converter<TFormat> converter = getConverter();
		Bytes bytes = converter.convert(data);
		return deserialize(manifest, bytes, null);
	}
}
