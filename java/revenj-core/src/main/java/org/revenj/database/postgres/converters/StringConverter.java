package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class StringConverter {

	public static void serializeURI(PostgresBuffer sw, String value) {
		if (value == null) return;
		sw.addToBuffer(value);
	}

	public static void serializeCompositeURI(PostgresBuffer sw, String value) {
		if (value == null) return;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '\\' || c == '/') {
				sw.addToBuffer('\\');
			}
			sw.addToBuffer(c);
		}
	}

	public static void skip(PostgresReader reader, int context) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return;
		}
		if (cur != '"' && cur != '\\') {
			reader.initBuffer();
			reader.fillUntil(',', ')');
			reader.read();
		} else {
			cur = reader.read(context);
			while (cur != -1) {
				if (cur == '\\' || cur == '"') {
					cur = reader.read(context);
					if (cur == ',' || cur == ')') {
						return;
					}
					cur = reader.read(context);
				} else cur = reader.read();
			}
			throw new IOException("Unable to find end of string");
		}
	}

	public static String parse(PostgresReader reader, int context, boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return allowNulls ? null : "";
		}
		if (cur != '"' && cur != '\\') {
			reader.initBuffer((char) cur);
			reader.fillUntil(',', ')');
			reader.read();
			return reader.bufferToString();
		}
		return parseEscapedString(reader, context, ')');
	}

	static String parseEscapedString(PostgresReader reader, int context, char matchEnd) throws IOException {
		int cur = reader.read(context);
		reader.initBuffer();
		do {
			if (cur == '\\' || cur == '"') {
				cur = reader.read(context);
				if (cur == ',' || cur == matchEnd) {
					return reader.bufferToString();
				}
				for (int i = 0; i < context - 1; i++) {
					cur = reader.read();
				}
			}
			reader.addToBuffer((char) cur);
			reader.fillUntil('\\', '"');
			cur = reader.read();
		} while (cur != -1);
		throw new IOException("Unable to find end of string");
	}

	public static List<String> parseCollection(PostgresReader reader, int context, boolean allowNull) throws IOException {
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
        int innerContext = context << 1;
		List<String> list = new ArrayList<>();
		String emptyCol = allowNull ? null : "";
		do {
			cur = reader.read();
			if (cur == '"' || cur == '\\') {
				list.add(parseEscapedString(reader, innerContext, '}'));
				cur = reader.last();
			} else {
				reader.initBuffer((char) cur);
				reader.fillUntil(',', '}');
				cur = reader.read();
				if (reader.bufferMatches("NULL")) {
					list.add(emptyCol);
				} else {
					list.add(reader.bufferToString());
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

	public static PostgresTuple toTuple(String value) {
		return ValueTuple.from(value);
	}

}
