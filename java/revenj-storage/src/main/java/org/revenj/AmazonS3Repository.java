package org.revenj;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.*;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.revenj.storage.S3Repository;

class AmazonS3Repository implements S3Repository, Closeable {
	private final String bucketName;
	private final ExecutorService executorService;
	private final AmazonS3Client s3Client;
	private final boolean disposeExecutor;

	public AmazonS3Repository(Properties properties, Optional<ExecutorService> executorService) {
		bucketName = properties.getProperty("revenj.s3-bucket-name");
		String s3AccessKey = properties.getProperty("revenj.s3-user");
		String s3SecretKey = properties.getProperty("revenj.s3-secret");
		String s3Region = properties.getProperty("revenj.s3-region");
		disposeExecutor = !executorService.isPresent();
		this.executorService = executorService.orElse(Executors.newSingleThreadExecutor());
		if (s3AccessKey == null || s3AccessKey.isEmpty()) {
			throw new RuntimeException("S3 configuration is missing. Please add revenj.s3-user");
		}
		if (s3SecretKey == null || s3SecretKey.isEmpty()) {
			throw new RuntimeException("S3 configuration is missing. Please add revenj.s3-secret");
		}
		s3Client = new AmazonS3Client(new BasicAWSCredentials(s3AccessKey, s3SecretKey));
		if (s3Region != null) {
			s3Client.setRegion(Region.getRegion(Regions.fromName(s3Region)));
		}
	}

	private String getBucketName(final String name) throws IOException {
		String bn = name == null || name.isEmpty() ? bucketName : name;
		if (bn == null || bn.isEmpty()) {
			throw new IOException("Bucket name not specified for this S3 instance or system wide.\n"
					+ "Either specify revenj.s3-bucket-name in Properties as system wide name or provide a bucket name to this S3 instance");
		}
		return bn;
	}

	@Override
	public Future<InputStream> get(final String bucket, final String key) {
		return executorService.submit(() -> {
			S3Object s3 = s3Client.getObject(new GetObjectRequest(bucket, key));
			return s3.getObjectContent();
		});
	}

	@Override
	public Future<Void> upload(
			String bucket,
			String key,
			InputStream stream,
			long length,
			Map<String, String> metadata) {
		return executorService.submit(() -> {
			ObjectMetadata om = new ObjectMetadata();
			om.setContentLength(length);
			if (metadata != null) {
				for (final Map.Entry<String, String> kv : metadata.entrySet()) {
					om.addUserMetadata(kv.getKey(), kv.getValue());
				}
			}
			s3Client.putObject(new PutObjectRequest(getBucketName(bucket), key, stream, om));
			return null;
		});
	}

	@Override
	public Future<Void> delete(String bucket, String key) {
		return executorService.submit(() -> {
			s3Client.deleteObject(new DeleteObjectRequest(getBucketName(bucket), key));
			return null;
		});
	}

	@Override
	public void close() throws IOException {
		if (disposeExecutor) {
			executorService.shutdown();
		}
	}
}
