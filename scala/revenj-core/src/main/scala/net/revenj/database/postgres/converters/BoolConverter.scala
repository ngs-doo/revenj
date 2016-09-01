package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object BoolConverter extends Converter[Boolean] {

  override def serializeURI(sw: PostgresBuffer, value: Boolean): Unit = {
    sw.addToBuffer(if (value) "true" else "false")
  }

  val dbName = "bool"

  def default() = false

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Boolean = {
    reader.read()
    start == 't'
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Boolean = {
    val cur = reader.read()
    if (cur == 't') {
      reader.read()
      true
    } else if (cur == 'f') {
      reader.read()
      false
    } else {
      reader.read(4)
      false
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Boolean] = {
    val cur = reader.read()
    if (cur == 't') {
      reader.read()
      Some(true)
    } else if (cur == 'f') {
      reader.read()
      Some(false)
    } else {
      reader.read(4)
      None
    }
  }

  def toTuple(value: Boolean): PostgresTuple = {
    new BooleanTuple(value)
  }

  private class BooleanTuple(val value: Boolean) extends PostgresTuple {
    val charValue = if (value) 't' else 'f'
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write(charValue)
    }

    override def buildTuple(quote: Boolean): String = {
      if (quote) "'" + charValue + "'" else String.valueOf(charValue)
    }
  }

}
