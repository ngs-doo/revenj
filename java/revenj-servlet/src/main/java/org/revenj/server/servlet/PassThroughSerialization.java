package org.revenj.server.servlet;

import org.revenj.serialization.Serialization;

import java.io.IOException;
import java.lang.reflect.Type;

final class PassThroughSerialization implements Serialization<Object> {

	@Override
	public Object serialize(Type type, Object value) {
		return value;
	}

	@Override
	public Object deserialize(Type type, Object data) throws IOException {
		return data;
	}
}
