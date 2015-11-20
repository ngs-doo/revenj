package org.revenj.spring;

import com.dslplatform.json.JsonObject;
import com.dslplatform.json.JsonWriter;
import org.revenj.json.DslJsonSerialization;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

public class DslJsonMessageConverter extends AbstractGenericHttpMessageConverter<Object> implements GenericHttpMessageConverter<Object> {
	private final DslJsonSerialization serializer;

	public DslJsonMessageConverter(DslJsonSerialization serializer) {
		super(MediaType.APPLICATION_JSON);
		this.serializer = serializer;
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
			return new byte[1024 * 1024];
		}
	};

	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return JsonObject.class.isAssignableFrom(clazz) && canRead(mediaType);
	}

	public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
		//TODO: check if reader for type exists
		return type instanceof Class<?> && JsonObject.class.isAssignableFrom((Class<?>) type)
				&& (contextClass == null || contextClass.isArray() || Collection.class.isAssignableFrom(contextClass));
	}

	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		//TODO: check if writer for type exists
		return JsonObject.class.isAssignableFrom(clazz) && canWrite(mediaType);
	}

	protected boolean supports(Class<?> clazz) {
		return JsonObject.class.isAssignableFrom(clazz);
	}

	protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return serializer.deserialize(clazz, inputMessage.getBody(), threadBuffer.get());
	}

	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return serializer.deserialize(contextClass, inputMessage.getBody(), threadBuffer.get());
	}

	protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		JsonWriter writer = threadWriter.get();
		writer.reset();
		serializer.serialize(writer, type, object);
		writer.toStream(outputMessage.getBody());
	}

	protected MediaType getDefaultContentType(Object object) throws IOException {
		return MediaType.APPLICATION_JSON;
	}
}
