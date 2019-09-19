package net.revenj

import java.io.{InputStream, OutputStream}
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

import net.revenj.RevenjSerialization.PassThroughSerialization
import net.revenj.serialization.{DslJsonSerialization, Serialization, WireSerialization}

import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

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

  override def deserialize(manifest: Type, content: Array[Byte], length: Int, contentType: String): Try[Any] = {
    require(contentType == "application/json", "Only application/json content type is supported")
    dslJson.deserializeRuntime(content, length, manifest)
  }

  override def deserialize(manifest: Type, stream: InputStream, contentType: String): Try[Any] = {
    require(contentType == "application/json", "Only application/json content type is supported")
    dslJson.deserializeRuntime(stream, manifest)
  }

  override def deserialize[T: TypeTag](content: Array[Byte], length: Int, contentType: String): Try[T] = {
    require(contentType == "application/json", "Only application/json content type is supported")
    Utils.findType(mirror.typeOf[T], mirror) match {
      case Some(tpe) => deserialize(tpe, content, length, contentType) match {
        case Success(value: T@unchecked) => Success(value)
        case Failure(ex) => Failure(ex)
      }
      case _ => Failure(new IllegalArgumentException(s"Unable to find Java type for: ${mirror.typeOf[T]}"))
    }
  }

  override def deserialize[T: TypeTag](stream: InputStream, contentType: String): Try[T] = {
    require(contentType == "application/json", "Only application/json content type is supported")
    Utils.findType(mirror.typeOf[T], mirror) match {
      case Some(tpe) => deserialize(tpe, stream, contentType) match {
        case Success(value: T@unchecked) => Success(value)
        case Failure(ex) => Failure(ex)
      }
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
private object RevenjSerialization {
  private class PassThroughSerialization extends Serialization[Any] {
    override def serialize[T: TypeTag](value: T): Try[Any] = Success(value)

    override private[revenj] def serializeRuntime(value: Any, manifest: Type): Try[Any] = Success(value)

    override def deserialize[T: TypeTag](input: Any): Try[T] = Success(input.asInstanceOf[T])

    override private[revenj] def deserializeRuntime[T](input: Any, manifest: Type): Try[T] = Success(input.asInstanceOf[T])
  }
}