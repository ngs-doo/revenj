package net.revenj.serialization

import java.awt.Point
import java.awt.geom.{Point2D, Rectangle2D}
import java.awt.image.BufferedImage
import java.io._
import java.lang.reflect.Type
import javax.imageio.ImageIO

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser, JsonToken}
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import net.revenj.{TreePath, Utils}
import net.revenj.patterns.ServiceLocator

import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

class JacksonSerialization(
  locator: ServiceLocator,
  jackson: Option[ObjectMapper],
  loader: ClassLoader) extends Serialization[String] {

  val mirror = runtimeMirror(loader)
  val mapper = jackson.getOrElse(new ObjectMapper)
    .configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true)
    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .setInjectableValues(new InjectableValues.Std().addValue("__locator", locator))
    .registerModule(new DefaultScalaModule)
    .registerModule(new Jdk8Module)
    .registerModule(new JavaTimeModule)
    .registerModule(new JodaModule)
    .registerModule(JacksonSerialization.withCustomSerializers)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

  private[revenj] def serializeAsBytes(value: Any): Array[Byte] = mapper.writeValueAsBytes(value)

  private[revenj] def serializeTo(value: Any, stream: OutputStream): Unit = {
    mapper.writeValue(stream, value)
  }

  def serialize[T: TypeTag](value: T): Try[String] = {
    Utils.findType(typeOf[T], mirror) match {
      case Some(tpe) => serializeRuntime(value, tpe)
      case _ => Failure(new IllegalArgumentException("Unable to find Java type for: " + typeOf[T]))
    }
  }

  private[revenj] def serializeRuntime(value: Any, manifest: Type): Try[String] = {
    if (value == null) Success("null")
    else Try {
      val javaType = mapper.getTypeFactory.constructType(if (manifest == null) value.getClass else manifest)
      mapper.writerFor(javaType).writeValueAsString(value)
    }
  }

  def serialize(value: Any, stream: OutputStream): Try[Unit] = {
    Try {
      mapper.writeValue(stream, value)
    }
  }

  private[revenj] def deserialize(manifest: Type, content: Array[Byte], length: Int): Try[Any] = {
    Try {
      val javaType = mapper.getTypeFactory.constructType(manifest)
      mapper.readValue[Any](content, 0, length, javaType)
    }
  }

  private[revenj] def deserialize(manifest: Type, stream: InputStream): Try[Any] = {
    Try {
      val javaType = mapper.getTypeFactory.constructType(manifest)
      mapper.readValue[Any](stream, javaType)
    }
  }

  override private[revenj] def deserializeRuntime[T](input: String, manifest: Type): Try[T] = {
    Try {
      val javaType = mapper.getTypeFactory.constructType(manifest)
      mapper.readValue[T](input, javaType)
    }
  }

  override def deserialize[T: TypeTag](input: String): Try[T] = {
    Utils.findType(typeOf[T], mirror) match {
      case Some(tpe) => deserializeRuntime[T](input, tpe)
      case _ => Failure(new IllegalArgumentException("Unable to find Java type for: " + typeOf[T]))
    }
  }
}

object JacksonSerialization {

  private def withCustomSerializers: SimpleModule = {
    val module: SimpleModule = new SimpleModule
    module.addSerializer(classOf[Point], new JsonSerializer[Point]() {
      def serialize(value: Point, jg: JsonGenerator, unused: SerializerProvider): Unit = {
        jg.writeStartObject()
        jg.writeNumberField("X", value.x)
        jg.writeNumberField("Y", value.y)
        jg.writeEndObject()
      }
    })
    module.addSerializer(classOf[Point2D], new JsonSerializer[Point2D]() {
      def serialize(value: Point2D, jg: JsonGenerator, unused: SerializerProvider): Unit = {
        jg.writeStartObject()
        jg.writeNumberField("X", value.getX)
        jg.writeNumberField("Y", value.getY)
        jg.writeEndObject()
      }
    })
    module.addSerializer(classOf[Rectangle2D], new JsonSerializer[Rectangle2D]() {
      def serialize(rect: Rectangle2D, jg: JsonGenerator, unused: SerializerProvider): Unit = {
        jg.writeStartObject()
        jg.writeNumberField("X", rect.getX)
        jg.writeNumberField("Y", rect.getY)
        jg.writeNumberField("Width", rect.getWidth)
        jg.writeNumberField("Height", rect.getHeight)
        jg.writeEndObject()
      }
    })
    module.addSerializer(classOf[BufferedImage], new JsonSerializer[BufferedImage]() {
      def serialize(image: BufferedImage, jg: JsonGenerator, unused: SerializerProvider): Unit = {
        val baos = new ByteArrayOutputStream
        ImageIO.write(image, "png", baos)
        jg.writeBinary(baos.toByteArray)
      }
    })
    module.addSerializer(classOf[TreePath], new JsonSerializer[TreePath]() {
      def serialize(path: TreePath, jg: JsonGenerator, unused: SerializerProvider): Unit = {
        jg.writeString(path.toString)
      }
    })
    module.addDeserializer(classOf[Point], new JsonDeserializer[Point]() {
      def deserialize(parser: JsonParser, unused: DeserializationContext): Point = {
        if (parser.getCurrentToken eq JsonToken.VALUE_STRING) {
          val parts = parser.getValueAsString.split(",")
          if (parts.length == 2) new Point(parts(0).toInt, parts(1).toInt)
          else throw new IOException("Unable to parse \"number,number\" format for point")
        } else {
          val tree = parser.getCodec.readTree[JsonNode](parser)
          var x = tree.get("X")
          if (x == null) x = tree.get("x")
          var y = tree.get("Y")
          if (y == null) y = tree.get("y")
          new Point(if (x != null) x.asInt else 0, if (y != null) y.asInt else 0)
        }
      }
    })
    module.addDeserializer(classOf[Point2D], new JsonDeserializer[Point2D]() {
      def deserialize(parser: JsonParser, unused: DeserializationContext): Point2D = {
        if (parser.getCurrentToken eq JsonToken.VALUE_STRING) {
          val parts = parser.getValueAsString.split(",")
          if (parts.length == 2) new Point2D.Double(parts(0).toDouble, parts(1).toDouble)
          else throw new IOException("Unable to parse \"number,number\" format for point")
        } else {
          val tree = parser.getCodec.readTree[JsonNode](parser)
          var x = tree.get("X")
          if (x == null) x = tree.get("x")
          var y = tree.get("Y")
          if (y == null) y = tree.get("y")
          new Point2D.Double(if (x != null) x.asDouble else 0, if (y != null) y.asDouble else 0)
        }
      }
    })
    module.addDeserializer(classOf[Rectangle2D], new JsonDeserializer[Rectangle2D]() {
      def deserialize(parser: JsonParser, unused: DeserializationContext): Rectangle2D = {
        if (parser.getCurrentToken eq JsonToken.VALUE_STRING) {
          val parts = parser.getValueAsString.split(",")
          if (parts.length == 4) new Rectangle2D.Double(parts(0).toDouble, parts(1).toDouble, parts(2).toDouble, parts(3).toDouble)
          else throw new IOException("Unable to parse \"number,number,number,number\" format for rectangle")
        } else {
          val tree = parser.getCodec.readTree[JsonNode](parser)
          var x = tree.get("X")
          if (x == null) x = tree.get("x")
          var y = tree.get("Y")
          if (y == null) y = tree.get("y")
          var width = tree.get("Width")
          if (width == null) width = tree.get("width")
          var height = tree.get("Height")
          if (height == null) height = tree.get("height")
          new Rectangle2D.Double(if (x != null) x.asDouble else 0, if (y != null) y.asDouble else 0, if (width != null) width.asDouble else 0, if (height != null) height.asDouble else 0)
        }
      }
    })
    module.addDeserializer(classOf[BufferedImage], new JsonDeserializer[BufferedImage]() {
      def deserialize(parser: JsonParser, unused: DeserializationContext): BufferedImage = {
        val is = new ByteArrayInputStream(parser.getBinaryValue)
        ImageIO.read(is)
      }
    })
    module.addDeserializer(classOf[TreePath], new JsonDeserializer[TreePath]() {
      def deserialize(parser: JsonParser, unused: DeserializationContext): TreePath = TreePath.create(parser.getValueAsString)
    })
    module
  }
}
