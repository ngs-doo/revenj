package org.revenj;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import rx.internal.util.unsafe.ConcurrentCircularArrayQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.time.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public abstract class Utils {
	public static final LocalDate MIN_LOCAL_DATE = LocalDate.of(1, 1, 1);
	public static final LocalDateTime MIN_LOCAL_DATE_TIME = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0);
	public static final OffsetDateTime MIN_DATE_TIME = OffsetDateTime.of(MIN_LOCAL_DATE_TIME, ZoneOffset.UTC);
	public static final UUID MIN_UUID = new java.util.UUID(0L, 0L);
	public static final byte[] EMPTY_BINARY = new byte[0];
	public static final BigDecimal ZERO_0 = BigDecimal.ZERO.setScale(0);
	public static final BigDecimal ZERO_1 = BigDecimal.ZERO.setScale(1);
	public static final BigDecimal ZERO_2 = BigDecimal.ZERO.setScale(2);
	public static final BigDecimal ZERO_3 = BigDecimal.ZERO.setScale(3);
	public static final BigDecimal ZERO_4 = BigDecimal.ZERO.setScale(4);
	public static final InetAddress LOOPBACK = InetAddress.getLoopbackAddress();

	private static final ConcurrentMap<String, GenericType> typeCache = new ConcurrentHashMap<>();

	private static final DocumentBuilder documentBuilder;
	private static final ArrayBlockingQueue<DocumentBuilder> documentBuilders;

	private static DocumentBuilder initializeBuilder() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setFeature("http://xml.org/sax/features/namespaces", false);
			dbf.setFeature("http://xml.org/sax/features/validation", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			dbf.setNamespaceAware(false);
			return dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	static {
		int cpu = Runtime.getRuntime().availableProcessors();
		documentBuilder = initializeBuilder();
		documentBuilders = new ArrayBlockingQueue<>(cpu);
		for (int i = 0; i < cpu; i++) {
			documentBuilders.offer(Utils.initializeBuilder());
		}
	}

	public static Document newDocument() {
		return documentBuilder.newDocument();
	}

	public static Document parse(InputSource source) throws IOException {
		DocumentBuilder builder = documentBuilders.poll();
		if (builder == null) {
			builder = initializeBuilder();
		}
		try {
			return builder.parse(source);
		} catch (SAXException ex) {
			throw new IOException(ex);
		} finally {
			documentBuilders.offer(builder);
		}
	}

	private static class GenericType implements ParameterizedType {

		private final String name;
		private final Type raw;
		private final Type[] arguments;

		public GenericType(String name, Type raw, Type[] arguments) {
			this.name = name;
			this.raw = raw;
			this.arguments = arguments;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(arguments) ^ raw.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) other;
				return raw.equals(pt.getRawType()) && Arrays.equals(arguments, pt.getActualTypeArguments());
			}
			return false;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return arguments;
		}

		@Override
		public Type getRawType() {
			return raw;
		}

		@Override
		public Type getOwnerType() {
			return null;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static ParameterizedType makeGenericType(Class<?> container, Type argument, Type... arguments) {
		StringBuilder sb = new StringBuilder();
		sb.append(container.getTypeName());
		sb.append("<");
		sb.append(argument.getTypeName());
		for (Type arg : arguments) {
			sb.append(", ");
			sb.append(arg.getTypeName());
		}
		sb.append(">");
		String name = sb.toString();
		GenericType found = typeCache.get(name);
		if (found == null) {
			Type[] genericArgs = new Type[arguments.length + 1];
			genericArgs[0] = argument;
			for (int i = 0; i < arguments.length; i++) {
				genericArgs[i + 1] = arguments[i];
			}
			found = new GenericType(name, container, genericArgs);
			typeCache.put(name, found);
		}
		return found;
	}
}
