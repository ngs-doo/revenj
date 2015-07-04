package org.revenj.serialization;

import org.revenj.patterns.Bytes;
import org.revenj.patterns.Serialization;
import org.revenj.patterns.ServiceLocator;

import java.io.IOException;
import java.lang.reflect.Type;

public class PassThroughSerialization implements Serialization<Object> {

	@Override
	public <T> Bytes serialize(T value) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public <T> Object serializeTo(T value) {
		return value;
	}

	@Override
	public Converter<Object> getConverter() {
		throw new RuntimeException("Not supported");
	}

	@Override
	public Object deserialize(Type type, Bytes data, ServiceLocator locator) throws IOException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public Object deserialize(Type type, Object data, ServiceLocator locator) throws IOException {
		return data;
	}

	@Override
	public <T> T deserialize(Class<T> manifest, Object data, ServiceLocator locator) throws IOException {
		return (T) data;
	}
}
