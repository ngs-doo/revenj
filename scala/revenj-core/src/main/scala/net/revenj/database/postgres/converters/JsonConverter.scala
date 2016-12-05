package net.revenj.database.postgres.converters

import java.io.IOException

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader}
import net.revenj.serialization.Serialization

class JsonConverter(serialization: Serialization[String]) extends Converter[Map[String, Any]] {
  override def serializeURI(sw: PostgresBuffer, value: Map[String, Any]): Unit = {
    if (value.isEmpty) {
      sw.addToBuffer("{}")
    } else {
      StringConverter.serializeURI(sw, serialization.serialize(value).getOrElse(throw new RuntimeException(s"Unable to serialize provided map: $value")))
    }
  }

  val dbName = "jsonb"

  def default() = Map.empty

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Map[String, Any] = {
    toMap(StringConverter.parseRaw(reader, start, context))
  }

  private def toMap(value: String) = {
    serialization.deserialize[Map[String, Any]](value).getOrElse(throw new RuntimeException("Unable to deserialize JsonB into map"))
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Map[String, Any] = {
    val value = StringConverter.parseCollectionItem(reader, context)
    if (value.isEmpty) Map.empty
    else toMap(value)
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Map[String, Any]] = {
    StringConverter.parseNullableCollectionItem(reader, context) match {
      case Some(value) =>
        if (value.isEmpty) Some(Map.empty)
        else Some(toMap(value))
      case _ => None
    }
  }

  private val EmptyTuple: PostgresTuple = ValueTuple.from("{}")

  override def toTuple(value: Map[String, Any]): PostgresTuple = {
    if (value.isEmpty) {
      EmptyTuple
    } else {
      ValueTuple.from(serialization.serialize(value).getOrElse(throw new IOException(s"Unable to serialize provided map: $value")))
    }
  }
}
