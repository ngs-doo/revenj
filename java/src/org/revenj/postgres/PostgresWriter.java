package org.revenj.postgres;

import java.util.Arrays;

public class PostgresWriter {
	private char[] buffer;
	public final char[] tmp;
	private int position;

	public PostgresWriter() {
		buffer = new char[64];
		tmp = new char[64];
	}

	public void write(String input) {
		int len = input.length();
		if (position + len < buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + input.length());
		}
		input.getChars(0, len, buffer, position);
		position += len;
	}

	public void write(char c) {
		if (position == buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2);
		}
		buffer[position++] = c;
	}

	public void write(char[] buf, int len) {
		if (position + len < buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + len);
		}
		for (int i = 0; i < len; i++) {
			buffer[position + i] = buf[i];
		}
		position += len;
	}

	public void write(char[] buf, int off, int end) {
		if (position + end < buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + end);
		}
		for (int i = off; i < end; i++) {
			buffer[position + i - off] = buf[i];
		}
		position += end - off;
	}

	public void writeBuffer(int len) {
		if (position + len < buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + len);
		}
		for (int i = 0; i < len; i++) {
			buffer[position + i] = tmp[i];
		}
		position += len;
	}

	public String toString() {
		return new String(buffer, 0, position);
	}
}
