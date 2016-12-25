package org.revenj.server.servlet;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.revenj.extensibility.Container;
import org.revenj.patterns.DomainModel;
import org.revenj.patterns.Query;
import org.revenj.patterns.Specification;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.Serialization;
import org.revenj.serialization.WireSerialization;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ServerService;
import org.revenj.server.TestProcessingEngine;
import org.revenj.server.commands.ExecuteService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Type;
import java.security.Principal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class ServiceTest extends Mockito {

	public static class MyInputArg {
		public String text;
		public int number;
	}

	public static class MyResponseArg {
		public String message;
		public long result;
	}

	public static class MyService implements ServerService<MyInputArg, MyResponseArg> {
		public MyResponseArg execute(MyInputArg input) {
			MyResponseArg response = new MyResponseArg();
			response.message = "message: " + input.text;
			response.result = input.number * 2;
			return response;
		}
	}

	public static class OutStream extends ServletOutputStream {
		public final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
		}

		@Override
		public void write(int b) throws IOException {
			stream.write(b);
		}
	}

	public static class InStream extends ServletInputStream {
		private final byte[] arg;
		private int position;

		public InStream(byte[] arg) {
			this.arg = arg;
		}

		@Override
		public boolean isFinished() {
			return true;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setReadListener(ReadListener readListener) {
		}

		@Override
		public int read() throws IOException {
			return position < arg.length ? arg[position++] : -1;
		}
	}

	static class SerializationMock implements WireSerialization {
		private final JacksonSerialization jackson = new JacksonSerialization(null, Optional.empty());

		@Override
		public String serialize(Object value, OutputStream stream, String accept) throws IOException {
			jackson.serialize(value, stream);
			return "application/json";
		}

		@Override
		public Object deserialize(Type type, byte[] content, int length, String contentType) throws IOException {
			return jackson.deserialize(type, content, length);
		}

		@Override
		public Object deserialize(Type type, InputStream stream, String contentType) throws IOException {
			return jackson.deserialize(type, stream);
		}

		static class PassThroughSerialization implements Serialization<Object> {

			@Override
			public Object serialize(Type type, Object value) {
				return value;
			}

			@Override
			public Object deserialize(Type type, Object data) throws IOException {
				return data;
			}
		}

		@Override
		public <TFormat> Optional<Serialization<TFormat>> find(Class<TFormat> format) {
			if (format == Object.class) return Optional.of((Serialization) new PassThroughSerialization());
			return Optional.of((Serialization) jackson);
		}
	}

	static class PermissionManagerMock implements PermissionManager {

		@Override
		public boolean canAccess(String identifier, Principal user) {
			return true;
		}

		@Override
		public <T, S extends T> Query<S> applyFilters(Class<T> manifest, Principal user, Query<S> data) {
			return data;
		}

		@Override
		public <T, S extends T> List<S> applyFilters(Class<T> manifest, Principal user, List<S> data) {
			return data;
		}

		@Override
		public <T> Closeable registerFilter(Class<T> manifest, Specification<T> filter, String role, boolean inverse) {
			return null;
		}
	}

	@Test
	public void serviceTarget() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		DomainModel model = mock(DomainModel.class);
		Container container = mock(Container.class);
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		PermissionManager permissions = new PermissionManagerMock();
		WireSerialization serialization = new SerializationMock();

		InStream inputStream = new InStream("{\"text\":\"some\",\"number\":5}".getBytes());
		OutStream outputStream = new OutStream();
		when(request.getPathInfo()).thenReturn("/execute/org.revenj.server.servlet.ServiceTest$MyService");
		when(request.getInputStream()).thenReturn(inputStream);
		when(request.getCharacterEncoding()).thenReturn(null);
		when(dataSource.getConnection()).thenReturn(connection);
		when(response.getOutputStream()).thenReturn(outputStream);
		when(container.createScope()).thenReturn(container);
		when(container.resolve((Type) MyService.class)).thenReturn(new MyService());

		ProcessingEngine engine = TestProcessingEngine.create(
			container,
			dataSource,
			serialization,
			permissions,
			new ExecuteService(Thread.currentThread().getContextClassLoader(), permissions));
		StandardServlet servlet = new StandardServlet(model, engine, serialization);
		servlet.doPost(request, response);

		Assert.assertEquals("{\"message\":\"message: some\",\"result\":10}", outputStream.stream.toString("UTF-8"));
	}
}
