package org.revenj.server.servlet;

import org.revenj.serialization.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

final class PassThroughSerialization implements Serialization<Object> {

	@Override
	public Object serialize(Object value) {
		return value;
	}

	@Override
	public void serialize(Object value, OutputStream stream) throws IOException {
		throw new IOException("Pass though can't serialize to stream");
	}

	@Override
	public Object deserialize(Type type, Object data) throws IOException {
		return data;
	}

	@Override
	public Object deserialize(Type type, InputStream stream) throws IOException {
		throw new IOException("Pass though can't deserialize from stream");
	}
}
