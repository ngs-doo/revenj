package org.revenj;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class Utils {
	public static final LocalDate MIN_LOCAL_DATE = LocalDate.of(1, 1, 1);
	public static final ZonedDateTime MIN_DATE_TIME = ZonedDateTime.of(MIN_LOCAL_DATE, LocalTime.of(0, 0), ZoneId.of("UTC"));
	public static final UUID MIN_UUID = new java.util.UUID(0L, 0L);
	public static final byte[] EMPTY_BINARY = new byte[0];
	public static final BigDecimal ZERO_0 = BigDecimal.ZERO.setScale(0);
	public static final BigDecimal ZERO_1 = BigDecimal.ZERO.setScale(1);
	public static final BigDecimal ZERO_2 = BigDecimal.ZERO.setScale(2);
	public static final BigDecimal ZERO_3 = BigDecimal.ZERO.setScale(3);

	private static final ConcurrentMap<String, GenericType> typeCache = new ConcurrentHashMap<>();

	private static class GenericType implements ParameterizedType {

		private final String name;
		private final Type raw;
		private final Type[] arguments;

		public GenericType(String name, Type raw, Type... arguments) {
			this.name = name;
			this.raw = raw;
			this.arguments = arguments;
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
			found = new GenericType(name, container, arguments);
			typeCache.put(name, found);
		}
		return found;
	}

}
