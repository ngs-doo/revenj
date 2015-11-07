package org.revenj.json;

public abstract class NumberConverter {

	private final static int[] Digits = new int[100];
	private final static double[] POW_10 = new double[16];

	static {
		for (int i = 0; i < 100; i++) {
			Digits[i] = (i < 10 ? 1 << 16 : 0) + (((i / 10) + '0') << 8) + i % 10 + '0';
		}
		long tenPow = 1;
		for (int i = 0; i < POW_10.length; i++) {
			POW_10[i] = tenPow;
			tenPow = tenPow * 10;
		}
	}

	static void write4(final int value, final byte[] buf, final int pos) {
		if (value > 9999) {
			throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + value);
		}
		final int q = value / 100;
		final int v1 = Digits[q];
		final int v2 = Digits[value - ((q << 6) + (q << 5) + (q << 2))];
		buf[pos] = (byte) (v1 >> 8);
		buf[pos + 1] = (byte) v1;
		buf[pos + 2] = (byte) (v2 >> 8);
		buf[pos + 3] = (byte) v2;
	}

	static void write2(final int value, final byte[] buf, final int pos) {
		final int v = Digits[value];
		buf[pos] = (byte) (v >> 8);
		buf[pos + 1] = (byte) v;
	}

	static void write3(final int number, final byte[] buf, int pos) {
		final int hi = number / 100;
		buf[pos] = (byte)(hi + '0');
		final int pair = Digits[number - hi * 100];
		buf[pos + 1] = (byte) (pair >> 8);
		buf[pos + 2] = (byte) pair;
	}

	static int read2(final char[] buf, final int pos) {
		final int v1 = buf[pos] - 48;
		return (v1 << 3) + (v1 << 1) + buf[pos + 1] - 48;
	}

	static int read4(final char[] buf, final int pos) {
		final int v2 = buf[pos + 1] - 48;
		final int v3 = buf[pos + 2] - 48;
		return (buf[pos] - 48) * 1000
				+ (v2 << 6) + (v2 << 5) + (v2 << 2)
				+ (v3 << 3) + (v3 << 1)
				+ buf[pos + 3] - 48;
	}
}
