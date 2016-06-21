package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresWriter;
import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class UuidConverter {
	public static final UUID MIN_UUID = new java.util.UUID(0L, 0L);

	private static final char[] Lookup;
	private static final byte[] Values;

	static {
		Lookup = new char[256];
		Values = new byte['f' + 1 - '0'];
		for (int i = 0; i < 256; i++) {
			int hi = (i >> 4) & 15;
			int lo = i & 15;
			Lookup[i] = (char) (((hi < 10 ? '0' + hi : 'a' + hi - 10) << 8) + (lo < 10 ? '0' + lo : 'a' + lo - 10));
		}
		for (char c = '0'; c <= '9'; c++) {
			Values[c - '0'] = (byte) (c - '0');
		}
		for (char c = 'a'; c <= 'f'; c++) {
			Values[c - '0'] = (byte) (c - 'a' + 10);
		}
		for (char c = 'A'; c <= 'F'; c++) {
			Values[c - '0'] = (byte) (c - 'A' + 10);
		}
	}

	public static void serializeURI(PostgresBuffer sw, UUID value) {
		if (value == null) return;
		serialize(value, sw.getTempBuffer(), 0);
		sw.addToBuffer(sw.getTempBuffer(), 36);
	}

	private static void serialize(UUID value, char[] buf, int start) {
		final long hi = value.getMostSignificantBits();
		final long lo = value.getLeastSignificantBits();
		final int hi1 = (int) (hi >> 32);
		final int hi2 = (int) hi;
		final int lo1 = (int) (lo >> 32);
		final int lo2 = (int) lo;
		int v = (hi1 >> 24) & 255;
		int l = Lookup[v];
		buf[start] = (char) (byte) (l >> 8);
		buf[start + 1] = (char) (byte) l;
		v = (hi1 >> 16) & 255;
		l = Lookup[v];
		buf[start + 2] = (char) (byte) (l >> 8);
		buf[start + 3] = (char) (byte) l;
		v = (hi1 >> 8) & 255;
		l = Lookup[v];
		buf[start + 4] = (char) (byte) (l >> 8);
		buf[start + 5] = (char) (byte) l;
		v = hi1 & 255;
		l = Lookup[v];
		buf[start + 6] = (char) (byte) (l >> 8);
		buf[start + 7] = (char) (byte) l;
		buf[start + 8] = '-';
		v = (hi2 >> 24) & 255;
		l = Lookup[v];
		buf[start + 9] = (char) (byte) (l >> 8);
		buf[start + 10] = (char) (byte) l;
		v = (hi2 >> 16) & 255;
		l = Lookup[v];
		buf[start + 11] = (char) (byte) (l >> 8);
		buf[start + 12] = (char) (byte) l;
		buf[start + 13] = '-';
		v = (hi2 >> 8) & 255;
		l = Lookup[v];
		buf[start + 14] = (char) (byte) (l >> 8);
		buf[start + 15] = (char) (byte) l;
		v = hi2 & 255;
		l = Lookup[v];
		buf[start + 16] = (char) (byte) (l >> 8);
		buf[start + 17] = (char) (byte) l;
		buf[start + 18] = '-';
		v = (lo1 >> 24) & 255;
		l = Lookup[v];
		buf[start + 19] = (char) (byte) (l >> 8);
		buf[start + 20] = (char) (byte) l;
		v = (lo1 >> 16) & 255;
		l = Lookup[v];
		buf[start + 21] = (char) (byte) (l >> 8);
		buf[start + 22] = (char) (byte) l;
		buf[start + 23] = '-';
		v = (lo1 >> 8) & 255;
		l = Lookup[v];
		buf[start + 24] = (char) (byte) (l >> 8);
		buf[start + 25] = (char) (byte) l;
		v = lo1 & 255;
		l = Lookup[v];
		buf[start + 26] = (char) (byte) (l >> 8);
		buf[start + 27] = (char) (byte) l;
		v = (lo2 >> 24) & 255;
		l = Lookup[v];
		buf[start + 28] = (char) (byte) (l >> 8);
		buf[start + 29] = (char) (byte) l;
		v = (lo2 >> 16) & 255;
		l = Lookup[v];
		buf[start + 30] = (char) (byte) (l >> 8);
		buf[start + 31] = (char) (byte) l;
		v = (lo2 >> 8) & 255;
		l = Lookup[v];
		buf[start + 32] = (char) (byte) (l >> 8);
		buf[start + 33] = (char) (byte) l;
		v = lo2 & 255;
		l = Lookup[v];
		buf[start + 34] = (char) (byte) (l >> 8);
		buf[start + 35] = (char) (byte) l;
	}

	public static int serializeURI(char[] buf, int pos, UUID value) throws IOException {
		if (value == null) return pos;
		serialize(value, buf, pos);
		return pos + 36;
	}

	public static UUID parse(PostgresReader reader, boolean nullable) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return nullable ? null : MIN_UUID;
		}
		char[] buf = reader.tmp;
		buf[0] = (char) cur;
		reader.fillTotal(buf, 1, 36);
		return toUuid(buf);
	}

	private static UUID toUuid(char[] buf) throws IOException {
		try {
			long hi = 0;
			for (int i = 0; i < 8; i++)
				hi = (hi << 4) + Values[buf[i] - '0'];
			for (int i = 9; i < 13; i++)
				hi = (hi << 4) + Values[buf[i] - '0'];
			for (int i = 14; i < 18; i++)
				hi = (hi << 4) + Values[buf[i] - '0'];
			long lo = 0;
			for (int i = 19; i < 23; i++)
				lo = (lo << 4) + Values[buf[i] - '0'];
			for (int i = 24; i < 36; i++)
				lo = (lo << 4) + Values[buf[i] - '0'];
			return new UUID(hi, lo);
		} catch (ArrayIndexOutOfBoundsException ex) {
			return UUID.fromString(new String(buf, 0, 36));
		}
	}

	public static List<UUID> parseCollection(PostgresReader reader, int context, boolean nullable) throws IOException {
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
		ArrayList<UUID> list = new ArrayList<>();
		UUID defaultValue = nullable ? null : MIN_UUID;
		char[] buf = reader.tmp;
		do {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read(4);
				list.add(defaultValue);
			} else {
				buf[0] = (char) cur;
				reader.fillTotal(buf, 1, 35);
				list.add(toUuid(buf));
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

	public static PostgresTuple toTupleNullable(UUID value) {
		return value == null ? null : new UuidTuple(value);
	}

	public static PostgresTuple toTuple(UUID value) {
		return new UuidTuple(value);
	}

	private static class UuidTuple extends PostgresTuple {
		private final UUID value;

		UuidTuple(UUID value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return false;
		}

		public boolean mustEscapeArray() {
			return false;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			serialize(value, sw.tmp, 0);
			sw.writeBuffer(36);
		}

		public String buildTuple(boolean quote) {
			if (quote) {
				char[] buf = new char[38];
				buf[0] = '\'';
				serialize(value, buf, 1);
				buf[37] = '\'';
				return new String(buf, 0, buf.length);
			} else {
				char[] buf = new char[36];
				serialize(value, buf, 0);
				return new String(buf, 0, buf.length);
			}
		}
	}
}
