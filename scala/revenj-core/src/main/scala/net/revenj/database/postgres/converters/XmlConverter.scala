package net.revenj.database.postgres.converters

import net.revenj.database.postgres.PostgresReader

import scala.xml.{Elem, XML}

object XmlConverter extends Converter[Elem] {
  override val dbName = "xml"

  override def default() = null

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Elem = {
    XML.loadString(StringConverter.parseRaw(reader, start, context))
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Elem = {
    XML.loadString(StringConverter.parseCollectionItem(reader, context))
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Elem] = {
    StringConverter.parseNullableCollectionItem(reader, context).map(XML.loadString)
  }

  override def toTuple(value: Elem): PostgresTuple = ValueTuple.from(value.toString)
}
