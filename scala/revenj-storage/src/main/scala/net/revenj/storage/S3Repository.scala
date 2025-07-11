package net.revenj.storage

import software.amazon.awssdk.services.s3.model.DeleteObjectResponse

import java.io.InputStream
import scala.concurrent.Future

trait S3Repository {
	def get(bucket: String, key: String): Future[InputStream]

	def get(s3: S3): Future[InputStream] = get(s3.bucket, s3.key)

	def upload(stream: InputStream, length: Long): Future[S3]

	def upload(
		bucket: String,
		key: String,
		stream: InputStream,
		length: Long,
		name: Option[String] = None,
		mimeType: Option[String] = None,
		metadata: Map[String, String] = Map.empty,
		tags: Map[String, String] = Map.empty): Future[S3]

	def delete(bucket: String, key: String): Future[DeleteObjectResponse]

	def delete(s3: S3): Future[DeleteObjectResponse] = delete(s3.bucket, s3.key)
}