package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;
import org.revenj.database.postgres.PostgresWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class IntConverter {

	public static void serializeURI(PostgresBuffer sw, int value) throws IOException {
		if (value == Integer.MIN_VALUE) {
			sw.addToBuffer("-2147483648");
		} else {
			int offset = NumberConverter.serialize(value, sw.getTempBuffer());
			sw.addToBuffer(sw.getTempBuffer(), offset, 11);
		}
	}

	public static void serializeURI(PostgresBuffer sw, Integer value) throws IOException {
		if (value == null) return;
		serializeURI(sw, value.intValue());
	}

	public static Integer parseNullable(PostgresReader reader) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		return parseInt(reader, cur, ')');
	}

	public static int parse(PostgresReader reader) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return 0;
		}
		return parseInt(reader, cur, ')');
	}

	private static int parseInt(PostgresReader reader, int cur, char matchEnd) {
		int res = 0;
		if (cur == '-') {
			cur = reader.read();
			do {
				res = (res << 3) + (res << 1) - (cur - 48);
				cur = reader.read();
			} while (cur != -1 && cur != ',' && cur != matchEnd);
		} else {
			do {
				res = (res << 3) + (res << 1) + (cur - 48);
				cur = reader.read();
			} while (cur != -1 && cur != ',' && cur != matchEnd);
		}
		return res;
	}

	public static List<Integer> parseCollection(PostgresReader reader, int context, boolean allowNulls) {
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
				reader.read(context + 2);
			} else {
				reader.read(2);
			}
			return new ArrayList<>(0);
		}
		Integer defaultValue = allowNulls ? null : 0;
		List<Integer> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				list.add(defaultValue);
				cur = reader.read(4);
			} else {
				list.add(parseInt(reader, cur, '}'));
				cur = reader.last();
			}
		} while (cur == ',');
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	private static final PostgresTuple MIN_TUPLE = new ValueTuple("-2147483648", false, false);

	public static PostgresTuple toTuple(Integer value) {
		if (value == null) return null;
		return toTuple(value.intValue());
	}

	public static PostgresTuple toTuple(int value) {
		if (value == Integer.MIN_VALUE) {
			return MIN_TUPLE;
		}
		return new IntTuple(value);
	}

	private static class IntTuple extends PostgresTuple {
		private final int value;

		IntTuple(int value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			int offset = NumberConverter.serialize(value, sw.tmp);
			sw.write(sw.tmp, offset, 11);
		}

		public String buildTuple(boolean quote) {
			return Integer.toString(value);
		}
	}
}
