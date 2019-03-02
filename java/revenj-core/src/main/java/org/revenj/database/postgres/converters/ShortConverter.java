package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ShortConverter {

	public static void serializeURI(PostgresBuffer sw, short value) throws IOException {
		int offset = NumberConverter.serialize(value, sw.getTempBuffer());
		sw.addToBuffer(sw.getTempBuffer(), offset, 6);
	}

	public static void serializeURI(PostgresBuffer sw, Short value) throws IOException {
		if (value == null) return;
		serializeURI(sw, value.shortValue());
	}

	public static Short parseNullable(PostgresReader reader) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		return parseShort(reader, cur, ')');
	}

	public static short parse(PostgresReader reader) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return 0;
		}
		return parseShort(reader, cur, ')');
	}

	private static short parseShort(PostgresReader reader, int cur, char matchEnd) {
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
		return (short) res;
	}

	public static List<Short> parseCollection(PostgresReader reader, int context, boolean allowNulls) {
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
		Short defaultValue = allowNulls ? null : (short)0;
		List<Short> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				list.add(defaultValue);
				cur = reader.read(4);
			} else {
				list.add(parseShort(reader, cur, '}'));
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

	public static PostgresTuple toTuple(Short value) {
		return value == null ? null : IntConverter.toTuple(value);
	}

	public static PostgresTuple toTuple(short value) {
		return IntConverter.toTuple(value);
	}
}
