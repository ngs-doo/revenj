package net.revenj.serialization

import java.lang.reflect
import java.nio.charset.StandardCharsets

import com.dslplatform.json.{ConfigurationException, ConfigureScala, DslJson, DslJsonScala, JsonReader, JsonWriter}
import com.dslplatform.json.runtime.Settings
import net.revenj.patterns.ServiceLocator

import scala.reflect.runtime.universe
import scala.util.{Failure, Success, Try}

class DslJsonSerialization(
  locator: ServiceLocator,
  settings: Option[DslJson.Settings[ServiceLocator]],
  json: Option[DslJson[ServiceLocator]]
) extends Serialization[String] {

  private val dslJson = {
    val dslSettings = settings.getOrElse(Settings.withRuntime().withContext(locator).`with`(new ConfigureScala).includeServiceLoader())
    json.getOrElse(new DslJson[ServiceLocator](dslSettings))
  }
  private val dslJsonScala = new DslJsonScala(dslJson)

  private val localWriter = ThreadLocal.withInitial[JsonWriter](() => dslJson.newWriter(8192))
  private val localReader = ThreadLocal.withInitial[JsonReader[ServiceLocator]](() => dslJson.newReader())

  override def serialize[T: universe.TypeTag](value: T): Try[String] = {
    Try(dslJsonScala.encoder[T]) match {
      case Success(encoder) =>
        val writer = localWriter.get()
        writer.reset()
        try {
          encoder.write(writer, value)
          Success(writer.toString)
        } catch {
          case ex: Throwable => Failure(ex)
        }
      case Failure(ex) =>
        Failure(new ConfigurationException(ex.getMessage))
    }
  }

  override private[revenj] def serializeRuntime(value: Any, manifest: reflect.Type): Try[String] = {
    val encoder = dslJson.tryFindWriter(manifest).asInstanceOf[JsonWriter.WriteObject[Any]]
    if (encoder == null) {
      Failure(new ConfigurationException(s"Unable to find encoder for $manifest"))
    } else {
      val writer = localWriter.get()
      writer.reset()
      try {
        encoder.write(writer, value)
        Success(writer.toString)
      } catch {
        case ex: Throwable => Failure(ex)
      }
    }
  }

  override def deserialize[T: universe.TypeTag](input: String): Try[T] = {
    Try(dslJsonScala.decoder[T]) match {
      case Success(decoder) =>
        val bytes = input.getBytes(StandardCharsets.UTF_8)
        val reader = localReader.get().process(bytes, bytes.length)
        Try {
          reader.read()
          decoder.read(reader)
        }
      case Failure(ex) =>
        Failure(new ConfigurationException(ex.getMessage))
    }
  }

  override private[revenj] def deserializeRuntime[T](input: String, manifest: reflect.Type) = {
    val decoder = dslJson.tryFindReader(manifest).asInstanceOf[JsonReader.ReadObject[T]]
    if (decoder == null) {
      Failure(new ConfigurationException(s"Unable to find decoder for $manifest"))
    } else {
        val bytes = input.getBytes(StandardCharsets.UTF_8)
        val reader = localReader.get().process(bytes, bytes.length)
        Try {
          reader.read()
          decoder.read(reader)
        }
    }
  }
}
