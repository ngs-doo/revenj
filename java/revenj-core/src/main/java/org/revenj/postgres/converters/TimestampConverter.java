package org.revenj.postgres.converters;

import org.postgresql.util.PGobject;
import org.revenj.postgres.PostgresBuffer;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.PostgresWriter;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public abstract class TimestampConverter {

	private static final LocalDateTime MIN_LOCAL_DATE_TIME = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0);
	private static final OffsetDateTime MIN_DATE_TIME_UTC = OffsetDateTime.of(MIN_LOCAL_DATE_TIME, ZoneOffset.UTC);

	private final static int[] TIMESTAMP_REMINDER = new int[]{
			100000,
			10000,
			1000,
			100,
			10,
			1
	};

	public static void setParameter(PostgresBuffer sw, PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
		PGobject pg = new PGobject();
		pg.setType("timestamptz");
		char[] buf = sw.getTempBuffer();
		int len = serialize(buf, 0, value);
		pg.setValue(new String(buf, 0, len));
		ps.setObject(index, pg);
	}

	public static void setParameter(PostgresBuffer sw, PreparedStatement ps, int index, OffsetDateTime value) throws SQLException {
		PGobject pg = new PGobject();
		pg.setType("timestamptz");
		char[] buf = sw.getTempBuffer();
		int len = serialize(buf, 0, value);
		pg.setValue(new String(buf, 0, len));
		ps.setObject(index, pg);
	}

	public static void serializeURI(PostgresBuffer sw, LocalDateTime value) {
		int len = serialize(sw.getTempBuffer(), 0, value);
		sw.addToBuffer(sw.getTempBuffer(), len);
	}

	public static void serializeURI(PostgresBuffer sw, OffsetDateTime value) {
		int len = serialize(sw.getTempBuffer(), 0, value);
		sw.addToBuffer(sw.getTempBuffer(), len);
	}

	private static int serialize(char[] buffer, int pos, LocalDateTime value) {
		//TODO: Java supports wider range of dates
		buffer[pos + 4] = '-';
		buffer[pos + 7] = '-';
		buffer[pos + 10] = ' ';
		buffer[pos + 13] = ':';
		buffer[pos + 16] = ':';
		NumberConverter.write4(value.getYear(), buffer, pos);
		NumberConverter.write2(value.getMonthValue(), buffer, pos + 5);
		NumberConverter.write2(value.getDayOfMonth(), buffer, pos + 8);
		NumberConverter.write2(value.getHour(), buffer, pos + 11);
		NumberConverter.write2(value.getMinute(), buffer, pos + 14);
		NumberConverter.write2(value.getSecond(), buffer, pos + 17);
		int micro = value.getNano() / 1000;
		int end = pos + 19;
		if (micro != 0) {
			buffer[pos + 19] = '.';
			int div = micro / 100;
			int rem = micro - div * 100;
			NumberConverter.write4(div, buffer, 20);
			NumberConverter.write2(rem, buffer, 24);
			end = pos + 25;
			while (buffer[end] == '0')
				end--;
			end++;
		}
		buffer[end] = '+';
		buffer[end + 1] = '0';
		buffer[end + 2] = '0';
		return end + 3;
	}

	public static int serialize(char[] buffer, int pos, OffsetDateTime value) {
		final int offset = value.getOffset().getTotalSeconds();
		final int offsetHours = offset / 3600;
		final int offsetDiff = offset - offsetHours * 3600;
		if (offsetDiff != 0) return serialize(buffer, pos, value.plusSeconds(offsetDiff));
		//TODO: Java supports wider range of dates
		buffer[pos + 4] = '-';
		buffer[pos + 7] = '-';
		buffer[pos + 10] = ' ';
		buffer[pos + 13] = ':';
		buffer[pos + 16] = ':';
		NumberConverter.write4(value.getYear(), buffer, pos);
		NumberConverter.write2(value.getMonthValue(), buffer, pos + 5);
		NumberConverter.write2(value.getDayOfMonth(), buffer, pos + 8);
		NumberConverter.write2(value.getHour(), buffer, pos + 11);
		NumberConverter.write2(value.getMinute(), buffer, pos + 14);
		NumberConverter.write2(value.getSecond(), buffer, pos + 17);
		int micro = value.getNano() / 1000;
		int end = pos + 19;
		if (micro != 0) {
			buffer[pos + 19] = '.';
			int div = micro / 100;
			int rem = micro - div * 100;
			NumberConverter.write4(div, buffer, 20);
			NumberConverter.write2(rem, buffer, 24);
			end = pos + 25;
			while (buffer[end] == '0')
				end--;
			end++;
		}
		if (offsetHours >= 0) {
			buffer[end] = '+';
			NumberConverter.write2(offsetHours, buffer, end + 1);
		} else {
			buffer[end] = '-';
			NumberConverter.write2(-offsetHours, buffer, end + 1);
		}
		return end + 3;
	}

	public static LocalDateTime parseLocal(PostgresReader reader, int context, boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return allowNulls ? null : MIN_LOCAL_DATE_TIME;
		}
		LocalDateTime res = parseLocalTimestamp(reader, context);
		reader.read();
		return res;
	}

	public static OffsetDateTime parseOffset(
			PostgresReader reader,
			int context,
			boolean allowNulls,
			boolean asUtc) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return allowNulls ? null : MIN_DATE_TIME_UTC;
		}
		OffsetDateTime res = parseOffsetTimestamp(reader, context, asUtc);
		reader.read();
		return res;
	}

	private static LocalDateTime parseLocalTimestamp(PostgresReader reader, int context) throws IOException {
		//TODO: BC after date for year < 0 ... not supported by .NET, but supported by Java
		int cur = reader.read(context);
		char[] buf = reader.tmp;
		buf[0] = (char) cur;
		int len = reader.fillUntil(buf, 1, '\\', '"') + 1;
		reader.read(context);
		if (buf[10] != ' ') {
			return LocalDateTime.parse(new String(buf, 0, len));
		}
		int year = NumberConverter.read4(buf, 0);
		int month = NumberConverter.read2(buf, 5);
		int date = NumberConverter.read2(buf, 8);
		int hour = NumberConverter.read2(buf, 11);
		int minutes = NumberConverter.read2(buf, 14);
		int seconds = NumberConverter.read2(buf, 17);
		if (buf[19] == '.') {
			int nano = 0;
			int max = len - 3;
			for (int i = 20, r = 0; i < max && r < TIMESTAMP_REMINDER.length; i++, r++) {
				nano += TIMESTAMP_REMINDER[r] * (buf[i] - 48);
			}
			boolean pos = buf[len - 3] == '+';
			int offset = NumberConverter.read2(buf, len - 2);
			return offset != 0
					? LocalDateTime.of(year, month, date, hour, minutes, seconds, nano * 1000).plusHours(pos ? -offset : offset)
					: LocalDateTime.of(year, month, date, hour, minutes, seconds, nano * 1000);
		} else if (len == 20 && buf[19] == 'Z') {
			return LocalDateTime.of(year, month, date, hour, minutes, seconds, 0);
		} else if (len == 22) {
			boolean pos = buf[len - 3] == '+';
			int offset = NumberConverter.read2(buf, len - 2);
			return offset != 0
					? LocalDateTime.of(year, month, date, hour, minutes, seconds).plusHours(pos ? -offset : offset)
					: LocalDateTime.of(year, month, date, hour, minutes, seconds);
		} else if (len == 25) {
			boolean pos = buf[19] == '+';
			int offsetHours = NumberConverter.read2(buf, 20);
			int offsetMin = NumberConverter.read2(buf, 23);
			return LocalDateTime.of(year, month, date, hour, minutes, seconds, 0)
					.plusHours(pos ? -offsetHours : offsetHours)
					.plusMinutes(pos ? -offsetMin : offsetMin);
		} else {
			buf[10] = 'T';
			return LocalDateTime.parse(new String(buf, 0 , len));
		}
	}

	private static OffsetDateTime parseOffsetTimestamp(PostgresReader reader, int context, boolean asUtc) throws IOException {
		//TODO: BC after date for year < 0 ... not supported by .NET, but supported by Java
		int cur = reader.read(context);
		char[] buf = reader.tmp;
		buf[0] = (char) cur;
		int len = reader.fillUntil(buf, 1, '\\', '"') + 1;
		reader.read(context);
		if (buf[10] != ' ') {
			return OffsetDateTime.parse(new String(buf, 0, len));
		}
		int year = NumberConverter.read4(buf, 0);
		int month = NumberConverter.read2(buf, 5);
		int date = NumberConverter.read2(buf, 8);
		int hour = NumberConverter.read2(buf, 11);
		int minutes = NumberConverter.read2(buf, 14);
		int seconds = NumberConverter.read2(buf, 17);
		if (buf[19] == '.') {
			int nano = 0;
			int max = len - 3;
			for (int i = 20, r = 0; i < max && r < TIMESTAMP_REMINDER.length; i++, r++) {
				nano += TIMESTAMP_REMINDER[r] * (buf[i] - 48);
			}
			boolean pos = buf[len - 3] == '+';
			int offset = NumberConverter.read2(buf, len - 2);
			return asUtc
					? OffsetDateTime.of(year, month, date, hour, minutes, seconds, nano * 1000, ZoneOffset.UTC).plusHours(pos ? -offset : offset)
					: offset != 0
					? OffsetDateTime.of(year, month, date, hour, minutes, seconds, nano * 1000, ZoneOffset.ofHours(pos ? offset : -offset))
					: OffsetDateTime.of(year, month, date, hour, minutes, seconds, nano * 1000, ZoneOffset.UTC);
		} else if (len == 20 && buf[19] == 'Z') {
			return OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.UTC);
		} else if (len == 22) {
			boolean pos = buf[19] == '+';
			int offset = NumberConverter.read2(buf, 20);
			return asUtc
					? OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.UTC).plusHours(pos ? -offset : offset)
					: offset != 0
					? OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.ofHours(pos ? offset : -offset))
					: OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.UTC);
		} else if (len == 25) {
			boolean pos = buf[19] == '+';
			int offsetHours = NumberConverter.read2(buf, 20);
			int offsetMin = NumberConverter.read2(buf, 23);
			return asUtc
					? OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.UTC)
						.plusHours(pos ? -offsetHours : offsetHours)
						.plusMinutes(pos ? -offsetMin : offsetMin)
					: OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.ofHoursMinutes(pos ? offsetHours : -offsetHours, pos ? offsetMin : -offsetMin));
		} else {
			buf[10] = 'T';
			return OffsetDateTime.parse(new String(buf, 0 , len));
		}
	}

	public static List<LocalDateTime> parseLocalCollection(
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
		cur = reader.peek();
		if (cur == '}') {
			if (escaped) {
				reader.read(context + 2);
			} else {
				reader.read(2);
			}
			return new ArrayList<>(0);
		}
		List<LocalDateTime> list = new ArrayList<>();
		LocalDateTime defaultValue = allowNulls ? null : MIN_LOCAL_DATE_TIME;
		do {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read(4);
				list.add(defaultValue);
			} else {
				list.add(parseLocalTimestamp(reader, innerContext));
				cur = reader.read();
			}
		} while (cur == ',');
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static List<OffsetDateTime> parseOffsetCollection(
			PostgresReader reader,
			int context,
			boolean allowNulls,
			boolean asUtc) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean escaped = cur != '{';
		if (escaped) {
			reader.read(context);
		}
		int innerContext = context == 0 ? 1 : context << 1;
		cur = reader.peek();
		if (cur == '}') {
			if (escaped) {
				reader.read(context + 2);
			} else {
				reader.read(2);
			}
			return new ArrayList<>(0);
		}
		List<OffsetDateTime> list = new ArrayList<>();
		OffsetDateTime defaultValue = allowNulls ? null : MIN_DATE_TIME_UTC;
		do {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read(4);
				list.add(defaultValue);
			} else {
				list.add(parseOffsetTimestamp(reader, innerContext, asUtc));
				cur = reader.read();
			}
		} while (cur == ',');
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static PostgresTuple toTuple(LocalDateTime value) {
		if (value == null) return null;
		return new LocalTimestampTuple(value);
	}

	public static PostgresTuple toTuple(OffsetDateTime value) {
		if (value == null) return null;
		return new OffsetTimestampTuple(value);
	}

	static class LocalTimestampTuple extends PostgresTuple {
		private final LocalDateTime value;

		public LocalTimestampTuple(LocalDateTime value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return true;
		}

		public boolean mustEscapeArray() {
			return true;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			int len = serialize(sw.tmp, 0, value);
			sw.writeBuffer(len);
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			insertRecord(sw, escaping, mappings);
		}

		public String buildTuple(boolean quote) {
			char[] buf = new char[32];
			if (quote) {
				buf[0] = '\'';
				int len = serialize(buf, 1, value);
				buf[len] = '\'';
				return new String(buf, 0, len + 1);
			} else {
				int len = serialize(buf, 1, value);
				return new String(buf, 0, len);
			}
		}
	}

	static class OffsetTimestampTuple extends PostgresTuple {
		private final OffsetDateTime value;

		public OffsetTimestampTuple(OffsetDateTime value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return true;
		}

		public boolean mustEscapeArray() {
			return true;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			int len = serialize(sw.tmp, 0, value);
			sw.write(sw.tmp, 0, len);
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			insertRecord(sw, escaping, mappings);
		}

		public String buildTuple(boolean quote) {
			char[] buf = new char[32];
			if (quote) {
				buf[0] = '\'';
				int len = serialize(buf, 1, value);
				buf[len] = '\'';
				return new String(buf, 0, len + 1);
			} else {
				int len = serialize(buf, 1, value);
				return new String(buf, 0, len);
			}
		}
	}
}
