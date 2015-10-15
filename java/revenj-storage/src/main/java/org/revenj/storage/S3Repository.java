package org.revenj.storage;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Future;

public interface S3Repository {
	Future<InputStream> get(String bucket, String key);

	default Future<InputStream> get(S3 s3) {
		return get(s3.getBucket(), s3.getKey());
	}

	Future<Void> upload(
			String bucket,
			String key,
			InputStream stream,
			long length,
			Map<String, String> metadata);

	Future<Void> delete(String bucket, String key);

	default Future<Void> delete(S3 s3) {
		return delete(s3.getBucket(), s3.getKey());
	}
}