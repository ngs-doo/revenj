package org.revenj.server.servlet;

import com.dslplatform.json.DslJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.revenj.json.DslJsonSerialization;
import org.revenj.serialization.Serialization;
import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.WireSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Optional;

final class RevenjSerialization implements WireSerialization {
	private final DslJsonSerialization json;
	private final PassThroughSerialization passThrough;

	public RevenjSerialization(ServiceLocator locator) {
		JacksonSerialization jackson = new JacksonSerialization(locator, locator.tryResolve(ObjectMapper.class));
		this.json = new DslJsonSerialization(locator, Optional.of(new DslJson.Fallback<ServiceLocator>() {
			@Override
			public void serialize(Object instance, OutputStream stream) throws IOException {
				jackson.serialize(instance, stream);
			}

			@Override
			public Object deserialize(ServiceLocator serviceLocator, Type manifest, byte[] body, int size) throws IOException {
				return jackson.deserialize(manifest, body, size);
			}
		}));
		this.passThrough = new PassThroughSerialization();
	}

	@Override
	public void serialize(Object value, OutputStream stream, String contentType) throws IOException {
		json.serialize(value, stream);
	}

	@Override
	public Object deserialize(Type type, byte[] content, int length, String accept) throws IOException {
		return json.deserialize(type, content, length);
	}

	@Override
	public Object deserialize(Type type, InputStream stream, String accept) throws IOException {
		return json.deserialize(type, stream);
	}

	@Override
	public <TFormat> Optional<Serialization<TFormat>> find(Class<TFormat> format) {
		if (Object.class.equals(format)) {
			return Optional.of((Serialization<TFormat>) passThrough);
		} else if (String.class.equals(format)) {
			return Optional.of((Serialization<TFormat>) json);
		}
		return Optional.empty();
	}
}
