package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresWriter;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class DoubleConverter {

	public static void serializeURI(PostgresBuffer sw, double value) throws IOException {
		sw.addToBuffer(Double.toString(value));
	}

	public static void serializeURI(PostgresWriter sw, Double value) throws IOException {
		if (value == null) return;
		sw.write(value.toString());
	}

	public static Double parseNullable(PostgresReader reader) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		double value = parseDouble(reader, cur, ')');
		reader.read();
		return value;
	}

	public static double parse(PostgresReader reader) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return 0;
		}
		double value = parseDouble(reader, cur, ')');
		reader.read();
		return value;
	}

	private static double parseDouble(PostgresReader reader, int cur, char matchEnd) throws IOException {
		reader.initBuffer((char) cur);
		reader.fillUntil(',', matchEnd);
		return Double.parseDouble(reader.bufferToString());
	}

	public static List<Double> parseCollection(PostgresReader reader, int context, boolean allowNulls) throws IOException {
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
		Double defaultValue = allowNulls ? null : 0d;
		List<Double> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read();
				if (cur == 'U') {
					cur = reader.read(3);
					list.add(defaultValue);
				} else {
					list.add(Double.NaN);
					cur = reader.read(2);
				}
			} else {
				list.add(parseDouble(reader, cur, '}'));
				cur = reader.read();
			}
		} while (cur == ',');
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static PostgresTuple toTuple(double value) {
		return new DoubleTuple(value);
	}

	public static PostgresTuple toTuple(Double value) {
		return value == null ? null : new DoubleTuple(value);
	}

	private static class DoubleTuple extends PostgresTuple {
		private final double value;

		DoubleTuple(double value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write(Double.toString(value));
		}

		public String buildTuple(boolean quote) {
			return Double.toString(value);
		}
	}
}
