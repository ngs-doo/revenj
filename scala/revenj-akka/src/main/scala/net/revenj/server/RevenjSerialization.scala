package net.revenj.server

import java.io.{InputStream, OutputStream}
import java.lang.reflect.Type

import net.revenj.Utils
import net.revenj.serialization.{JacksonSerialization, Serialization}

import scala.util.{Failure, Try}
import scala.reflect.runtime.universe._

private[revenj] class RevenjSerialization(jackson: JacksonSerialization, loader: ClassLoader) extends WireSerialization {
  private val passThrough = new PassThroughSerialization()
  private val mirror = runtimeMirror(loader)

  override def serialize(value: Any, stream: OutputStream, accept: String): Try[String] = {
    jackson.serialize(value, stream).map(_ => "application/json")
  }

  override def deserialize(manifest: Type, content: Array[Byte], length: Int, contentType: String): Try[Any] = {
    jackson.deserialize(manifest, content, length)
  }

  override def deserialize(manifest: Type, stream: InputStream, contentType: String): Try[Any] = {
    jackson.deserialize(manifest, stream).map(_ => "application/json")
  }

  override def deserialize[T: TypeTag](content: Array[Byte], length: Int, contentType: String): Try[T] = {
    Utils.findType(typeOf[T], mirror) match {
      case Some(tpe) => deserialize(tpe, content, length, contentType).map(_.asInstanceOf[T])
      case _ => Failure(new IllegalArgumentException("Unable to find Java type for: " + typeOf[T]))
    }
  }

  override def deserialize[T: TypeTag](stream: InputStream, contentType: String): Try[T] = {
    Utils.findType(typeOf[T], mirror) match {
      case Some(tpe) => deserialize(tpe, stream, contentType).map(_.asInstanceOf[T])
      case _ => Failure(new IllegalArgumentException("Unable to find Java type for: " + typeOf[T]))
    }
  }

  override def find[TFormat: TypeTag](): Option[Serialization[TFormat]] = {
    val format = typeOf[TFormat]
    if (typeOf[Any] == format || typeOf[AnyRef] == format) Some(passThrough.asInstanceOf[Serialization[TFormat]])
    else if (typeOf[String] == format) Some(jackson.asInstanceOf[Serialization[TFormat]])
    else None
  }
}
