package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresWriter;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

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

	private static class EmptyArrayTuple extends PostgresTuple {
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
		return escapeRecord;
	}

	public boolean mustEscapeArray() {
		return true;
	}

	public static PostgresTuple create(double[] elements, Function<Double, PostgresTuple> converter) {
		if (elements == null) {
			return null;
		} else if (elements.length == 0) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.length];
		for (int i = 0; i < elements.length; i++) {
			tuples[i] = converter.apply(elements[i]);
		}
		return new ArrayTuple(tuples);
	}

	public static PostgresTuple create(int[] elements, Function<Integer, PostgresTuple> converter) {
		if (elements == null) {
			return null;
		} else if (elements.length == 0) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.length];
		for (int i = 0; i < elements.length; i++) {
			tuples[i] = converter.apply(elements[i]);
		}
		return new ArrayTuple(tuples);
	}

	public static PostgresTuple create(long[] elements, Function<Long, PostgresTuple> converter) {
		if (elements == null) {
			return null;
		} else if (elements.length == 0) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.length];
		for (int i = 0; i < elements.length; i++) {
			tuples[i] = converter.apply(elements[i]);
		}
		return new ArrayTuple(tuples);
	}

	public static PostgresTuple create(float[] elements, Function<Float, PostgresTuple> converter) {
		if (elements == null) {
			return null;
		} else if (elements.length == 0) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.length];
		for (int i = 0; i < elements.length; i++) {
			tuples[i] = converter.apply(elements[i]);
		}
		return new ArrayTuple(tuples);
	}

	public static PostgresTuple create(boolean[] elements, Function<Boolean, PostgresTuple> converter) {
		if (elements == null) {
			return null;
		} else if (elements.length == 0) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.length];
		for (int i = 0; i < elements.length; i++) {
			tuples[i] = converter.apply(elements[i]);
		}
		return new ArrayTuple(tuples);
	}

	public static <T> PostgresTuple create(T[] elements, Function<T, PostgresTuple> converter) {
		if (elements == null) {
			return null;
		} else if (elements.length == 0) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.length];
		for (int i = 0; i < elements.length; i++) {
			tuples[i] = converter.apply(elements[i]);
		}
		return new ArrayTuple(tuples);
	}

	public static <T> PostgresTuple create(List<T> elements, Function<T, PostgresTuple> converter) {
		if (elements == null) {
			return null;
		} else if (elements.isEmpty()) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			tuples[i] = converter.apply(elements.get(i));
		}
		return new ArrayTuple(tuples);
	}

	public static <T> PostgresTuple create(Collection<T> elements, Function<T, PostgresTuple> converter) {
		if (elements == null) {
			return null;
		}
		if (elements.isEmpty()) {
			return EMPTY;
		}
		PostgresTuple[] tuples = new PostgresTuple[elements.size()];
		int i = 0;
		for (T t : elements) {
			tuples[i++] = converter.apply(t);
		}
		return new ArrayTuple(tuples);
	}

	public interface RecordParser<T> {
		T parse(PostgresReader reader, int outerContext, int context) throws IOException;
	}

	public static <T> List<T> parse(PostgresReader reader, int context, RecordParser<T> converter) throws IOException {
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
		List<T> list = new ArrayList<>();
		int arrayContext = Math.max(context << 1, 1);
		int recordContext = arrayContext << 1;
		while (cur != -1 && cur != '}') {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read(4);
				list.add(null);
			} else {
				boolean innerEscaped = cur != '(';
				if (innerEscaped) {
					reader.read(arrayContext);
				}
				list.add(converter.parse(reader, 0, recordContext));
				if (innerEscaped) {
					cur = reader.read(arrayContext + 1);
				} else {
					cur = reader.read();
				}
			}
		}
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public void buildTuple(PostgresWriter sw, boolean quote) {
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
