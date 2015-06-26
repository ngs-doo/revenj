package org.revenj.postgres.converters;

import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.PostgresWriter;

import java.util.ArrayList;
import java.util.List;

public abstract class LongConverter {

	public static Long parseNullable(PostgresReader reader) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		return parseLong(reader, cur, ')');
	}

	public static long parse(PostgresReader reader) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return 0L;
		}
		return parseLong(reader, cur, ')');
	}

	private static long parseLong(PostgresReader reader, int cur, char matchEnd) {
		long res = 0;
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

	public static List<Long> parseNullableCollection(PostgresReader reader, int context) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean espaced = cur != '{';
		if (espaced) {
			reader.read(context);
		}
		cur = reader.peek();
		if (cur == '}') {
			if (espaced) {
				reader.read(context + 2);
			} else {
				reader.read(2);
			}
			return new ArrayList<>(0);
		}
		List<Long> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				list.add(null);
				cur = reader.read(4);
			} else {
				list.add(parseLong(reader, cur, '}'));
				cur = reader.last();
			}
		} while (cur == ',');
		if (espaced) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static List<Long> parseCollection(PostgresReader reader, int context) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean espaced = cur != '{';
		if (espaced) {
			reader.read(context);
		}
		cur = reader.peek();
		if (cur == '}') {
			if (espaced) {
				reader.read(context + 2);
			} else {
				reader.read(2);
			}
			return new ArrayList<>(0);
		}
		List<Long> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				list.add(0L);
				cur = reader.read(4);
			} else {
				list.add(parseLong(reader, cur, '}'));
				cur = reader.last();
			}
		} while (cur == ',');
		if (espaced) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	private static final PostgresTuple MAX_TUPLE = new ValueTuple("-9223372036854775808", false, false);

	public static PostgresTuple toTuple(long value) {
		if (value == Long.MIN_VALUE) {
			return MAX_TUPLE;
		}
		return new LongTuple(value);
	}

	static class LongTuple extends PostgresTuple {
		private final long value;

		public LongTuple(long value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			int offset = NumberConverter.serialize(value, sw.tmp, 0);
			sw.write(sw.tmp, offset, 21);
		}

		public String buildTuple(boolean quote) {
			return Long.toString(value);
		}
	}
}
