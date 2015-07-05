package org.revenj.postgres.converters;

import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.PostgresWriter;

import java.util.ArrayList;
import java.util.List;

public abstract class ByteaConverter {

	private static final char[] CharMap;
	private static final int[] CharLookup;

	static {
		CharMap = "0123456789abcdef".toCharArray();
		CharLookup = new int['f' + 1];
		for (int i = 0; i < CharMap.length; i++) {
			CharLookup[CharMap[i]] = i;
		}
	}

	private static final byte[] EMPTY_BYTES = new byte[0];

	public static int serialize(char[] buf, int pos, byte[] value) {
		buf[pos] = '\\';
		buf[pos + 1] = 'x';
		int cnt = 2;
		//TODO: buf array index check
		for (byte b : value) {
			buf[cnt++] = CharMap[b >> 4];
			buf[cnt++] = CharMap[b & 0xf];
		}
		return pos + 2 + value.length * 2;
	}

	private static byte[] toArray(List<Byte> list) {
		byte[] result = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		return result;
	}

	public static byte[] parse(PostgresReader reader, int context) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		int len = context + (context << 1);
		if (len == 0) {
			len = 1;
		}
		cur = reader.read(len + 1);
		List<Byte> list = new ArrayList<>(1024);
		while (cur != -1 && cur != '\\' && cur != '"') {
			list.add((byte) ((CharLookup[cur] << 4) + CharLookup[reader.read()]));
			cur = reader.read();
		}
		reader.read(context);
		return toArray(list);
	}

	public static List<byte[]> parseCollection(PostgresReader reader, int context, boolean allowNulls) {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean escaped = cur != '{';
		if (escaped) {
			reader.read(context);
		}
		int innerContext = context << 1;
		int skipInner = innerContext + (innerContext << 1);
		List<byte[]> list = new ArrayList<>();
		cur = reader.peek();
		if (cur == '}') {
			reader.read();
		}
		byte[] emptyColl = allowNulls ? null : EMPTY_BYTES;
		while (cur != -1 && cur != '}') {
			cur = reader.read();
			if (cur == 'N') {
				list.add(emptyColl);
				cur = reader.read(4);
			} else {
				reader.read(skipInner);
				List<Byte> item = new ArrayList<>(1024);
				cur = reader.read();
				while (cur != -1 && cur != '"' && cur != '\\') {
					item.add((byte) ((CharLookup[cur] << 4) + CharLookup[reader.read()]));
					cur = reader.read();
				}
				cur = reader.read(innerContext);
				list.add(toArray(item));
			}
		}
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static PostgresTuple toTuple(byte[] value) {
		return value != null ? new ByteTuple(value) : null;
	}

	static class ByteTuple extends PostgresTuple {
		private final byte[] value;

		public ByteTuple(byte[] value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return true;
		}

		public boolean mustEscapeArray() {
			return true;
		}

		private void buildArray(PostgresWriter sw) {
			for (byte b : value) {
				sw.write(CharMap[b >> 4]);
				sw.write(CharMap[b & 0xf]);
			}
		}

		public void buildTuple(PostgresWriter sw, boolean quote) {
			if (quote) {
				sw.write('\'');
				insertRecord(sw, "", null);
				sw.write('\'');
			} else insertRecord(sw, "", null);
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			String pref = buildSlashEscape(escaping.length());
			if (mappings != null) {
				for (int x = 0; x < pref.length(); x++)
					mappings.map(sw, pref.charAt(x));
			} else sw.write(pref);
			sw.write('x');
			buildArray(sw);
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			//TODO this is wrong
			insertRecord(sw, escaping, mappings);
		}
	}
}
