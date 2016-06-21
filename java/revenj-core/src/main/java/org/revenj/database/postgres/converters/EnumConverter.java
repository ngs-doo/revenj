package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresWriter;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class EnumConverter {

	public static void serializeURI(PostgresBuffer sw, Enum value) {
		if (value == null) return;
		sw.addToBuffer(value.name());
	}

	public static <T extends Enum> List<T> parseCollection(
			PostgresReader reader,
			int context,
			T defaultValue,
			Function<PostgresReader, T> factory) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean escaped = cur != '{';
		if (escaped) {
			reader.read(context);
		}
		cur = reader.peek();
		if (cur == '}') {
			if (escaped) {
				reader.read(context);
			} else {
				reader.read(2);
			}
			return new ArrayList<>(0);
		}
		int innerContext = context == 0 ? 1 : context << 1;
		List<T> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == '"' || cur == '\\') {
				cur = reader.read(innerContext);
				reader.initBuffer((char) cur);
				reader.fillUntil('\\', '"');
				list.add(factory.apply(reader));
				cur = reader.read(innerContext + 1);
			} else {
				reader.initBuffer((char) cur);
				reader.fillUntil(',', '}');
				cur = reader.read();
				if (reader.bufferMatches("NULL")) {
					list.add(defaultValue);
				} else {
					list.add(factory.apply(reader));
				}
			}
		} while (cur == ',');
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static <T extends Enum> PostgresTuple toTuple(T value) {
		if (value == null) return null;
		return new EnumTuple(value.name());
	}

	private static class EnumTuple extends PostgresTuple {
		private final String value;
		private final boolean escapeArray;

		EnumTuple(String value) {
			this.value = value;
			escapeArray = "NULL".equals(value);
		}

		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return escapeArray;
		}

		public String buildTuple(boolean quote) {
			return quote ? "'" + value + "'" : value;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write(value);
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			if (mappings != null) {
				for (int x = 0; x < value.length(); x++) {
					mappings.map(sw, value.charAt(x));
				}
			} else sw.write(value);
		}
	}
}
