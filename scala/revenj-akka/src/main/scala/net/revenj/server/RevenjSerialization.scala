package net.revenj.server

import java.io.{InputStream, OutputStream}
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

import net.revenj.Utils
import net.revenj.serialization.{DslJsonSerialization, Serialization}

import scala.util.{Failure, Try}
import scala.reflect.runtime.universe._

private[revenj] class RevenjSerialization(
  dslJson: DslJsonSerialization,
  loader: ClassLoader
) extends WireSerialization {
  private val passThrough = new PassThroughSerialization()
  private val mirror = runtimeMirror(loader)

  private val NULL = "null".getBytes(StandardCharsets.UTF_8)
  override def serialize(value: Any, stream: OutputStream, contentType: String, manifest: Type): Try[String] = {
    require(contentType == "application/json", "Only application/json content type is supported")
    if (value != null) {
      dslJson.serializeRuntime(value, stream, manifest).map(_ => "application/json")
    } else {
      Try(stream.write(NULL)).map(_ => "application/json")
    }
  }

  override def deserialize[T](manifest: Type, content: Array[Byte], length: Int, contentType: String): Try[T] = {
    require(contentType == "application/json", "Only application/json content type is supported")
    dslJson.deserializeRuntime[T](content, length, manifest)
  }

  override def deserialize[T](manifest: Type, stream: InputStream, contentType: String): Try[T] = {
    require(contentType == "application/json", "Only application/json content type is supported")
    dslJson.deserializeRuntime[T](stream, manifest)
  }

  override def deserialize[T: TypeTag](content: Array[Byte], length: Int, contentType: String): Try[T] = {
    require(contentType == "application/json", "Only application/json content type is supported")
    Utils.findType(mirror.typeOf[T], mirror) match {
      case Some(tpe) => deserialize[T](tpe, content, length, contentType)
      case _ => Failure(new IllegalArgumentException(s"Unable to find Java type for: ${mirror.typeOf[T]}"))
    }
  }

  override def deserialize[T: TypeTag](stream: InputStream, contentType: String): Try[T] = {
    require(contentType == "application/json", "Only application/json content type is supported")
    Utils.findType(mirror.typeOf[T], mirror) match {
      case Some(tpe) => deserialize[T](tpe, stream, contentType)
      case _ => Failure(new IllegalArgumentException(s"Unable to find Java type for: ${mirror.typeOf[T]}"))
    }
  }

  override def find[TFormat: TypeTag](): Option[Serialization[TFormat]] = {
    val format = mirror.typeOf[TFormat]
    if (typeOf[Any] == format || typeOf[AnyRef] == format) Some(passThrough.asInstanceOf[Serialization[TFormat]])
    else if (typeOf[String] == format) Some(dslJson.asInstanceOf[Serialization[TFormat]])
    else None
  }
}
