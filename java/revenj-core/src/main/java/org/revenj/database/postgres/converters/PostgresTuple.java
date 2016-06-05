package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresWriter;

import java.util.concurrent.ConcurrentHashMap;

public abstract class PostgresTuple {

	private static final ConcurrentHashMap<String, String> QUOTE_ESCAPE = new ConcurrentHashMap<String, String>();
	private static final String[] SLASHES;

	static {
		SLASHES = new String[20];
		for (int i = 0; i < SLASHES.length; i++) {
			SLASHES[i] = new String(new char[1 << i]).replace('\0', '\\');
		}
	}

	abstract boolean mustEscapeRecord();

	abstract boolean mustEscapeArray();

	abstract void insertRecord(PostgresWriter writer, String escaping, Mapping mappings);

	void insertArray(PostgresWriter writer, String escaping, Mapping mappings) {
		insertRecord(writer, escaping, mappings);
	}

	interface Mapping {
		void map(PostgresWriter writer, char arg);
	}

	public void buildTuple(PostgresWriter sw, boolean quote) {
		if (quote) {
			sw.write('\'');
			insertRecord(sw, "", PostgresTuple::escapeQuote);
			sw.write('\'');
		} else insertRecord(sw, "", null);
	}

	protected static ThreadLocal<PostgresWriter> threadWriter = new ThreadLocal<PostgresWriter>() {
		@Override
		protected PostgresWriter initialValue() {
			return new PostgresWriter();
		}
	};

	public String buildTuple(boolean quote) {
		try (PostgresWriter sw = threadWriter.get()) {
			sw.reset();
			buildTuple(sw, quote);
			return sw.toString();
		}
	}

	static void escapeQuote(PostgresWriter sw, char c) {
		if (c == '\'') {
			sw.write('\'');
		}
		sw.write(c);
	}

	static void escapeBulkCopy(PostgresWriter sw, char c) {
		switch (c) {
			case '\\':
				sw.write("\\\\");
				break;
			case '\t':
				sw.write("\\t");
				break;
			case '\n':
				sw.write("\\n");
				break;
			case '\r':
				sw.write("\\r");
				break;
			case 11:
				sw.write("\\v");
				break;
			case '\b':
				sw.write("\\b");
				break;
			case '\f':
				sw.write("\\f");
				break;
			default:
				sw.write(c);
				break;
		}
	}

	public static String buildQuoteEscape(String escaping) {
		String result = QUOTE_ESCAPE.get(escaping);
		if (result != null) return result;
		StringBuilder sb = new StringBuilder();
		sb.append('"');
		for (int j = escaping.length() - 1; j >= 0; j--) {
			if (escaping.charAt(j) == '1') {
				int len = sb.length();
				for (int i = 0; i < len; i++) {
					sb.insert(i * 2, sb.charAt(i * 2));
				}
			} else sb = new StringBuilder(sb.toString().replace("\\", "\\\\").replace("\"", "\\\""));
		}
		result = sb.toString();
		QUOTE_ESCAPE.put(escaping, result);
		return result;
	}

	public static String buildSlashEscape(int len) {
		if (len < SLASHES.length) {
			return SLASHES[len];
		}
		return new String(new char[1 << len]).replace('\0', '\\');
	}
}
