package org.revenj.postgres;

import java.io.IOException;
import java.util.Arrays;

public class PostgresReader {
	private String input = "";
	private int length;
	private int positionInInput;
	private int last;
	private char[] buffer;
	private int positionInBuffer;
	public final Resolver resolver;
	public final char[] tmp;

	public interface Resolver {
		<T> T resolve(Class<T> manifest);
	}

	public PostgresReader() {
		this(null);
	}

	public PostgresReader(Resolver resolver) {
		this.buffer = new char[64];
		this.tmp = new char[48];
		this.resolver = resolver;
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

	public void initBuffer() {
		positionInBuffer = 0;
	}

	public void initBuffer(char c) {
		positionInBuffer = 1;
		buffer[0] = c;
	}

	public void addToBuffer(char c) {
		if (positionInBuffer == buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length * 2);
		}
		buffer[positionInBuffer++] = c;
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
		for(int i = 0; i < count; i++) {
			target[i + offset] = input.charAt(positionInInput + i);
		}
		positionInInput += count;
	}

	public String bufferToString() {
		return new String(buffer, 0, positionInBuffer);
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
}
