package org.revenj.serialization;

import org.revenj.patterns.Bytes;
import org.revenj.patterns.Serialization;
import org.revenj.patterns.ServiceLocator;
import org.revenj.patterns.WireSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Optional;

public final class RevenjSerialization implements WireSerialization {
	private final JacksonSerialization json;
	private final PassThroughSerialization passThrough;

	public RevenjSerialization(ServiceLocator locator) {
		this.json = new JacksonSerialization(locator);
		this.passThrough = new PassThroughSerialization();
	}

	@Override
	public Bytes serialize(Object value, String contentType) {
		try {
			//if (contentType == null || "application/json".equals(contentType)) {
			byte[] content = json.serializeAsBytes(value);
			return new Bytes(content, content.length);
			//}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void serialize(Object value, OutputStream stream, String contentType) throws IOException {
		json.serializeTo(value, stream);
	}

	@Override
	public Object deserialize(Type type, Bytes data, String accept) throws IOException {
		return json.deserialize(type, data.content, data.length);
	}

	@Override
	public Object deserialize(Type type, InputStream stream, String accept) throws IOException {
		//if (contentType == null || "application/json".equals(contentType)) {
		return json.deserialize(type, stream);
		//}
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
