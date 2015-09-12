package org.revenj.patterns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Allow reuse of same byte[] by specifying custom length
 * Array is valid only up until specified length.
 */
public final class Bytes {
	/**
	 * Byte array which can be reused
	 */
	public final byte[] content;
	/**
	 * Length which specifies valid data in array
	 */
	public final int length;

	/**
	 * Create a new instance of Bytes object by providing used array and length which indicates end of valid data.
	 *
	 * @param content data
	 * @param length actual length of array
	 */
	public Bytes(final byte[] content, final int length) {
		this.content = content;
		this.length = length;
	}

	public static Bytes from(final byte[] content) {
		return new Bytes(content, content.length);
	}

	/**
	 * Helper object for providing zero length object (instead of null object)
	 */
	public static final Bytes EMPTY = Bytes.from(new byte[0]);

	/**
	 * Copy bytes to output stream.
	 *
	 * @param stream output stream
	 * @throws IOException
	 */
	public void copyTo(final OutputStream stream) throws IOException {
		stream.write(content, 0, length);
	}

	public ByteArrayInputStream toStream() {
		return new ByteArrayInputStream(content, 0, length);
	}

	private static final Charset utf8 = Charset.forName("UTF-8");

	/**
	 * Utility method for displaying byte[] as UTF-8 string.
	 *
	 * @return string value from UTF-8 charset
	 */
	public String toUtf8() {
		return new String(content, 0, length, utf8);
	}

	/**
	 * Utility method for returning byte[] of the actual size.
	 *
	 * @return copy of original byte[] with expected length
	 */
	public byte[] toByteArray() {
		return Arrays.copyOf(content, length);
	}
}