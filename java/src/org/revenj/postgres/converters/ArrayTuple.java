package org.revenj.postgres.converters;

import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresWriter;

import java.util.Collection;
import java.util.List;

public class ArrayTuple extends PostgresTuple {
	public static final PostgresTuple EMPTY;
	public static final PostgresTuple NULL;

	static {
		EMPTY = new EmptyArrayTuple();
		NULL = new NullTuple();
	}

	private final PostgresTuple[] elements;
	private final boolean escapeRecord;

	private ArrayTuple(PostgresTuple[] elements) {
		this.elements = elements;
		this.escapeRecord = elements.length > 1 || elements[0] != null && elements[0].mustEscapeRecord();
	}

	public static PostgresTuple from(PostgresTuple[] elements) {
		if (elements == null) {
			return NULL;
		} else if (elements.length == 0) {
			return EMPTY;
		}
		return new ArrayTuple(elements);
	}

	static class EmptyArrayTuple extends PostgresTuple {
		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write("{}");
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			throw new RuntimeException("Should not happen. Insert array called on array tuple. Nested arrays are invalid construct.");
		}

		public String buildTuple(boolean quote) {
			return quote ? "'{}'" : "{}";
		}
	}

	static class NullTuple extends PostgresTuple {
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
		return escapeRecord;
	}

	public boolean mustEscapeArray() {
		return true;
	}

	public static <T> PostgresTuple create(T[] elements, ObjectConverter<T> converter) {
		if (elements == null) {
			return null;
		} else if (elements.length == 0) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.length];
		for (int i = 0; i < elements.length; i++) {
			tuples[i] = converter.to(elements[i]);
		}
		return new ArrayTuple(tuples);
	}

	public static <T> PostgresTuple create(List<T> elements, ObjectConverter<T> converter) {
		if (elements == null) {
			return null;
		} else if (elements.isEmpty()) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			tuples[i] = converter.to(elements.get(i));
		}
		return new ArrayTuple(tuples);
	}

	public static <T> PostgresTuple create(Collection<T> elements, ObjectConverter<T> converter) {
		if (elements == null) {
			return null;
		}
		if (elements.isEmpty()) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.size()];
		int i = 0;
		for (T t : elements) {
			tuples[i++] = converter.to(t);
		}
		return new ArrayTuple(tuples);
	}

	public String buildTuple(boolean quote) {
		PostgresWriter sw = new PostgresWriter();
		Mapping mappings = null;
		if (quote) {
			mappings = PostgresTuple::escapeQuote;
			sw.write('\'');
		}
		sw.write('{');
		PostgresTuple e = elements[0];
		if (e != null) {
			if (e.mustEscapeArray()) {
				sw.write('"');
				e.insertArray(sw, "0", mappings);
				sw.write('"');
			} else e.insertArray(sw, "", mappings);
		} else sw.write("NULL");
		for (int i = 1; i < elements.length; i++) {
			sw.write(',');
			e = elements[i];
			if (e != null) {
				if (e.mustEscapeArray()) {
					sw.write('"');
					e.insertArray(sw, "0", mappings);
					sw.write('"');
				} else e.insertArray(sw, "", mappings);
			} else sw.write("NULL");
		}
		sw.write('}');
		if (quote) {
			sw.write('\'');
		}
		return sw.toString();
	}

	public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
		sw.write('{');
		String newEscaping = escaping + "0";
		String quote = null;
		PostgresTuple e = elements[0];
		if (e != null) {
			if (e.mustEscapeArray()) {
				quote = buildQuoteEscape(escaping);
				if (mappings != null) {
					for (int x = 0; x < quote.length(); x++) {
						mappings.map(sw, quote.charAt(x));
					}
				} else sw.write(quote);
				e.insertArray(sw, newEscaping, mappings);
				if (mappings != null) {
					for (int x = 0; x < quote.length(); x++) {
						mappings.map(sw, quote.charAt(x));
					}
				} else sw.write(quote);
			} else e.insertArray(sw, escaping, mappings);
		} else sw.write("NULL");
		for (int i = 1; i < elements.length; i++) {
			sw.write(',');
			e = elements[i];
			if (e != null) {
				if (e.mustEscapeArray()) {
					quote = quote != null ? quote : buildQuoteEscape(escaping);
					if (mappings != null) {
						for (int x = 0; x < quote.length(); x++) {
							mappings.map(sw, quote.charAt(x));
						}
					} else sw.write(quote);
					e.insertArray(sw, newEscaping, mappings);
					if (mappings != null) {
						for (int x = 0; x < quote.length(); x++) {
							mappings.map(sw, quote.charAt(x));
						}
					} else sw.write(quote);
				} else e.insertArray(sw, escaping, mappings);
			} else sw.write("NULL");
		}
		sw.write('}');
	}

	public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
		throw new RuntimeException("Should not happen. Insert array called on array tuple. Nested arrays are invalid construct.");
	}
}
