package org.revenj.patterns.postgres;

import java.lang.reflect.Array;
import java.text.ParseException;

import org.pgscala.util.PGArray;
import org.pgscala.util.PGLiteral;
import org.pgscala.util.PGRecord;

import scala.Option;
import scala.Function1;
import scala.Some;
import scala.collection.immutable.IndexedSeq;
import scala.collection.immutable.VectorBuilder;
import scala.collection.immutable.IndexedSeq$;

public abstract class Utils {
	public static String buildURI(final String uris[]) {
		// escaping is a stable function
		if (uris == null) return null;

		int cnt = uris.length; // number of ','s between uris
		if (cnt == 0) throw new IllegalArgumentException("URI list cannot be empty");

		// cache arrays for scanning, to be used later
		final char[][] uriChars = new char[cnt][];

		// calculate the maximum length of the output buffer
		for (int i = 0; i < uris.length; i++) {
			final String uri = uris[i];
			if (uri == null) throw new NullPointerException(
					"URI at index " + i + " was a null value");

			final char[] chars = uri.toCharArray();
			uriChars[i] = chars;

			cnt += chars.length;
			for (final char ch : chars) {
				if (ch == '\\' || ch == '/') cnt++;
			}
		}

		// output buffer
		final char[] joined = new char[cnt];
		int pos = 0;

		for (int i = 0; i < uriChars.length; i++) {
			final char[] chars = uriChars[i];
			for (final char ch : chars) {
				if (ch == '\\' || ch == '/') joined[pos++] = '\\';
				joined[pos++] = ch;
			}
			joined[pos++] = '/';
		}

		return new String(joined, 0, pos - 1);
	}

	public static String buildSimpleUriList(final String uris[]) {
		// escaping is a stable function
		if (uris == null) return null;

		final int ulen = uris.length;
		if (ulen == 0) throw new IllegalArgumentException("URI list cannot be empty");

		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < uris.length; i++) {
			final String uri = uris[i];
			if (uri == null) throw new NullPointerException("Uri part detected as null");
			sb.append(PGLiteral.quote(uri)).append(',');
		}

		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public static String buildCompositeUriList(final String uris[]) {
		// escaping is a stable function
		if (uris == null) return null;

		final int ulen = uris.length;
		if (ulen == 0) throw new IllegalArgumentException("URI list cannot be empty");

		// cache arrays for scanning, to be used later
		final char[][] uriChars = new char[ulen][];

		// calculate the maximum length of the output buffer
		int cnt = (ulen << 2) + ulen; // number of ('') and ,
		for (int i = 0; i < ulen; i++) {
			final String uri = uris[i];
			if (uri == null) throw new NullPointerException(
					"URI at index " + i + " was a null value");

			final char[] chars = uris[i].toCharArray();
			uriChars[i] = chars;

			final int len = chars.length;
			cnt += len;

			for (int j = 0; j < len; j++) {
				final char ch = chars[j];
				if (ch == '\\') {
					if (j < len - 1) { // if not last character in string
						cnt -= 1; // take character after \\
						j++; // skip next char
					} else throw new IllegalArgumentException(
						"URI at index " + i + " contained a single " +
						'\\' + " at the end");
				} else if (ch == '/') {
					cnt += 2; // add ',' if / at end
				} else if (ch == '\'') {
					cnt++; // expand ' to ''
				}
			}
		}

		// output buffer
		final char[] joined = new char[cnt];
		int pos = 0;

		for (int i = 0; i < ulen; i++) {
			// '(' with ('
			joined[pos++] = '(';
			joined[pos++] = '\'';

			final char[] chars = uriChars[i];
			final int len = chars.length;

			for (int j = 0; j < len; j++) {
				final char ch = chars[j];
				if (ch == '\\') {
					if (j < len - 1) { // if not last character in string
						joined[pos++] = chars[j + 1]; // take character after \\
						j++;
					}
				} else if (ch == '\'') {
					// expand ' to ''
					joined[pos++] = ch;
					joined[pos++] = ch;
				} else if (ch == '/') {
					// add ',' instead of /
					joined[pos++] = '\'';
					joined[pos++] = ',';
					joined[pos++] = '\'';
				} else {
					// use character
					joined[pos++] = ch;
				}
			}
			// ')' with ')
			joined[pos++] = '\'';
			joined[pos++] = ')';
			joined[pos++] = ','; // add , if needed
		}

		return new String(joined, 0, pos - 1);
	}

// -----------------------------------------------------------------------------

	// Convertor functions (to sql array)

	public static <T> String createArray(
			final T[] items,
			final Function1<T, String> conv) {
		final String[] parts = new String[items.length];
		for (int i = 0; i < parts.length; i++)
			parts[i] = conv.apply(items[i]);
		return PGArray.pack(parts);
	}

	public static <T> String createArrayPair(
			final scala.Tuple2<T, T>[] items,
			final Function1<T, String> conv) {
		final String[] parts = new String[items.length];
		for (int i = 0; i < parts.length; i++)
			parts[i] = PGRecord.pack(new String[] { conv.apply(items[i]._1()), conv.apply(items[i]._2()) });
		return PGArray.pack(parts);
	}

	public static String createBooleanArray(final boolean[] items) {
		final String[] parts = new String[items.length];
		for (int i = 0; i < parts.length; i++)
			parts[i] = org.pgscala.converters.PGBooleanConverter.toPGString(items[i]);
		return PGArray.pack(parts);
	}

	public static String createIntArray(final int[] items) {
		final String[] parts = new String[items.length];
		for (int i = 0; i < parts.length; i++)
			parts[i] = org.pgscala.converters.PGIntConverter.toPGString(items[i]);
		return PGArray.pack(parts);
	}

	public static String createLongArray(final long[] items) {
		final String[] parts = new String[items.length];
		for (int i = 0; i < parts.length; i++)
			parts[i] = org.pgscala.converters.PGLongConverter.toPGString(items[i]);
		return PGArray.pack(parts);
	}

	public static String createFloatArray(final float[] items) {
		final String[] parts = new String[items.length];
		for (int i = 0; i < parts.length; i++)
			parts[i] = org.pgscala.converters.PGFloatConverter.toPGString(items[i]);
		return PGArray.pack(parts);
	}

	public static String createDoubleArray(final double[] items) {
		final String[] parts = new String[items.length];
		for (int i = 0; i < parts.length; i++)
			parts[i] = org.pgscala.converters.PGDoubleConverter.toPGString(items[i]);
		return PGArray.pack(parts);
	}

	public static <T> String createOptionArray(
			final Option<T[]> items,
			final Function1<T, String> conv) {
		if (items.isEmpty()) return null;
		return createArray(items.get(), conv);
	}

	public static String createOptionBooleanArray(final Option<boolean[]> items) {
		if (items.isEmpty()) return null;
		return createBooleanArray(items.get());
	}

	public static String createOptionIntArray(final Option<int[]> items) {
		if (items.isEmpty()) return null;
		return createIntArray(items.get());
	}

	public static String createOptionLongArray(final Option<long[]> items) {
		if (items.isEmpty()) return null;
		return createLongArray(items.get());
	}

	public static String createOptionFloatArray(final Option<float[]> items) {
		if (items.isEmpty()) return null;
		return createFloatArray(items.get());
	}

	public static String createOptionDoubleArray(final Option<double[]> items) {
		if (items.isEmpty()) return null;
		return createDoubleArray(items.get());
	}

	public static <T> String createCollection(
			final scala.collection.Iterable<T> items,
			final Function1<T, String> conv) {
		final String[] parts = new String[items.size()];
		final scala.collection.Iterator<T> iterator = items.iterator();
		for (int i = 0; iterator.hasNext(); i++)
			parts[i] = conv.apply(iterator.next());
		return PGArray.pack(parts);
	}

// -----------------------------------------------------------------------------

	// Convertor functions (from sql array)

	@SuppressWarnings("unchecked")
	public static <T> T[] parseArray(
			final String array,
			final Function1<String, T> conv,
			final Class<T> clazz) throws ParseException {
		if (array == null || array.isEmpty())
			return (T[]) Array.newInstance(clazz, 0);

		final String[] parts = PGArray.unpack(array);
		final T[] ab = (T[]) Array.newInstance(clazz, parts.length);
		for (int i = 0; i < parts.length; i++)
			ab[i] = conv.apply(parts[i]);
		return ab;
	}

	public static boolean[] parseBooleanArray(final String array) throws ParseException {
		if (array == null || array.isEmpty())
			return new boolean[0];

		final String[] parts = PGArray.unpack(array);
		final boolean[] ab = new boolean[parts.length];
		for (int i = 0; i < parts.length; i++)
			ab[i] = "t".equals(parts[i]);
		return ab;
	}

	public static Option<boolean[]> parseOptionBooleanArray(final String array) throws ParseException {
		if (array == null || array.isEmpty())
			return scala.None$.empty();

		final String[] parts = PGArray.unpack(array);
		final boolean[] ab = new boolean[parts.length];
		for (int i = 0; i < parts.length; i++)
			ab[i] = "t".equals(parts[i]);
		return Some.apply(ab);
	}

	public static int[] parseIntArray(final String array) throws ParseException {
		if (array == null || array.isEmpty())
			return new int[0];

		final String[] parts = PGArray.unpack(array);
		final int[] ab = new int[parts.length];
		for (int i = 0; i < parts.length; i++)
			ab[i] = Integer.parseInt(parts[i]);
		return ab;
	}

	public static Option<int[]> parseOptionIntArray(final String array) throws ParseException {
		if (array == null || array.isEmpty())
			return scala.None$.empty();

		final String[] parts = PGArray.unpack(array);
		final int[] ab = new int[parts.length];
		for (int i = 0; i < parts.length; i++)
			ab[i] = Integer.parseInt(parts[i]);
		return Some.apply(ab);
	}

	public static long[] parseLongArray(final String array) throws ParseException {
		if (array == null || array.isEmpty())
			return new long[0];

		final String[] parts = PGArray.unpack(array);
		final long[] ab = new long[parts.length];
		for (int i = 0; i < parts.length; i++)
			ab[i] = Long.parseLong(parts[i]);
		return ab;
	}

	public static Option<long[]> parseOptionLongArray(final String array) throws ParseException {
		if (array == null || array.isEmpty())
			return scala.None$.empty();

		final String[] parts = PGArray.unpack(array);
		final long[] ab = new long[parts.length];
		for (int i = 0; i < parts.length; i++)
			ab[i] = Long.parseLong(parts[i]);
		return Some.apply(ab);
	}

	public static float[] parseFloatArray(final String array) throws ParseException {
		if (array == null || array.isEmpty())
			return new float[0];

		final String[] parts = PGArray.unpack(array);
		final float[] ab = new float[parts.length];
		for (int i = 0; i < parts.length; i++)
			ab[i] = Float.parseFloat(parts[i]);
		return ab;
	}

	public static Option<float[]> parseOptionFloatArray(final String array) throws ParseException {
		if (array == null || array.isEmpty())
			return scala.None$.empty();

		final String[] parts = PGArray.unpack(array);
		final float[] ab = new float[parts.length];
		for (int i = 0; i < parts.length; i++)
			ab[i] = Float.parseFloat(parts[i]);
		return Some.apply(ab);
	}

	public static double[] parseDoubleArray(final String array) throws ParseException {
		if (array == null || array.isEmpty())
			return new double[0];

		final String[] parts = PGArray.unpack(array);
		final double[] ab = new double[parts.length];
		for (int i = 0; i < parts.length; i++)
			ab[i] = Double.parseDouble(parts[i]);
		return ab;
	}

	public static Option<double[]> parseOptionDoubleArray(final String array) throws ParseException {
		if (array == null || array.isEmpty())
			return scala.None$.empty();

		final String[] parts = PGArray.unpack(array);
		final double[] ab = new double[parts.length];
		for (int i = 0; i < parts.length; i++)
			ab[i] = Double.parseDouble(parts[i]);
		return Some.apply(ab);
	}

	@SuppressWarnings("unchecked")
	public static <T> Option<T[]> parseOptionArray(
			final String array,
			final Function1<String, T> conv,
			final Class<T> clazz) throws ParseException {
		if (array == null || array.isEmpty())
			return (Option<T[]>) (Option<?>) scala.None$.MODULE$;

		return Some.apply(parseArray(array, conv, clazz));
	}

	@SuppressWarnings("unchecked")
	public static <T> IndexedSeq<T> parseIndexedSeq(
			final String array,
			final Function1<String, T> conv) throws ParseException {
		if (array == null || array.isEmpty())
			return (IndexedSeq<T>) IndexedSeq$.MODULE$.empty();

		final String[] parts = PGArray.unpack(array);
		final VectorBuilder<T> vb = new VectorBuilder<T>();
		for (final String part : parts)
			vb.$plus$eq(conv.apply(part));
		return vb.result();
	}

	@SuppressWarnings("unchecked")
	public static <T> Option<IndexedSeq<T>> parseOptionIndexedSeq(
			final String array,
			final Function1<String, T> conv) throws ParseException {
		if (array == null || array.isEmpty())
			return (Option<IndexedSeq<T>>) (Option<?>) scala.None$.MODULE$;

		return Some.apply(parseIndexedSeq(array, conv));
	}

// -----------------------------------------------------------------------------

	// Repository helper functions

	public static <T> String createArrayLiteral(
			final T[] items,
			final Function1<T, String> conv) {
		return PGLiteral.quote(createArray(items, conv));
	}

	public static <T> String createArrayPairLiteral(
			final scala.Tuple2<T, T>[] items,
			final Function1<T, String> conv) {
		return PGLiteral.quote(createArrayPair(items, conv));
	}
}
