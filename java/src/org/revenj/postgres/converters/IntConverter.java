package org.revenj.postgres.converters;

import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.PostgresWriter;

import java.util.ArrayList;
import java.util.List;

public abstract class IntConverter {

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

	public static List<Integer> parseNullableCollection(PostgresReader reader, int context) {
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
		List<Integer> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				list.add(null);
				cur = reader.read(4);
			} else {
				list.add(parseInt(reader, cur, '}'));
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

	public static List<Integer> parseCollection(PostgresReader reader, int context) {
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
		List<Integer> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				list.add(0);
				cur = reader.read(4);
			} else {
				list.add(parseInt(reader, cur, '}'));
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

	public static PostgresTuple toTuple(int value) {
		return new IntTuple(value);
	}

	static class IntTuple extends PostgresTuple {
		private final int value;

		public IntTuple(int value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			int len = NumberConverter.serialize(value, sw.tmp, 0);
			sw.writeBuffer(len);
		}

		public String buildTuple(boolean quote) {
			return Integer.toString(value);
		}
	}
}
