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

	public S3() {
	}

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

	public static S3 from(InputStream stream, long length, ServiceLocator locator) throws IOException {
		S3 s3 = new S3();
		s3.upload(stream, length, locator);
		return s3;
	}

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
			this.metadata.putAll(metadata);
		}
	}

	private String bucket;

	public String getBucket() {
		return bucket;
	}

	private String key;

	public String getKey() {
		return key;
	}

	public String getURI() {
		return bucket + ":" + key;
	}

	private long length;

	public long getLength() {
		return length;
	}

	private String name;

	public String getName() {
		return name;
	}

	public S3 setName(final String value) {
		name = value;
		return this;
	}

	private String mimeType;

	public String getMimeType() {
		return mimeType;
	}

	public S3 setMimeType(final String value) {
		mimeType = value;
		return this;
	}

	private final Map<String, String> metadata = new HashMap<>();

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

	public byte[] getContent(ServiceLocator locator) throws IOException {
		if (cachedContent != null) {
			cachedContent = getBytes(locator);
		}
		return cachedContent;
	}

	public InputStream getStream(ServiceLocator locator) throws IOException {
		try {
			return locator.resolve(S3Repository.class).get(bucket, key).get();
		} catch (final InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
	}

	public byte[] getBytes(ServiceLocator locator) throws IOException {
		final InputStream stream;
		try {
			stream = locator.resolve(S3Repository.class).get(bucket, key).get();
		} catch (final InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
		return streamToByteArray(stream);
	}

	public String upload(ByteArrayInputStream stream, ServiceLocator locator) throws IOException {
		return upload(streamToByteArray(stream), locator);
	}

	public String upload(InputStream stream, long length, ServiceLocator locator) throws IOException {
		return upload(bucket, stream, length, locator);
	}

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

	public String upload(byte[] bytes, ServiceLocator locator) throws IOException {
		return upload(bucket, bytes, locator);
	}

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
