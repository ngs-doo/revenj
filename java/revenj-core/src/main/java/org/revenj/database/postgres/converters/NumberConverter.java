package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresWriter;

public abstract class NumberConverter {

	private static final int[] NUMBERS;

	static {
		NUMBERS = new int[100];
		for (int i = 0; i < NUMBERS.length; i++) {
			char first = (char) ((i / 10) + '0');
			char second = (char) ((i % 10) + '0');
			short offset = i < 10 ? (short) 1 : (short) 0;
			NUMBERS[i] = (offset << 24) + (first << 8) + second;
		}
	}

	static void write2(int number, char[] buffer, int start) {
		int pair = NUMBERS[number];
		buffer[start] = (char) (pair >> 8);
		buffer[start + 1] = (char) (byte) pair;
	}

	static void write2(int number, PostgresWriter sw) {
		int pair = NUMBERS[number];
		sw.write((char) (pair >> 8));
		sw.write((byte) pair);
	}

	static void write4(int number, char[] buffer, int start) {
		int div = number / 100;
		int pair1 = NUMBERS[div];
		buffer[start] = (char) (pair1 >> 8);
		buffer[start + 1] = (char) (byte) (pair1);
		int rem = number - div * 100;
		int pair2 = NUMBERS[rem];
		buffer[start + 2] = (char) (pair2 >> 8);
		buffer[start + 3] = (char) (byte) pair2;
	}

	static void write4(int number, PostgresWriter sw) {
		int div = number / 100;
		int pair1 = NUMBERS[div];
		sw.write((char) (pair1 >> 8));
		sw.write((char) (byte) (pair1));
		int rem = number - div * 100;
		int pair2 = NUMBERS[rem];
		sw.write((char) (pair2 >> 8));
		sw.write((byte) pair2);
	}

	static int read2(char[] source, int start) {
		int first = source[start] - 48;
		return (first << 3) + (first << 1) + source[start + 1] - 48;
	}

	static int read4(char[] source, int start) {
		int first = source[start] - 48;
		int second = source[start + 1] - 48;
		int third = source[start + 2] - 48;
		return first * 1000 + second * 100 + (third << 3) + (third << 1) + source[start + 3] - 48;
	}

	public static Integer tryParsePositiveInt(String number) {
		if (number.length() == 0 || number.charAt(0) < '0' || number.charAt(0) > '9') {
			return null;
		}
		int value = 0;
		for (int i = 0; i < number.length(); i++)
			value = (value << 3) + (value << 1) + number.charAt(i) - '0';
		return value;
	}

	public static long parseLong(String number) {
		long value = 0;
		if (number.charAt(0) == '-') {
			for (int i = 1; i < number.length(); i++)
				value = (value << 3) + (value << 2) - number.charAt(i) + '0';
		} else {
			for (int i = 0; i < number.length(); i++)
				value = (value << 3) + (value << 2) + number.charAt(i) - '0';
		}
		return value;
	}

	/**
	 * Should not be used for Integer.MIN_VALUE
	 *
	 * @param value
	 * @param buf
	 * @return
	 */
	public static int serialize(int value, char[] buf) {
		int q, r;
		int charPos = 10;
		int offset;
		int i;
		if (value < 0) {
			i = -value;
			offset = 0;
		} else {
			i = value;
			offset = 1;
		}
		int v = 0;
		while (charPos > 0) {
			q = i / 100;
			r = i - ((q << 6) + (q << 5) + (q << 2));
			i = q;
			v = NUMBERS[r];
			buf[charPos--] = (char) (byte) v;
			buf[charPos--] = (char) (v >> 8);
			if (i == 0) break;
		}
		int zeroBased = v >> 24;
		buf[charPos + zeroBased] = '-';
		return charPos + offset + zeroBased;
	}

	/**
	 * Should not be used for LONG.MIN_VALUE
	 *
	 * @param value
	 * @param buf
	 * @return
	 */
	public static int serialize(long value, char[] buf) {
		long q;
		int r;
		int charPos = 20;
		int offset;
		long i;
		if (value < 0) {
			i = -value;
			offset = 0;
		} else {
			i = value;
			offset = 1;
		}

		int v = 0;
		while (charPos > 0) {
			q = i / 100;
			r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
			i = q;
			v = NUMBERS[r];
			buf[charPos--] = (char) (byte) v;
			buf[charPos--] = (char) (v >> 8);
			if (i == 0) break;
		}
		int zeroBased = v >> 24;
		buf[charPos + zeroBased] = '-';
		return charPos + offset + zeroBased;
	}


	public static int parsePositive(char[] source, int start, int end) {
		int res = 0;
		for (int i = start; i < source.length; i++) {
			if (i == end) break;
			res = res * 10 + (source[i] - 48);
		}
		return res;
	}
}
