package org.revenj.storage;

import org.revenj.patterns.ServiceLocator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class S3 {

	/**
	 * Create new instance of S3.
	 * Upload must be called before persistence to the database.
	 */
	public S3() {
	}

	/**
	 * Create new instance of S3 from provided stream.
	 * Upload will be called immediately. Stream will be read to check for length.
	 *
	 * @param stream Input stream which will be sent to the remote server
	 */
	public static S3 from(InputStream stream, ServiceLocator locator) throws IOException {
		S3 s3 = new S3();
		s3.upload(streamToByteArray(stream), locator);
		return s3;
	}

	public S3(String bucket, String key) throws IOException {
		if (bucket == null) throw new IllegalArgumentException("bucket cannot be null!");
		if (key == null) throw new IllegalArgumentException("key cannot be null!");

		cachedContent = null;

		this.key = key;
		this.bucket = bucket;
	}

	/**
	 * Create new instance of S3 from provided stream.
	 * Upload will be called immediately.
	 *
	 * @param stream Input stream which will be sent to the remote server
	 * @param length size of the stream
	 */
	public static S3 from(InputStream stream, long length, ServiceLocator locator) throws IOException {
		S3 s3 = new S3();
		s3.upload(stream, length, locator);
		return s3;
	}

	/**
	 * Create new instance of S3 from provided byte array.
	 * Upload will be called immediately.
	 *
	 * @param bytes Byte array which will be sent to the remote server
	 */
	public static S3 from(byte[] bytes, ServiceLocator locator) throws IOException {
		S3 s3 = new S3();
		s3.upload(bytes, locator);
		return s3;
	}

	public S3(
			String bucket,
			String key,
			long length,
			String name,
			String mimeType,
			Map<String, String> metadata) {
		this.bucket = bucket;
		this.key = key;
		this.length = length;
		this.name = name;
		this.mimeType = mimeType;
		if (metadata != null) {
			this.metadata.putAll(metadata);;
		}
	}

	private String bucket;

	/**
	 * Bucket under which data will be saved.
	 * By default bucket is defined in the dsl-project.properties file under s3-bucket key
	 *
	 * @return bucket to remote server
	 */
	public String getBucket() {
		return bucket;
	}

	private String key;

	/**
	 * Key for bucket in which the data was saved.
	 *
	 * @return key in bucket on the remote server
	 */
	public String getKey() {
		return key;
	}

	public String getURI() {
		return bucket + ":" + key;
	}

	private long length;

	/**
	 * Byte length of data.
	 *
	 * @return number of bytes
	 */
	public long getLength() {
		return length;
	}

	private String name;

	/**
	 * For convenience, remote data can be assigned a name.
	 *
	 * @return name associated with the remote data
	 */
	public String getName() {
		return name;
	}

	/**
	 * For convenience, remote data can be assigned a name.
	 *
	 * @param value name which will be associated with data
	 * @return itself
	 */
	public S3 setName(final String value) {
		name = value;
		return this;
	}

	private String mimeType;

	/**
	 * For convenience, remote data can be assigned a mime type.
	 *
	 * @return mime type associated with the remote data
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * For convenience, remote data can be assigned a mime type.
	 *
	 * @param value mime type which will be associated with data
	 * @return itself
	 */
	public S3 setMimeType(final String value) {
		mimeType = value;
		return this;
	}

	private final Map<String, String> metadata = new HashMap<>();

	/**
	 * For convenience, various metadata can be associated with the remote data.
	 * Metadata is a map of string keys and values
	 *
	 * @return associated metadata
	 */
	public Map<String, String> getMetadata() {
		return metadata;
	}

	@Override
	public int hashCode() {
		return (bucket != null ? bucket.hashCode() : 0)
				^ (key != null ? key.hashCode() : 0);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || other instanceof S3 == false) return false;
		S3 s3 = (S3) other;
		return Objects.equals(s3.key, key)
				&& Objects.equals(s3.bucket, bucket);
	}

	private byte[] cachedContent;

	/**
	 * Get bytes saved on the remote server.
	 * Data will be cached, so subsequent request will reuse downloaded bytes.
	 *
	 * @return bytes saved on the remote server
	 * @throws IOException in case of communication failure
	 */
	public byte[] getContent(ServiceLocator locator) throws IOException {
		if (cachedContent != null) {
			cachedContent = getBytes(locator);
		}
		return cachedContent;
	}

	/**
	 * Get stream saved on the remote server.
	 * Data will not be cached, so subsequent request will download stream again.
	 *
	 * @return stream saved on the remote server
	 * @throws IOException in case of communication failure
	 */
	public InputStream getStream(ServiceLocator locator) throws IOException {
		try {
			return locator.resolve(S3Repository.class).get(bucket, key).get();
		} catch (final InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Get bytes saved on the remote server.
	 * Data will not be cached, so subsequent request will download bytes again.
	 *
	 * @return bytes saved on the remote server
	 * @throws IOException in case of communication failure
	 */
	public byte[] getBytes(ServiceLocator locator) throws IOException {
		final InputStream stream;
		try {
			stream = locator.resolve(S3Repository.class).get(bucket, key).get();
		} catch (final InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
		return streamToByteArray(stream);
	}

	/**
	 * Upload provided stream to remote S3 server.
	 * If key is already defined, this stream will overwrite remote stream,
	 * otherwise new key will be created.
	 *
	 * @param stream upload provided stream
	 * @return key under which data was saved
	 * @throws IOException in case of communication error
	 */
	public String upload(ByteArrayInputStream stream, ServiceLocator locator) throws IOException {
		return upload(streamToByteArray(stream), locator);
	}

	/**
	 * Upload provided stream to remote S3 server.
	 * If key is already defined, this stream will overwrite remote stream,
	 * otherwise new key will be created.
	 *
	 * @param stream upload provided stream
	 * @param length size of provided stream
	 * @return key under which data was saved
	 * @throws IOException in case of communication error
	 */
	public String upload(InputStream stream, long length, ServiceLocator locator) throws IOException {
		return upload(bucket, stream, length, locator);
	}

	/**
	 * Upload provided stream to remote S3 server.
	 * If key is already defined, this stream will overwrite remote stream,
	 * otherwise new key will be created.
	 * If key was already defined, bucket name can't be changed.
	 *
	 * @param bucket bucket under data will be saved
	 * @param stream upload provided stream
	 * @param length size of provided stream
	 * @return key under which data was saved
	 * @throws IOException in case of communication error
	 */
	public String upload(String bucket, InputStream stream, long length, ServiceLocator locator) throws IOException {
		if (stream == null) throw new IllegalArgumentException("Stream can't be null.");
		if (key == null || key.isEmpty()) {
			this.bucket = bucket;
			key = UUID.randomUUID().toString();
		} else if (!this.bucket.equals(bucket)) {
			throw new IllegalArgumentException("Can't change bucket name");
		}
		try {
			locator.resolve(S3Repository.class).upload(bucket, key, stream, length, metadata).get();
		} catch (final InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
		this.length = length;
		cachedContent = null;
		return key;
	}

	/**
	 * Upload provided bytes to remote S3 server.
	 * If key is already defined, this bytes will overwrite remote bytes,
	 * otherwise new key will be created.
	 *
	 * @param bytes upload provided bytes
	 * @return key under which data was saved
	 * @throws IOException in case of communication error
	 */
	public String upload(byte[] bytes, ServiceLocator locator) throws IOException {
		return upload(bucket, bytes, locator);
	}

	/**
	 * Upload provided bytes to remote S3 server.
	 * If key is already defined, this bytes will overwrite remote bytes,
	 * otherwise new key will be created.
	 * If key was already defined, bucket name can't be changed.
	 *
	 * @param bucket bucket under data will be saved
	 * @param bytes  upload provided bytes
	 * @return key under which data was saved
	 * @throws IOException in case of communication error
	 */
	public String upload(final String bucket, final byte[] bytes, ServiceLocator locator) throws IOException {
		if (bytes == null) throw new IllegalArgumentException("Stream/bytes can't be null.");
		if (key == null || key.isEmpty()) {
			this.bucket = bucket;
			key = UUID.randomUUID().toString();
		} else if (!this.bucket.equals(bucket)) {
			throw new IllegalArgumentException("Can't change bucket name");
		}
		final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		try {
			locator.resolve(S3Repository.class).upload(bucket, key, stream, bytes.length, metadata).get();
		} catch (final InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
		length = bytes.length;
		cachedContent = null;
		return key;
	}

	/**
	 * Remote data from the remote S3 server.
	 *
	 * @throws IOException in case of communication error
	 */
	public void delete(ServiceLocator locator) throws IOException {
		if (key == null || key.isEmpty()) throw new IllegalArgumentException("S3 object is empty.");
		cachedContent = null;
		try {
			locator.resolve(S3Repository.class).delete(bucket, key).get();
		} catch (final InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
		length = 0;
		cachedContent = null;
		key = null;
	}

	private static byte[] streamToByteArray(final InputStream inputStream) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[4096];

		int read;
		while ((read = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, read);
		}

		return baos.toByteArray();
	}
}
