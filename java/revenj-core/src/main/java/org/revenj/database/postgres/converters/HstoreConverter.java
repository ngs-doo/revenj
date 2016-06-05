package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;
import org.revenj.database.postgres.PostgresWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HstoreConverter {

	private static String toDatabase(Map<String, String> value) {
		if (value.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> kv : value.entrySet()) {
			sb.append('"');
			sb.append(kv.getKey().replace("\\", "\\\\").replace("\"", "\\\""));
			sb.append("\"=>");
			if (kv.getValue() == null) {
				sb.append("NULL, ");
			} else {
				sb.append('"');
				sb.append(kv.getValue().replace("\\", "\\\\").replace("\"", "\\\""));
				sb.append("\", ");
			}
		}
		sb.setLength(sb.length() - 2);
		return sb.toString();
	}

	public static void serializeURI(PostgresBuffer sw, Map<String, String> value) {
		if (value == null) return;
		sw.addToBuffer(toDatabase(value));
	}

	public static void serializeCompositeURI(PostgresBuffer sw, Map<String, String> value) {
		if (value == null) return;
		String str = toDatabase(value);
		StringConverter.serializeCompositeURI(sw, str);
	}

	public static Map<String, String> parse(PostgresReader reader, int context, boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return allowNulls ? null : new HashMap<>(0);
		}
		return parseMap(reader, context, context > 0 ? context << 1 : 1, ')');
	}

	private static Map<String, String> parseMap(
			PostgresReader reader,
			int context,
			int quoteContext,
			char matchEnd) throws IOException {
		int cur = reader.read(quoteContext);
		if (cur == ',' || cur == matchEnd) {
			return new HashMap<>(0);
		}
		Map<String, String> dict = new HashMap<>();
		for (int i = 0; i < context; i++) {
			cur = reader.read();
		}
		reader.initBuffer();
		do {
			do {
				if (cur == '\\' || cur == '"') {
					cur = reader.read(quoteContext);
					if (cur == '=') break;
					for (int i = 0; i < quoteContext - 1; i++) {
						cur = reader.read();
					}
				}
				reader.addToBuffer((char) cur);
				reader.fillUntil('\\', '"');
				cur = reader.read();
			} while (cur != -1);
			String name = reader.bufferToString();
			cur = reader.read(2);
			if (cur == 'N') {
				dict.put(name, null);
				cur = reader.read(4);
				if (cur == '\\' || cur == '"') {
					reader.read(context);
					return dict;
				}
				if (cur == ',' && reader.peek() != ' ') return dict;
				do {
					cur = reader.read();
				}
				while (cur == ' ');
			} else {
				cur = reader.read(quoteContext);
				do {
					if (cur == '\\' || cur == '"') {
						cur = reader.read(quoteContext);
						if (cur == ',') {
							dict.put(name, reader.bufferToString());
							do {
								cur = reader.read();
							}
							while (cur == ' ');
							cur = reader.read(quoteContext);
							break;
						}
						for (int i = 0; i < context; i++) {
							cur = reader.read();
						}
						if (cur == ',' || cur == -1 || cur == matchEnd) {
							dict.put(name, reader.bufferToString());
							return dict;
						}
						for (int i = 0; i < context - 1; i++) {
							cur = reader.read();
						}
					}
					reader.addToBuffer((char) cur);
					reader.fillUntil('\\', '"');
					cur = reader.read();
				} while (cur != -1);
			}
		} while (cur != -1);
		return dict;
	}

	public static List<Map<String, String>> parseCollection(
			PostgresReader reader,
			int context,
			boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean escaped = cur != '{';
		if (escaped) {
			reader.read(context);
		}
		int innerContext = context == 0 ? 1 : context << 1;
		ArrayList<Map<String, String>> list = new ArrayList<>();
		cur = reader.peek();
		if (cur == '}') {
			reader.read();
		}
		while (cur != -1 && cur != '}') {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read(4);
				list.add(allowNulls ? null : new HashMap<>());
			} else {
				list.add(parseMap(reader, innerContext, innerContext << 1, '}'));
				cur = reader.last();
			}
		}
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static PostgresTuple toTuple(Map<String, String> value) {
		if (value == null) return null;
		//TODO empty map
		return new MapTuple(value);
	}

	static class MapTuple extends PostgresTuple {
		private final Map<String, String> value;

		public MapTuple(Map<String, String> value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return true;
		}

		public boolean mustEscapeArray() {
			return true;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			String esc = buildQuoteEscape(escaping);
			String quoteEscape = buildQuoteEscape(escaping + "0");
			String slashEscape = buildSlashEscape(escaping.length() + 1);
			int len = value.size();
			if (mappings != null) {
				for (Map.Entry<String, String> kv : value.entrySet()) {
					len--;
					for (int x = 0; x < esc.length(); x++) {
						mappings.map(sw, esc.charAt(x));
					}
					String key = kv.getKey();
					for (int i = 0; i < key.length(); i++) {
						char c = key.charAt(i);
						if (c == '"') {
							for (int x = 0; x < quoteEscape.length(); x++) {
								mappings.map(sw, quoteEscape.charAt(x));
							}
						} else if (c == '\\') {
							for (int x = 0; x < slashEscape.length(); x++) {
								mappings.map(sw, slashEscape.charAt(x));
							}
						} else mappings.map(sw, c);
					}
					for (int x = 0; x < esc.length(); x++) {
						mappings.map(sw, esc.charAt(x));
					}
					sw.write("=>");
					if (kv.getKey() == null) {
						sw.write("NULL");
					} else {
						for (int x = 0; x < esc.length(); x++) {
							mappings.map(sw, esc.charAt(x));
						}
						String val = kv.getValue();
						for (int i = 0; i < val.length(); i++) {
							char c = val.charAt(i);
							if (c == '"') {
								for (int x = 0; x < quoteEscape.length(); x++) {
									mappings.map(sw, quoteEscape.charAt(x));
								}
							} else if (c == '\\') {
								for (int x = 0; x < slashEscape.length(); x++) {
									mappings.map(sw, slashEscape.charAt(x));
								}
							} else mappings.map(sw, c);
						}
						for (int x = 0; x < esc.length(); x++) {
							mappings.map(sw, esc.charAt(x));
						}
					}
					if (len > 0) {
						sw.write(", ");
					}
				}
			} else {
				for (Map.Entry<String, String> kv : value.entrySet()) {
					len--;
					sw.write(esc);
					String key = kv.getKey();
					for (int i = 0; i < key.length(); i++) {
						char c = key.charAt(i);
						if (c == '"') {
							sw.write(quoteEscape);
						} else if (c == '\\') {
							sw.write(slashEscape);
						} else sw.write(c);
					}
					sw.write(esc);
					sw.write("=>");
					if (kv.getValue() == null) {
						sw.write("NULL");
					} else {
						sw.write(esc);
						String val = kv.getValue();
						for (int i = 0; i < val.length(); i++) {
							char c = val.charAt(i);
							if (c == '"') {
								sw.write(quoteEscape);
							} else if (c == '\\') {
								sw.write(slashEscape);
							} else sw.write(c);
						}
						sw.write(esc);
					}
					if (len > 0) {
						sw.write(", ");
					}
				}
			}
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			insertRecord(sw, escaping, mappings);
		}
	}
}
