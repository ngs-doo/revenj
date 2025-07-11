package net.revenj

import net.revenj.storage.{S3, S3Repository}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model._

import java.io.InputStream
import java.util.{Properties, UUID}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class AmazonS3Repository(
  properties: Properties,
  tryExecutionContext: Option[ExecutionContext],
  tryS3: Option[S3Client]
) extends S3Repository {

  private val executionContext = tryExecutionContext.getOrElse(ExecutionContext.global)
  private val s3Client = tryS3.getOrElse{
    val s3AccessKey = Option(properties.getProperty("revenj.s3-user"))
    val s3SecretKey = Option(properties.getProperty("revenj.s3-secret"))
    val s3Region = Option(properties.getProperty("revenj.s3-region"))
    require(s3AccessKey.isDefined && s3AccessKey.get.nonEmpty, "S3 configuration is missing. Please add revenj.s3-user")
    require(s3SecretKey.isDefined && s3SecretKey.get.nonEmpty, "S3 configuration is missing. Please add revenj.s3-secret")
    val builder = S3Client.builder()
    builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey.get, s3SecretKey.get)))
    s3Region.foreach(r => builder.region(Region.of(r)))
    builder.build()
  }

  private val bucketName = Option(properties.getProperty("revenj.s3-bucket-name"))
  private def getBucketName(name: String) = {
    val bn = if (name == null || name.isEmpty) bucketName else Some(name)
    require(bn.isDefined && bn.get.nonEmpty, """Bucket name not specified for this S3 instance or system wide.
Either specify revenj.s3-bucket-name in Properties as system wide name or provide a bucket name to this S3 instance""")
    bn.get
  }

  override def get(bucket: String, key: String): Future[InputStream] = {
    Future {
      s3Client.getObject(GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build()
      )
    }(executionContext)
  }

  override def upload(stream: InputStream, length: Long): Future[S3] = {
    val bn = getBucketName("")
    val key = UUID.randomUUID.toString
    val putObjectRequest = PutObjectRequest.builder()
      .bucket(bn)
      .key(key)
      .contentLength(length)
      .build()
    Future {
      s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(stream, length))
      S3(bn, key, length)
    }(executionContext)
  }

  override def upload(
    bucket: String,
    key: String,
    stream: InputStream,
    length: Long,
    name: Option[String],
    mimeType: Option[String],
    metadata: Map[String, String],
    tags: Map[String, String]
  ): Future[S3] = {
    val bn = getBucketName(bucket)
    val javaTags = tags.map { case (key, value) =>
      Tag.builder().key(key).value(value).build()
    }.toSet.asJava
    val putObjectRequestBuilder = PutObjectRequest.builder()
      .bucket(bn)
      .key(key)
      .contentLength(length)
      .metadata(metadata.asJava)
      .tagging(Tagging.builder().tagSet(javaTags).build())
    mimeType.foreach(putObjectRequestBuilder.contentType)
    val putObjectRequest = putObjectRequestBuilder.build()
    Future {
      s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(stream, length))
      S3(bn, key, length, name, mimeType, metadata)
    }(executionContext)
  }

  override def delete(bucket: String, key: String): Future[DeleteObjectResponse] = {
    val deleteObjectRequest = DeleteObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .build()
    Future {
      s3Client.deleteObject(deleteObjectRequest)
    }(executionContext)
  }
}
