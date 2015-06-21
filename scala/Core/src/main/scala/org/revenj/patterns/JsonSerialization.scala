package org.revenj.patterns

import scala.io.Source
import scala.reflect.ClassTag
import scala.xml.Elem
import scala.xml.parsing.ConstructingParser

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class JsonSerialization extends ISerialization[String] {
  //TODO: fix this
  private val dateFormat = DateTimeFormat.forPattern("y-MM-dd'T00:00:00")

  private val dateSerializer = new JsonSerializer[LocalDate] {
    override def serialize(value: LocalDate, generator: JsonGenerator, x: SerializerProvider) =
      generator.writeString(dateFormat.print(value))
  }

  private val dateDeserializer = new JsonDeserializer[LocalDate] {
    override def deserialize(parser: JsonParser, context: DeserializationContext) =
      dateFormat.parseLocalDate(parser.getValueAsString())
  }

// -----------------------------------------------------------------------------

  private val timestampSerializer = new JsonSerializer[DateTime] {
    override def serialize(value: DateTime, generator: JsonGenerator, x: SerializerProvider) =
      generator.writeString(value.toString())
  }

  private val timestampDeserializer = new JsonDeserializer[DateTime] {
    override def deserialize(parser: JsonParser, context: DeserializationContext) =
      new DateTime(parser.getValueAsString())
  }


// -----------------------------------------------------------------------------

  private val bigDecimalSerializer = new JsonSerializer[BigDecimal] {
    override def serialize(value: BigDecimal, generator: JsonGenerator, x: SerializerProvider) =
      generator.writeString(value.toString)
  }

  private val bigDecimalDeserializer = new JsonDeserializer[BigDecimal] {
    override def deserialize(parser: JsonParser, context: DeserializationContext) =
      BigDecimal(parser.getValueAsString())
  }

// -----------------------------------------------------------------------------

  private val elemSerializer = new JsonSerializer[Elem] {
    override def serialize(value: Elem, generator: JsonGenerator, x: SerializerProvider) =
      generator.writeString(value.toString())
  }

  private val elemDeserializer = new JsonDeserializer[Elem] {
    override def deserialize(parser: JsonParser, context: DeserializationContext) =
      ConstructingParser
        .fromSource(Source.fromString(parser.getValueAsString()), true)
        .document.docElem.asInstanceOf[Elem]
  }

// -----------------------------------------------------------------------------

  private val version = new Version(0, 5, 0, "SNAPSHOT", "org.revenj.patterns", "revenj-scala-core")

  private val serializationModule =
    new SimpleModule("SerializationModule", version)
      .addSerializer(classOf[LocalDate], dateSerializer)
      .addSerializer(classOf[DateTime], timestampSerializer)
      .addSerializer(classOf[BigDecimal], bigDecimalSerializer)
      .addSerializer(classOf[Elem], elemSerializer)

  private val serializationMapper =
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .registerModule(serializationModule)

  override def serialize[T](t: T): String =
    serializationMapper.writer.writeValueAsString(t)

// -----------------------------------------------------------------------------

  private val deserializationModule =
    new SimpleModule("DeserializationModule", version)
      .addDeserializer(classOf[LocalDate], dateDeserializer)
      .addDeserializer(classOf[DateTime], timestampDeserializer)
      .addDeserializer(classOf[BigDecimal], bigDecimalDeserializer)
      .addDeserializer(classOf[Elem], elemDeserializer)

  override def deserialize[T](data: String, locator: IServiceLocator)(implicit ev: ClassTag[T]): T =
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .registerModule(deserializationModule)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setInjectableValues(new InjectableValues.Std addValue("__locator", locator))
      .readValue(data, ev.runtimeClass.asInstanceOf[Class[T]])
}
