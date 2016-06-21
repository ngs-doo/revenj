package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresWriter;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class FloatConverter {

	public static void serializeURI(PostgresBuffer sw, float value) throws IOException {
		sw.addToBuffer(Float.toString(value));
	}

	public static void serializeURI(PostgresBuffer sw, Float value) throws IOException {
		if (value == null) return;
		sw.addToBuffer(Float.toString(value));
	}

	public static Float parseNullable(PostgresReader reader) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		float value = parseFloat(reader, cur, ')');
		reader.read();
		return value;
	}

	public static float parse(PostgresReader reader) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return 0;
		}
		float value = parseFloat(reader, cur, ')');
		reader.read();
		return value;
	}

	private static float parseFloat(PostgresReader reader, int cur, char matchEnd) throws IOException {
		reader.initBuffer((char) cur);
		reader.fillUntil(',', matchEnd);
		return Float.parseFloat(reader.bufferToString());
	}

	public static List<Float> parseCollection(PostgresReader reader, int context, boolean allowNulls) throws IOException {
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
		Float defaultValue = allowNulls ? null : 0f;
		List<Float> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read();
				if (cur == 'U') {
					cur = reader.read(3);
					list.add(defaultValue);
				} else {
					list.add(Float.NaN);
					cur = reader.read(2);
				}
			} else {
				list.add(parseFloat(reader, cur, '}'));
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

	public static PostgresTuple toTuple(float value) {
		return new FloatTuple(value);
	}

	public static PostgresTuple toTuple(Float value) {
		return value == null ? null : new FloatTuple(value);
	}

	private static class FloatTuple extends PostgresTuple {
		private final float value;

		FloatTuple(float value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write(Float.toString(value));
		}

		public String buildTuple(boolean quote) {
			return Float.toString(value);
		}
	}
}
