package org.revenj.database.postgres.converters;

import org.postgresql.util.PGobject;
import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;
import org.revenj.database.postgres.PostgresWriter;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class DateConverter {

	//actually different date can be used
	public static final LocalDate MIN_DATE = LocalDate.of(1, 1, 1);

	public static void setParameter(PostgresBuffer sw, PreparedStatement ps, int index, LocalDate value) throws SQLException {
		PGobject pg = new PGobject();
		//TODO: postgres driver for some reason embeds timezone along the date
		pg.setType("date");
		char[] buf = sw.getTempBuffer();
		serialize(buf, 0, value);
		pg.setValue(new String(buf, 0, 10));
		ps.setObject(index, pg);
	}

	public static void serializeURI(PostgresBuffer sw, LocalDate value) {
		serialize(sw.getTempBuffer(), 0, value);
		sw.addToBuffer(sw.getTempBuffer(), 10);
	}

	private static void serialize(char[] buf, int start, LocalDate value) {
		//TODO: Java supports wider range of dates
		NumberConverter.write4(value.getYear(), buf, start);
		buf[start + 4] = '-';
		NumberConverter.write2(value.getMonthValue(), buf, start + 5);
		buf[start + 7] = '-';
		NumberConverter.write2(value.getDayOfMonth(), buf, start + 8);
	}

	public static LocalDate parse(PostgresReader reader, boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return allowNulls ? null : MIN_DATE;
		}
		LocalDate res = parseDate(reader, cur);
		reader.read();
		return res;
	}

	private static LocalDate parseDate(PostgresReader reader, int cur) throws IOException {
		//TODO: BC after date for year < 0 ... not supported by .NET, but supported by Java
		if (cur == '\\' || cur == '"') {
			throw new RuntimeException("Negative dates are not yet implemented.");
		}
		char[] buf = reader.tmp;
		buf[0] = (char) cur;
		reader.fillTotal(buf, 1, 9);//TODO: dates larger than 9999
		if (buf[4] != '-') {
			return parseDateSlow(buf, reader);
		}
		return LocalDate.of(NumberConverter.read4(buf, 0), NumberConverter.read2(buf, 5), NumberConverter.read2(buf, 8));
	}

	private static LocalDate parseDateSlow(char[] buf, PostgresReader reader) {
		int foundAt = 4;
		for (; foundAt < buf.length; foundAt++) {
			if (buf[foundAt] == '-') break;
		}
		if (foundAt == buf.length) {
			throw new RuntimeException("Invalid date value.");
		}
		int year = NumberConverter.parsePositive(buf, 0, foundAt);
		char[] newBuf = reader.tmp;
		for (int i = foundAt + 1; i < buf.length; i++) {
			newBuf[i - foundAt - 1] = buf[i];
		}
		for (int i = buf.length - foundAt - 1; i < 5; i++) {
			newBuf[i] = (char) reader.read();
		}
		return LocalDate.of(year, NumberConverter.read2(newBuf, 0), NumberConverter.read2(newBuf, 3));
	}

	public static List<LocalDate> parseCollection(PostgresReader reader, int context, boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean escaped = cur == '"' || cur == '\\';
		if (escaped) {
			reader.read(context);
		}
		List<LocalDate> list = new ArrayList<>();
		cur = reader.peek();
		if (cur == '}') {
			reader.read();
		}
		LocalDate defaultValue = allowNulls ? null : MIN_DATE;
		while (cur != -1 && cur != '}') {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read(4);
				list.add(defaultValue);
			} else {
				list.add(parseDate(reader, cur));
				cur = reader.read();
			}
		}
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static PostgresTuple toTuple(LocalDate value) {
		if (value == null) return null;
		return new DateTuple(value);
	}

	private static class DateTuple extends PostgresTuple {
		private final LocalDate value;

		DateTuple(LocalDate value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			serialize(sw.tmp, 0, value);
			sw.writeBuffer(10);
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			insertRecord(sw, escaping, mappings);
		}

		public String buildTuple(boolean quote) {
			if (quote) {
				char[] buf = new char[12];
				buf[0] = '\'';
				serialize(buf, 1, value);
				buf[11] = '\'';
				return new String(buf, 0, 12);
			} else {
				char[] buf = new char[10];
				serialize(buf, 0, value);
				return new String(buf, 0, 10);
			}
		}
	}
}
