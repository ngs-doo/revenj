package net.revenj.server

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}
import java.lang.reflect.Type

import com.dslplatform.json.SerializationException
import net.revenj.Utils
import net.revenj.serialization.Serialization

import scala.util.{Failure, Success, Try}
import scala.reflect.runtime.universe._

trait WireSerialization {
  def serialize(value: Any, stream: OutputStream, contentType: String, manifest: Type): Try[String]
  def serialize(value: Any, stream: OutputStream, contentType: String): Try[String] = {
    if (value != null) serialize(value, stream, contentType, value.getClass)
    else serialize(null, stream, contentType, classOf[AnyRef])
  }

  def serialize(value: Any, accept: String): Try[ByteArrayOutputStream] = {
    Try {
      val os = new ByteArrayOutputStream
      serialize(value, os, accept)
      os
    }
  }

  def deserialize(manifest: Type, content: Array[Byte], length: Int, contentType: String): Try[Any]

  def deserialize[T](manifest: Class[T], content: Array[Byte], length: Int, contentType: String): Try[T] = {
    val signature: Type = manifest
    deserialize(signature, content, length, contentType) match {
      case Success(result) =>
        result match {
          case t: T => Some(t)
          case _ => Failure(new SerializationException(s"Expecting: $manifest. Got: ${if (result != null) result.getClass else "nothing"}"))
        }
      case f@Failure(ex) => Failure(ex)
    }
  }

  def deserialize(manifest: Type, stream: InputStream, contentType: String): Try[Any]

  def deserialize[T](manifest: Class[T], stream: InputStream, contentType: String): Try[T] = {
    val signature: Type = manifest
    deserialize(signature, stream, contentType) match {
      case Success(result) =>
        result match {
          case t: T => Some(t)
          case _ => Failure(new SerializationException(s"Expecting: $manifest. Got: ${if (result != null) result.getClass else "nothing"}"))
        }
      case f@Failure(ex) => Failure(ex)
    }
  }

  def deserialize[T: TypeTag](content: Array[Byte], length: Int, contentType: String): Try[T]

  def deserialize[T: TypeTag](content: Array[Byte], contentType: String): Try[T] = {
    deserialize[T](content, content.length, contentType)
  }

  def deserialize[T: TypeTag](stream: InputStream, contentType: String): Try[T]

  def deserialize[T](content: Array[Byte], length: Int, contentType: String, container: Class[T], argument: Type, arguments: Type*): Try[T] = {
    deserialize(Utils.makeGenericType(container, argument, arguments: _*), content, length, contentType).map(_.asInstanceOf[T])
  }

  def deserialize[T](stream: InputStream, contentType: String, container: Class[T], argument: Type, arguments: Type*): Try[T] = {
    deserialize(Utils.makeGenericType(container, argument, arguments: _*), stream, contentType).map(_.asInstanceOf[T])
  }

  def find[TFormat: TypeTag](): Option[Serialization[TFormat]]
}
