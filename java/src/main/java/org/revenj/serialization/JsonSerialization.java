package org.revenj.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.revenj.patterns.Bytes;
import org.revenj.patterns.Serialization;
import org.revenj.patterns.ServiceLocator;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class JsonSerialization implements Serialization<String>, Serialization.Converter<String> {

	private final ObjectMapper mapper;

	public JsonSerialization() {
		mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public <T> Bytes serialize(T value) {
		try {
			byte[] content = mapper.writeValueAsBytes(value);
			return new Bytes(content, content.length);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public Converter<String> getConverter() {
		return this;
	}

	@Override
	public Object deserialize(Type type, Bytes data, ServiceLocator locator) throws IOException {
		JavaType javaType = mapper.getTypeFactory().constructType(type);
		return mapper.readValue(data.content, 0, data.length, javaType);
	}

	@Override
	public String convert(Bytes input) {
		return input.toUtf8();
	}

	private static Charset utf8 = Charset.forName("UTF-8");

	@Override
	public Bytes convert(String input) {
		byte[] content = input.getBytes(utf8);
		return new Bytes(content, content.length);
	}
}
