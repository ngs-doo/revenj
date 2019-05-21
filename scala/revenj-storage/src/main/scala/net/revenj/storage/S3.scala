package net.revenj.storage

import com.fasterxml.jackson.annotation.JsonIgnore
import net.revenj.patterns.Identifiable

case class S3(
  bucket: String,
  key: String,
  length: Long,
  name: Option[String] = None,
  mimeType: Option[String] = None,
  metadata: Map[String, String] = Map.empty
) extends Identifiable {
  @JsonIgnore
  override def URI: String = bucket + ":" + key

  override def hashCode(): Int = bucket.hashCode + key.hashCode
}
