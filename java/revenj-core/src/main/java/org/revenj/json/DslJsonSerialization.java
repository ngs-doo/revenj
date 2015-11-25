package org.revenj.json;

import com.dslplatform.json.*;
import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.Serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;

public class DslJsonSerialization extends DslJson<ServiceLocator> implements Serialization<String> {

	public DslJsonSerialization(final ServiceLocator locator, Optional<Fallback<ServiceLocator>> fallback) {
		super(locator, false, true, false, fallback.orElse(null), ServiceLoader.load(Configuration.class));

		registerReader(LocalDate.class, JavaTimeConverter.LocalDateReader);
		registerWriter(LocalDate.class, JavaTimeConverter.LocalDateWriter);
		registerReader(LocalDateTime.class, JavaTimeConverter.LocalDateTimeReader);
		registerWriter(LocalDateTime.class, JavaTimeConverter.LocalDateTimeWriter);
		registerReader(OffsetDateTime.class, JavaTimeConverter.DateTimeReader);
		registerWriter(OffsetDateTime.class, JavaTimeConverter.DateTimeWriter);
		registerReader(java.sql.Date.class, rdr -> java.sql.Date.valueOf(JavaTimeConverter.deserializeLocalDate(rdr)));
		registerWriter(java.sql.Date.class, (writer, value) -> JavaTimeConverter.serialize(value.toLocalDate(), writer));
		registerReader(java.sql.Timestamp.class, rdr -> java.sql.Timestamp.from(JavaTimeConverter.deserializeDateTime(rdr).toInstant()));
		registerWriter(java.sql.Timestamp.class, (writer, value) -> JavaTimeConverter.serialize(OffsetDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()), writer));
		registerReader(java.util.Date.class, rdr -> java.util.Date.from(JavaTimeConverter.deserializeDateTime(rdr).toInstant()));
		registerWriter(java.util.Date.class, (writer, value) -> JavaTimeConverter.serialize(OffsetDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()), writer));
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
		//TODO: use underlying stream deserializer when it starts supporting autogrowing buffer
		return super.deserialize(type, bytes, size);
	}
}
