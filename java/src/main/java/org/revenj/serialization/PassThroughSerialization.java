package org.revenj.serialization;

import org.revenj.patterns.Serialization;

import java.io.IOException;
import java.lang.reflect.Type;

final class PassThroughSerialization implements Serialization<Object> {

	@Override
	public Object serialize(Object value) {
		return value;
	}

	@Override
	public Object deserialize(Type type, Object data) throws IOException {
		return data;
	}
}
