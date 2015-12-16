package org.revenj.json;

import com.dslplatform.json.*;
import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.Serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;

public class DslJsonSerialization extends DslJson<ServiceLocator> implements Serialization<String> {

	public DslJsonSerialization(final ServiceLocator locator, Optional<Fallback<ServiceLocator>> fallback) {
		super(locator, false, true, false, fallback.orElse(null), ServiceLoader.load(Configuration.class));
	}

	@Override
	public String serialize(Object value) throws IOException {
		if (value == null) return "null";
		final JsonWriter jw = new JsonWriter();
		final Class<?> manifest = value.getClass();
		if (!serialize(jw, manifest, value)) {
			if (fallback != null) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				fallback.serialize(value, os);
				return os.toString("UTF-8");
			}
			throw new IOException("Unable to serialize provided object. Failed to find serializer for: " + manifest);
		}
		return jw.toString();
	}

	@Override
	public Object deserialize(Type type, String data) throws IOException {
		byte[] bytes = data.getBytes("UTF-8");
		return super.deserialize(type, bytes, bytes.length);
	}

	@Deprecated
	public Object deserialize(byte[] tmp, Type type, InputStream stream) throws IOException {
		int size = 0;
		int read;
		while ((read = stream.read(tmp, size, tmp.length - size)) != -1) {
			size += read;
			if (size == tmp.length) {
				tmp = Arrays.copyOf(tmp, tmp.length * 2);
			}
		}
		//TODO: use underlying stream deserializer when it starts supporting autogrowing buffer
		return super.deserialize(type, tmp, size);
	}
}
