package org.revenj.json;

import com.dslplatform.json.*;
import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;

public class DslJsonSerialization extends DslJson<ServiceLocator> implements Serialization<String> {

	public DslJsonSerialization(final ServiceLocator locator) {
		super(locator, false, true, false, null);

		registerReader(LocalDate.class, JavaTimeConverter.LocalDateReader);
		registerWriter(LocalDate.class, JavaTimeConverter.LocalDateWriter);
		registerReader(LocalDateTime.class, JavaTimeConverter.LocalDateTimeReader);
		registerWriter(LocalDateTime.class, JavaTimeConverter.LocalDateTimeWriter);
		registerReader(OffsetDateTime.class, JavaTimeConverter.DateTimeReader);
		registerWriter(OffsetDateTime.class, JavaTimeConverter.DateTimeWriter);
	}

	@Override
	public String serialize(Object value) throws IOException {
		if (value == null) return "null";
		final JsonWriter jw = new JsonWriter();
		final Class<?> manifest = value.getClass();
		if (!serialize(jw, manifest, value)) {
			throw new IOException("Unable to serialize provided object. Failed to find serializer for: " + manifest);
		}
		return jw.toString();
	}

	@Override
	public Object deserialize(Type type, String data) throws IOException {
		byte[] bytes = data.getBytes("UTF-8");
		if (type instanceof Class<?>) {
			return super.deserialize((Class<?>) type, bytes, bytes.length);
		} else {
			throw new IOException("Provided type is not an instace of Class. Unable to deserialize provided object.");
		}
	}

	@Override
	public Object deserialize(Type type, InputStream stream) throws IOException {
		byte[] bytes = new byte[512];
		int size = 0;
		int read;
		while ((read = stream.read(bytes, size, bytes.length - size)) != -1) {
			size += read;
			if (size == read) {
				bytes = Arrays.copyOf(bytes, bytes.length * 2);
			}
		}
		if (type instanceof Class<?>) {
			return super.deserialize((Class<?>) type, bytes, size);
		} else {
			throw new IOException("Provided type is not an instace of Class. Unable to deserialize provided object.");
		}
	}
}
