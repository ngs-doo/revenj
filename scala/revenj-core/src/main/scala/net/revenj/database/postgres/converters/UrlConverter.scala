package net.revenj.database.postgres.converters

import java.net.URI

import net.revenj.database.postgres.PostgresReader

object UrlConverter extends Converter[URI] {
  override val dbName = "varchar"

  override def default(): URI = null

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): URI = {
    new URI(StringConverter.parseRaw(reader, start, context))
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): URI = {
    new URI(StringConverter.parseCollectionItem(reader, context))
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[URI] = {
    StringConverter.parseNullableCollectionItem(reader, context).map(new URI(_))
  }

  override def toTuple(value: URI): PostgresTuple = ValueTuple.from(value.toString)
}
