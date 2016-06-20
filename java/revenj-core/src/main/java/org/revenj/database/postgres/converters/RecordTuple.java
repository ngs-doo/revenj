package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresWriter;

public class RecordTuple extends PostgresTuple {
	public static final PostgresTuple EMPTY;
	public static final PostgresTuple NULL;

	static {
		EMPTY = new EmptyRecordTuple();
		NULL = new NullTuple();
	}

	private final PostgresTuple[] properties;

	RecordTuple(PostgresTuple[] properties) {
		this.properties = properties;
	}

	public static PostgresTuple from(PostgresTuple[] properties) {
		if (properties == null) return NULL;
		if (properties.length == 0) return EMPTY;
		return new RecordTuple(properties);
	}

	private static class EmptyRecordTuple extends PostgresTuple {
		public boolean mustEscapeRecord() {
			return true;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write("()");
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write("()");
		}

		public String buildTuple(boolean quote) {
			return quote ? "'()'" : "()";
		}
	}

	private static class NullTuple extends PostgresTuple {
		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write("NULL");
		}

		public String buildTuple(boolean quote) {
			return "NULL";
		}
	}

	public boolean mustEscapeRecord() {
		return true;
	}

	public boolean mustEscapeArray() {
		return true;
	}

	public String buildTuple(boolean quote) {
		PostgresWriter sw = threadWriter.get();
		sw.reset();
		Mapping mappings = null;
		if (quote) {
			mappings = PostgresTuple::escapeQuote;
			sw.write('\'');
		}
		sw.write('(');
		PostgresTuple p = properties[0];
		if (p != null) {
			if (p.mustEscapeRecord()) {
				sw.write('"');
				p.insertRecord(sw, "1", mappings);
				sw.write('"');
			} else p.insertRecord(sw, "", mappings);
		}
		for (int i = 1; i < properties.length; i++) {
			sw.write(',');
			p = properties[i];
			if (p != null) {
				if (p.mustEscapeRecord()) {
					sw.write('"');
					p.insertRecord(sw, "1", mappings);
					sw.write('"');
				} else p.insertRecord(sw, "", mappings);
			}
		}
		sw.write(')');
		if (quote) {
			sw.write('\'');
		}
		return sw.toString();
	}

	public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
		sw.write('(');
		String newEscaping = escaping + '1';
		String quote = null;
		PostgresTuple p = properties[0];
		if (p != null) {
			if (p.mustEscapeRecord()) {
				quote = buildQuoteEscape(escaping);
				if (mappings != null) {
					for (int x = 0; x < quote.length(); x++) {
						mappings.map(sw, quote.charAt(x));
					}
				} else sw.write(quote);
				p.insertRecord(sw, newEscaping, mappings);
				if (mappings != null) {
					for (int x = 0; x < quote.length(); x++) {
						mappings.map(sw, quote.charAt(x));
					}
				} else sw.write(quote);
			} else p.insertRecord(sw, escaping, mappings);
		}
		for (int i = 1; i < properties.length; i++) {
			sw.write(',');
			p = properties[i];
			if (p != null) {
				if (p.mustEscapeRecord()) {
					//TODO: build quote only once and reuse it, instead of looping all the time
					quote = quote != null ? quote : buildQuoteEscape(escaping);
					if (mappings != null) {
						for (int x = 0; x < quote.length(); x++) {
							mappings.map(sw, quote.charAt(x));
						}
					} else
						sw.write(quote);
					p.insertRecord(sw, newEscaping, mappings);
					if (mappings != null) {
						for (int x = 0; x < quote.length(); x++) {
							mappings.map(sw, quote.charAt(x));
						}
					} else sw.write(quote);
				} else p.insertRecord(sw, escaping, mappings);
			}
		}
		sw.write(')');
	}
}
