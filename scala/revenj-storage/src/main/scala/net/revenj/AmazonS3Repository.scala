package net.revenj

import java.io.InputStream
import java.util.{Properties, UUID}

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client}
import net.revenj.storage.{S3, S3Repository}

import scala.concurrent.{ExecutionContext, Future}

class AmazonS3Repository(
  properties: Properties,
  tryExecutionContext: Option[ExecutionContext],
  tryS3: Option[AmazonS3]
) extends S3Repository {

  private val executionContext = tryExecutionContext.getOrElse(ExecutionContext.global)
  private val s3Client = tryS3.getOrElse{
    val s3AccessKey = Option(properties.getProperty("revenj.s3-user"))
    val s3SecretKey = Option(properties.getProperty("revenj.s3-secret"))
    val s3Region = Option(properties.getProperty("revenj.s3-region"))
    require(s3AccessKey.isDefined && s3AccessKey.get.nonEmpty, "S3 configuration is missing. Please add revenj.s3-user")
    require(s3SecretKey.isDefined && s3SecretKey.get.nonEmpty, "S3 configuration is missing. Please add revenj.s3-secret")
    val builder = AmazonS3Client.builder()
    builder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(s3AccessKey.get, s3SecretKey.get)))
    s3Region.foreach(builder.setRegion)
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
      s3Client.getObject(bucket, key).getObjectContent
    }(executionContext)
  }

  override def upload(stream: InputStream, length: Long): Future[S3] = {
    val bn = getBucketName("")
    val key = UUID.randomUUID.toString
    val om = new ObjectMetadata()
    om.setContentLength(length)
    Future {
      s3Client.putObject(new PutObjectRequest(bn, key, stream, om))
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
    metadata: Map[String, String]
  ): Future[S3] = {
    val bn = getBucketName(bucket)
    val om = new ObjectMetadata()
    om.setContentLength(length)
    mimeType.foreach(om.setContentType)
    metadata.foreach { case (k, v) =>
      om.addUserMetadata(k, v)
    }
    Future {
      s3Client.putObject(new PutObjectRequest(bn, key, stream, om))
      S3(bn, key, length, name, mimeType, metadata)
    }(executionContext)
  }

  override def delete(bucket: String, key: String): Future[Unit] = {
    Future {
      s3Client.deleteObject(bucket, key)
    }(executionContext)
  }
}
