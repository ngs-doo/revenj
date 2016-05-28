package org.revenj.server.servlet;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.revenj.serialization.json.DslJsonSerialization;
import org.revenj.serialization.Serialization;
import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.WireSerialization;
import org.revenj.serialization.xml.XmlJaxbSerialization;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Optional;

final class RevenjSerialization implements WireSerialization {
	private final DslJsonSerialization json;
	private final XmlJaxbSerialization xml;
	private final PassThroughSerialization passThrough;

	public RevenjSerialization(ServiceLocator locator, XmlJaxbSerialization xml) {
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

			@Override
			public Object deserialize(ServiceLocator serviceLocator, Type manifest, InputStream stream) throws IOException {
				return jackson.deserialize(manifest, stream);
			}
		}));
		this.passThrough = new PassThroughSerialization();
		this.xml = xml;
	}

	private static final ThreadLocal<JsonWriter> threadWriter = new ThreadLocal<JsonWriter>() {
		@Override
		protected JsonWriter initialValue() {
			return new JsonWriter();
		}
	};
	private static final ThreadLocal<byte[]> threadBuffer = new ThreadLocal<byte[]>() {
		@Override
		protected byte[] initialValue() {
			return new byte[65536];
		}
	};

	@Override
	public String serialize(Object value, OutputStream stream, String accept) throws IOException {
		if (accept != null && accept.startsWith("application/xml")) {
			xml.serializeTo(value, stream);
			return "application/xml; charset=UTF-8";
		}
		JsonWriter writer = threadWriter.get();
		writer.reset();
		json.serialize(writer, value);
		writer.toStream(stream);
		return "application/json";
	}

	@Override
	public Object deserialize(Type type, byte[] content, int length, String contentType) throws IOException {
		if (contentType != null && contentType.startsWith("application/xml")) {
			ByteArrayInputStream is = new ByteArrayInputStream(content, 0, length);
			return xml.deserialize(type, is);
		}
		return json.deserialize(type, content, length);
	}

	@Override
	public Object deserialize(Type type, InputStream stream, String contentType) throws IOException {
		if (contentType != null && contentType.startsWith("application/xml")) {
			return xml.deserialize(type, stream);
		}
		return json.deserialize(type, stream, threadBuffer.get());
	}

	@Override
	public <TFormat> Optional<Serialization<TFormat>> find(Class<TFormat> format) {
		if (Object.class.equals(format)) {
			return Optional.of((Serialization) passThrough);
		} else if (String.class.equals(format)) {
			return Optional.of((Serialization) json);
		} else if (Element.class.equals(format)) {
			return Optional.of((Serialization) xml);
		}
		return Optional.empty();
	}
}
