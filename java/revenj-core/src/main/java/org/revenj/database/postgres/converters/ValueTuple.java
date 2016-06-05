package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresWriter;

public class ValueTuple extends PostgresTuple {
	//Postgres whitespace definition
	//private static final char[] WHITESPACE = new char[]{(char) 9, (char) 10, (char) 11, (char) 12, (char) 13, (char) 32, (char) 160, (char) 5760, (char) 8192, (char) 8193, (char) 8194, (char) 8195, (char) 8196, (char) 8197, (char) 8198, (char) 8199, (char) 8200, (char) 8201, (char) 8202, (char) 8232, (char) 8233, (char) 8239, (char) 8287, (char) 12288};

	public static final PostgresTuple EMPTY;

	static {
		EMPTY = new EmptyValueTuple();
	}

	private final String value;
	private final boolean hasMarkers;

	private final boolean escapeRecord;
	private final boolean escapeArray;

	public ValueTuple(String value) {
		this.value = value;
		if (value != null) {
			boolean hasMarkers = false;
			boolean escapeRecord = value.length() == 0;
			boolean escapeArray = value.length() == 0 || value.equals("NULL");
			boolean hasWhitespace = false;
			for(int i = 0; i < value.length(); i ++) {
				char c = value.charAt(i);
				if (c == '\\' || c == '"') {
					hasMarkers = escapeRecord = escapeArray = true;
					break;
				} else if (c == ',') {
					escapeRecord = escapeArray = true;
				} else if (c == '(' || c == ')') {
					escapeRecord = true;
				} else if (c == '{' || c == '}') {
					escapeArray = true;
				} else if (!hasWhitespace) {
					hasWhitespace = Character.isWhitespace(c);
				}
			}
			this.hasMarkers = hasMarkers;
			//TODO: postgres can actually cope with whitespace... think about changing this
			this.escapeRecord = hasMarkers || escapeRecord || hasWhitespace;
			this.escapeArray = hasMarkers || escapeArray || hasWhitespace;
		} else {
			escapeRecord = false;
			escapeArray = true;
			hasMarkers = false;
		}
	}

	public static PostgresTuple from(String value) {
		if (value == null) return null;
		if (value.length() == 0) return EMPTY;
		return new ValueTuple(value);
	}

	public ValueTuple(String value, boolean record, boolean array) {
		this.value = value;
		this.escapeRecord = record;
		this.escapeArray = array;
		this.hasMarkers = record || array;
	}

	public boolean mustEscapeRecord() {
		return escapeRecord;
	}

	public boolean mustEscapeArray() {
		return escapeArray;
	}

	public String buildTuple(boolean quote) {
		if (value == null) return "NULL";
		return quote ? "'" + value.replace("'", "''") + "'" : value;
	}

	private void escape(PostgresWriter sw, String escaping, Mapping mappings) {
		String quoteEscape = null;
		String slashEscape = null;
		if (mappings != null) {
			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if (c == '"') {
					quoteEscape = quoteEscape != null ? quoteEscape : buildQuoteEscape(escaping);
					for (int x = 0; x < quoteEscape.length(); x++) {
						mappings.map(sw, quoteEscape.charAt(x));
					}
				} else if (c == '\\') {
					slashEscape = slashEscape != null ? slashEscape : buildSlashEscape(escaping.length());
					for (int x = 0; x < slashEscape.length(); x++) {
						mappings.map(sw, slashEscape.charAt(x));
					}
				} else mappings.map(sw, c);
			}
		} else {
			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if (c == '"') {
					quoteEscape = quoteEscape != null ? quoteEscape : buildQuoteEscape(escaping);
					sw.write(quoteEscape);
				} else if (c == '\\') {
					slashEscape = slashEscape != null ? slashEscape : buildSlashEscape(escaping.length());
					sw.write(slashEscape);
				} else sw.write(c);
			}
		}
	}

	public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
		if (hasMarkers) escape(sw, escaping, mappings);
		else {
			if (mappings != null) {
				for (int x = 0; x < value.length(); x++) {
					mappings.map(sw, value.charAt(x));
				}
			} else if (value != null) sw.write(value);
		}
	}

	public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
		if (value == null) sw.write("NULL");
		else if (hasMarkers) escape(sw, escaping, mappings);
		else {
			if (mappings != null) {
				for (int x = 0; x < value.length(); x++) {
					mappings.map(sw, value.charAt(x));
				}
			} else sw.write(value);
		}
	}

	static class EmptyValueTuple extends PostgresTuple {
		public boolean mustEscapeRecord() {
			return true;
		}

		public boolean mustEscapeArray() {
			return true;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
		}

		public String buildTuple(boolean quote) {
			return quote ? "'\"\"'" : "\"\"";
		}
	}
}
