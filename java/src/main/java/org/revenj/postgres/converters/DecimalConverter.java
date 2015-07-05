package org.revenj.postgres.converters;

import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.PostgresWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class DecimalConverter {

	public static int serialize(char[] buf, int pos, BigDecimal value) {
		if (value == null) return pos;
		String str = value.toString();
		str.getChars(0, str.length(), buf, pos);
		return pos + str.length();
	}

	public static BigDecimal parse(PostgresReader reader, boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return allowNulls ? null : BigDecimal.ZERO;
		}
		return parseDecimal(reader, cur, ')');
	}

	private static BigDecimal parseDecimal(PostgresReader reader, int cur, char matchEnd) throws IOException {
		reader.initBuffer((char) cur);
		reader.fillUntil(',', matchEnd);
		reader.read();
		return reader.bufferToValue(BigDecimal::new);
	}

	public static List<BigDecimal> parseCollection(PostgresReader reader, int context, boolean allowNulls) throws IOException {
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
		BigDecimal defaultValue = allowNulls ? null : BigDecimal.ZERO;
		List<BigDecimal> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				list.add(defaultValue);
				cur = reader.read(4);
			} else {
				list.add(parseDecimal(reader, cur, '}'));
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

	public static PostgresTuple toTuple(BigDecimal value) {
		if (value == null) return null;
		return new DecimalTuple(value);
	}

	static class DecimalTuple extends PostgresTuple {
		private final BigDecimal value;

		public DecimalTuple(BigDecimal value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write(value.toPlainString());
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write(value.toPlainString());
		}

		public String buildTuple(boolean quote) {
			return value.toPlainString();
		}
	}
}
