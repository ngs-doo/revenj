package org.revenj.postgres.converters;

import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.PostgresWriter;

import java.io.IOException;
import java.util.HashMap;
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

	public static int serializeURI(char[] buf, int pos, Map<String, String> value) {
		if (value == null) return pos;
		String str = toDatabase(value);
		str.getChars(0, str.length(), buf, pos);
		return pos + str.length();
	}

	public static int serializeCompositeURI(char[] buf, int pos, Map<String, String> value) {
		if (value == null) return pos;
		String str = toDatabase(value);
		return StringConverter.serializeCompositeURI(buf, pos, str);
	}

	public static Map<String, String> parse(PostgresReader reader, int context, boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return allowNulls ? null : new HashMap<>(0);
		}
		reader.read();
		return parseMap(reader, context, context > 0 ? context << 1 : 1, cur, ')');
	}

	private static Map<String, String> parseMap(
			PostgresReader reader,
			int context,
			int quoteContext,
			int cur,
			char matchEnd) throws IOException {
		cur = reader.read(quoteContext);
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

	public static PostgresTuple toTuple(Map<String, String> value) {
		if (value == null) return null;
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
