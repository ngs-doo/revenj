package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresWriter;
import org.revenj.database.postgres.PostgresReader;

import java.util.ArrayList;
import java.util.List;

public abstract class BoolConverter {

	private static final char[] TRUE = "true".toCharArray();
	private static final char[] FALSE = "false".toCharArray();

	public static void serializeURI(PostgresBuffer sw, Boolean value) {
		if (value == null) return;
		if (value) {
			sw.addToBuffer(TRUE);
		} else {
			sw.addToBuffer(FALSE);
		}
	}

	public static Boolean parseNullable(PostgresReader reader) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		reader.read();
		return cur == 't';
	}

	public static boolean parse(PostgresReader reader) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return false;
		}
		reader.read();
		return cur == 't';
	}

	public static List<Boolean> parseCollection(PostgresReader reader, int context, boolean allowNulls) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean escaped = cur != '{';
		if (escaped) {
			reader.read(context);
		}
		List<Boolean> list = new ArrayList<>();
		cur = reader.peek();
		if (cur == '}') {
			reader.read();
		}
		Boolean defaultValue = allowNulls ? null : false;
		while (cur != -1 && cur != '}') {
			cur = reader.read();
			if (cur == 't') {
				list.add(true);
			} else if (cur == 'f') {
				list.add(false);
			} else {
				reader.read(3);
				list.add(defaultValue);
			}
			cur = reader.read();
		}
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static PostgresTuple toTuple(Boolean value) {
		if (value == null) return null;
		return new BoolTuple(value);
	}

	public static PostgresTuple toTuple(boolean value) {
		return new BoolTuple(value);
	}

	private static class BoolTuple extends PostgresTuple {
		private final char value;

		BoolTuple(boolean value) {
			this.value = value ? 't' : 'f';
		}

		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write(value);
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write(value);
		}

		public String buildTuple(boolean quote) {
			return quote ? "'" + value + "'" : String.valueOf(value);
		}
	}
}
