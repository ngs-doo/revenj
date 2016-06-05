package org.revenj.database.postgres;

import java.util.Arrays;

public final class PostgresWriter implements PostgresBuffer, AutoCloseable {
	private char[] buffer;
	public final char[] tmp;
	private int position;

	public PostgresWriter() {
		buffer = new char[64];
		tmp = new char[64];
		position = 0;
	}

	private static ThreadLocal<PostgresWriter> threadWriter = new ThreadLocal<PostgresWriter>() {
		@Override
		protected PostgresWriter initialValue() {
			return new PostgresWriter();
		}
	};

	public static PostgresWriter create() {
		PostgresWriter writer = threadWriter.get();
		writer.reset();
		return writer;
	}

	public void close() {
		position = 0;
	}

	public void reset() {
		position = 0;
	}

	public void write(String input) {
		int len = input.length();
		if (position + len >= buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + len);
		}
		input.getChars(0, len, buffer, position);
		position += len;
	}

	public void write(byte c) {
		if (position == buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2);
		}
		buffer[position++] = (char) c;
	}

	public void write(char c) {
		if (position == buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2);
		}
		buffer[position++] = c;
	}

	public void write(char[] buf) {
		if (position + buf.length >= buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + buf.length);
		}
		for (int i = 0; i < buf.length; i++) {
			buffer[position + i] = buf[i];
		}
		position += buf.length;
	}

	public void write(char[] buf, int len) {
		if (position + len >= buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + len);
		}
		for (int i = 0; i < len; i++) {
			buffer[position + i] = buf[i];
		}
		position += len;
	}

	public void write(char[] buf, int off, int end) {
		if (position + end >= buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + end);
		}
		for (int i = off; i < end; i++) {
			buffer[position + i - off] = buf[i];
		}
		position += end - off;
	}

	public void writeBuffer(int len) {
		if (position + len >= buffer.length) {
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

	public static void writeSimpleUriList(StringBuilder sb, String[] uris) {
		sb.append('\'');
		String uri = uris[0];
		int ind = uri.indexOf('\'');
		if (ind == -1) {
			sb.append(uri);
		} else {
			for (int i = 0; i < uri.length(); i++) {
				char c = uri.charAt(i);
				if (c == '\'') {
					sb.append("''");
				} else {
					sb.append(c);
				}
			}
		}
		for (int x = 1; x < uris.length; x++) {
			uri = uris[x];
			sb.append("','");
			ind = uri.indexOf('\'');
			if (ind == -1) {
				sb.append(uri);
			} else {
				for (int i = 0; i < uri.length(); i++) {
					char c = uri.charAt(i);
					if (c == '\'') {
						sb.append("''");
					} else {
						sb.append(c);
					}
				}
			}
		}
		sb.append('\'');
	}

	public static void writeSimpleUri(StringBuilder sb, String uri) {
		sb.append('\'');
		int ind = uri.indexOf('\'');
		if (ind == -1) {
			sb.append(uri);
		} else {
			for (int i = 0; i < uri.length(); i++) {
				char c = uri.charAt(i);
				if (c == '\'') {
					sb.append("''");
				} else {
					sb.append(c);
				}
			}
		}
		sb.append('\'');
	}

	private static int findEscapedChar(String input) {
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == '\\' || c == '/' || c == '\'') {
				return i;
			}
		}
		return -1;
	}

	public static void writeCompositeUriList(StringBuilder sb, String[] uris) {
		sb.append("('");
		String uri = uris[0];
		int i = 0;
		int ind = findEscapedChar(uri);
		if (ind == -1) {
			sb.append(uri);
		} else {
			while (i < uri.length()) {
				char c = uri.charAt(i);
				if (c == '\\') {
					sb.append(uri.charAt(++i));
				} else if (c == '/') {
					sb.append("','");
				} else if (c == '\'') {
					sb.append("''");
				} else {
					sb.append(c);
				}
				i++;
			}
		}
		for (int x = 1; x < uris.length; x++) {
			sb.append("'),('");
			uri = uris[x];
			ind = findEscapedChar(uri);
			if (ind == -1) {
				sb.append(uri);
			} else {
				i = 0;
				while (i < uri.length()) {
					char c = uri.charAt(i);
					if (c == '\\') {
						sb.append(uri.charAt(++i));
					} else if (c == '/') {
						sb.append("','");
					} else if (c == '\'') {
						sb.append("''");
					} else {
						sb.append(c);
					}
					i++;
				}
			}
		}
		sb.append("')");
	}

	public static void writeCompositeUri(StringBuilder sb, String uri) {
		sb.append("('");
		int i = 0;
		int ind = findEscapedChar(uri);
		if (ind == -1) {
			sb.append(uri);
		} else {
			while (i < uri.length()) {
				char c = uri.charAt(i);
				if (c == '\\') {
					sb.append(uri.charAt(++i));
				} else if (c == '/') {
					sb.append("','");
				} else if (c == '\'') {
					sb.append("''");
				} else {
					sb.append(c);
				}
				i++;
			}
		}
		sb.append("')");
	}

	@Override
	public char[] getTempBuffer() {
		return tmp;
	}

	@Override
	public void initBuffer() {
		reset();
	}

	@Override
	public void initBuffer(char c) {
		reset();
		write(c);
	}

	@Override
	public void addToBuffer(char c) {
		write(c);
	}

	@Override
	public void addToBuffer(char[] buf) {
		write(buf);
	}

	@Override
	public void addToBuffer(char[] buf, int len) {
		write(buf, len);
	}

	@Override
	public void addToBuffer(char[] buf, int off, int end) {
		write(buf, off, end);
	}

	@Override
	public void addToBuffer(String input) {
		write(input);
	}

	@Override
	public String bufferToString() {
		String result = toString();
		position = 0;
		return result;
	}
}
