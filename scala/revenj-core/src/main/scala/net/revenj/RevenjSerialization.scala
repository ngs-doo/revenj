package net.revenj

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream}
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import net.revenj.RevenjSerialization.PassThroughSerialization
import net.revenj.serialization.{DslJsonSerialization, JacksonSerialization, Serialization, WireSerialization}

import java.util.Properties
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

private[revenj] class RevenjSerialization(
  dslJson: DslJsonSerialization,
  jackson: JacksonSerialization,
  properties: Properties,
  loader: ClassLoader
) extends WireSerialization {
  private val passThrough = new PassThroughSerialization()
  private val mirror = runtimeMirror(loader)

  private val useDslJson = properties.getProperty("revenj.serialization") == "dsl-json"

  import RevenjSerialization._

  private val sharedBuffer = new ThreadLocal[Array[Byte]] {
    override def initialValue(): Array[Byte] = new Array[Byte](4096)
  }

  private val arrayFormat = mirror.typeOf[Array[Byte]]
  private val streamFormat = mirror.typeOf[InputStream]

  private def inputToOutput(input: InputStream, output: OutputStream): Unit = {
    val buffer = sharedBuffer.get()
    var read = 0
    while ({read = input.read(buffer); read != -1}) {
      output.write(buffer, 0, read)
    }
  }

  override def serialize(value: Any, stream: OutputStream, contentType: String, manifest: Type): Try[String] = {
    value match {
      case byte: Array[Byte] if isBinary(contentType) =>
        stream.write(byte)
        Success(contentType)
      case is: InputStream =>
        inputToOutput(is, stream)
        Success(contentType)
      case opt: Option[_] if opt.isDefined => serialize(opt.get, stream, contentType, manifest)
      case _ =>
        require(contentType == "application/json", "Only application/json content type is supported")
        if (value != null) {
          if (useDslJson) {
            dslJson.serializeRuntime(value, stream, manifest).map(_ => "application/json")
          } else {
            jackson.serialize(value, stream, manifest).map(_ => "application/json")
          }
        } else {
          Try(stream.write(Null)).map(_ => "application/json")
        }
    }
  }

  override def deserialize(manifest: Type, content: Array[Byte], length: Int, contentType: String): Try[Any] = {
    if (isBinary(contentType)) {
      Success(content)
    } else {
      require(contentType == "application/json", "Only application/json content type is supported")
      if (useDslJson) {
        dslJson.deserializeRuntime(content, length, manifest)
      } else {
        jackson.deserialize(manifest, content, length)
      }
    }
  }

  override def deserialize(manifest: Type, stream: InputStream, contentType: String): Try[Any] = {
    if (isBinary(contentType)) {
      Success(stream)
    } else {
      require(contentType == "application/json", "Only application/json content type is supported")
      if (useDslJson) {
        dslJson.deserializeRuntime(stream, manifest)
      } else {
        jackson.deserialize(manifest, stream)
      }
    }
  }

  override def deserialize[T: TypeTag](content: Array[Byte], length: Int, contentType: String): Try[T] = {
    val format = mirror.typeOf[T]
    Utils.findType(format, mirror) match {
      case Some(tpe) =>
        if (isBinary(contentType)) {
          if (format == arrayFormat) {
            Success(content.asInstanceOf[T])
          } else if (format == streamFormat) {
            Success(new ByteArrayInputStream(content).asInstanceOf[T])
          } else {
            throw new IllegalArgumentException("Invalid format specified. Only Array[Byte] and InputStream supported")
          }
        } else {
          require(contentType == "application/json", "Only application/json content type is supported")
          deserialize(tpe, content, length, contentType) match {
            case Success(value: T@unchecked) => Success(value)
            case Failure(ex) => Failure(ex)
          }
        }
      case _ =>
        Failure(new IllegalArgumentException(s"Unable to find Java type for: ${mirror.typeOf[T]}"))
    }
  }

  override def deserialize[T: TypeTag](stream: InputStream, contentType: String): Try[T] = {
    val format = mirror.typeOf[T]
    Utils.findType(format, mirror) match {
      case Some(tpe) =>
        if (isBinary(contentType)) {
          if (format == arrayFormat) {
            val baos = new ByteArrayOutputStream()
            inputToOutput(stream, baos)
            Success(baos.toByteArray.asInstanceOf[T])
          } else if (format == streamFormat) {
            Success(stream.asInstanceOf[T])
          } else {
            throw new IllegalArgumentException("Invalid format specified. Only Array[Byte] and InputStream supported")
          }
        } else {
          require(contentType == "application/json", "Only application/json content type is supported")
          deserialize(tpe, stream, contentType) match {
            case Success(value: T@unchecked) => Success(value)
            case Failure(ex) => Failure(ex)
          }
        }
      case _ =>
        Failure(new IllegalArgumentException(s"Unable to find Java type for: ${mirror.typeOf[T]}"))
    }
  }

  override def find[TFormat: TypeTag](): Option[Serialization[TFormat]] = {
    val format = mirror.typeOf[TFormat]
    if (typeOf[Any] == format || typeOf[AnyRef] == format) {
      Some(passThrough.asInstanceOf[Serialization[TFormat]])
    } else if (typeOf[String] == format) {
      if (useDslJson) Some(dslJson.asInstanceOf[Serialization[TFormat]])
      else Some(jackson.asInstanceOf[Serialization[TFormat]])
    } else {
      None
    }
  }
}
private object RevenjSerialization {
  private val Null = "null".getBytes(StandardCharsets.UTF_8)

  private def isBinary(contentType: String): Boolean = {
    contentType.startsWith("image/") || contentType.startsWith("vide/") || Binary.contains(contentType)
  }

  private val Binary = Set(
    "application/octet-stream",
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-word.document.macroEnabled.12",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.ms-excel.sheet.macroEnabled.12",
    "application/vnd.ms-powerpoint",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/vnd.ms-powerpoint.presentation.macroEnabled.12"
  )

  private class PassThroughSerialization extends Serialization[Any] {
    override def serialize[T: TypeTag](value: T): Try[Any] = Success(value)

    override private[revenj] def serializeRuntime(value: Any, manifest: Type): Try[Any] = Success(value)

    override def deserialize[T: TypeTag](input: Any): Try[T] = Success(input.asInstanceOf[T])

    override private[revenj] def deserializeRuntime[T](input: Any, manifest: Type): Try[T] = Success(input.asInstanceOf[T])
  }
}