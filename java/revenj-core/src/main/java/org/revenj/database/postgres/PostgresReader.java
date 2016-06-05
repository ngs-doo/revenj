package org.revenj.database.postgres;

import org.revenj.patterns.ServiceLocator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public final class PostgresReader implements PostgresBuffer, AutoCloseable {
	private String input = "";
	private int length;
	private int positionInInput;
	private int last;
	private char[] buffer;
	private int positionInBuffer;
	private ServiceLocator locator;
	public final char[] tmp;

	public PostgresReader() {
		this(null);
	}

	public PostgresReader(ServiceLocator locator) {
		this.buffer = new char[64];
		this.tmp = new char[48];
		this.locator = locator;
	}

	public Optional<ServiceLocator> getLocator() {
		return Optional.ofNullable(locator);
	}

	void reset(ServiceLocator locator) {
		positionInBuffer = 0;
		positionInInput = 0;
		this.locator = locator;
	}

	private static ThreadLocal<PostgresReader> threadReader = new ThreadLocal<PostgresReader>() {
		@Override
		protected PostgresReader initialValue() {
			return new PostgresReader();
		}
	};

	public static PostgresReader create(ServiceLocator locator) {
		PostgresReader reader = threadReader.get();
		reader.reset(locator);
		return reader;
	}

	public void close() {
		length = positionInBuffer = positionInInput = 0;
		last = -1;
	}

	public void process(String input) {
		this.input = input;
		this.length = input.length();
		positionInInput = 0;
		positionInBuffer = 0;
		last = 0;
	}

	public int read() {
		if (positionInInput >= length) {
			return last = -1;
		}
		return last = input.charAt(positionInInput++);
	}

	public int read(int total) {
		if (total == 0) {
			return 0;
		}
		if (total > 1) {
			positionInInput += total - 1;
		}
		return read();
	}

	public int peek() {
		if (positionInInput >= length) {
			return -1;
		}
		return input.charAt(positionInInput);
	}

	public int last() {
		return last;
	}

	@Override
	public char[] getTempBuffer() {
		return tmp;
	}

	@Override
	public void initBuffer() {
		positionInBuffer = 0;
	}

	@Override
	public void initBuffer(char c) {
		positionInBuffer = 1;
		buffer[0] = c;
	}

	@Override
	public void addToBuffer(char c) {
		if (positionInBuffer == buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2);
		}
		buffer[positionInBuffer++] = c;
	}

	@Override
	public void addToBuffer(char[] buf) {
		if (positionInBuffer + buf.length >= buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + buf.length);
		}
		for (int i = 0; i < buf.length; i++) {
			buffer[positionInBuffer + i] = buf[i];
		}
		positionInBuffer += buf.length;
	}

	@Override
	public void addToBuffer(char[] buf, int len) {
		if (positionInBuffer + len >= buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + len);
		}
		for (int i = 0; i < len; i++) {
			buffer[positionInBuffer + i] = buf[i];
		}
		positionInBuffer += len;
	}

	@Override
	public void addToBuffer(char[] buf, int offset, int end) {
		if (positionInBuffer + end >= buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + end);
		}
		for (int i = offset; i < end; i++) {
			buffer[positionInBuffer + i - offset] = buf[i];
		}
		positionInBuffer += end - offset;
	}

	@Override
	public void addToBuffer(String input) {
		int len = input.length();
		if (positionInBuffer + len >= buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2 + len);
		}
		input.getChars(0, len, buffer, positionInBuffer);
		positionInBuffer += len;
	}

	public void fillUntil(char c1, char c2) throws IOException {
		int i;
		for (i = positionInInput; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == c1 || c == c2) {
				break;
			}
			addToBuffer(c);
		}
		positionInInput = i;
		if (positionInInput == input.length()) {
			throw new IOException("End of input detected");
		}
	}

	public int fillUntil(char[] target, int offset, char c1, char c2) throws IOException {
		int i;
		int start = offset;
		for (i = positionInInput; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == c1 || c == c2) {
				break;
			}
			target[offset++] = c;
		}
		positionInInput = i;
		if (positionInInput == input.length()) {
			throw new IOException("End of input detected");
		}
		return offset - start;
	}

	public void fillTotal(char[] target, int offset, int count) throws IOException {
		//TODO: better exceptions
		for (int i = 0; i < count; i++) {
			target[i + offset] = input.charAt(positionInInput + i);
		}
		positionInInput += count;
	}

	@Override
	public String bufferToString() {
		int len = positionInBuffer;
		positionInBuffer = 0;
		if (len == 0) {
			return "";
		}
		return new String(buffer, 0, len);
	}

	public interface ConvertToValue<T> {
		T to(char[] buffer, int offset, int len);
	}

	public <T> T bufferToValue(ConvertToValue<T> converter) {
		return converter.to(buffer, 0, positionInBuffer);
	}

	public boolean bufferMatches(String compare) {
		if (compare.length() != positionInBuffer) {
			return false;
		}
		for (int i = 0; i < compare.length(); i++) {
			if (buffer[i] != compare.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	public int bufferHash() {
		int len = positionInBuffer;
		long hash = 0x811C9DC5;
		for (int i = 0; i < len && i < buffer.length; i++)
			hash = (hash ^ buffer[i]) * 0x1000193;
		return (int) hash;
	}

	private static int findEscapedChar(String input) {
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == '\\' || c == '/') {
				return i;
			}
		}
		return -1;
	}

	public static void parseCompositeURI(String uri, String[] result) throws IOException {
		int index = 0;
		int i = findEscapedChar(uri);
		if (i == -1) {
			result[0] = uri;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(uri, 0, i);
			while (i < uri.length()) {
				char c = uri.charAt(i);
				if (c == '\\') {
					sb.append(uri.charAt(++i));
				} else if (c == '/') {
					result[index++] = sb.toString();
					if (index == result.length) throw new IOException("Invalid URI provided: " + uri + ". Number of expected parts: " +  result.length);
					sb.setLength(0);
				} else {
					sb.append(c);
				}
				i++;
			}
			sb.append(uri, i, uri.length());
			result[index] = sb.toString();
		}
	}
}
