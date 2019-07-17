package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object BoolConverter extends Converter[Boolean] {

  override def serializeURI(sw: PostgresBuffer, value: Boolean): Unit = {
    sw.addToBuffer(if (value) "true" else "false")
  }

  override val dbName = "bool"

  override def default() = false

  private val someTrue = Some(true)
  private val someFalse = Some(false)

  override def parse(reader: PostgresReader, context: Int): Boolean = {
    val cur = reader.read()
    if (cur == ',' || cur == ')') {
      false
    } else {
      reader.read()
      cur == 't'
    }
  }

  override def parseOption(reader: PostgresReader, context: Int): Option[Boolean] = {
    val cur = reader.read()
    if (cur == ',' || cur == ')') {
      None
    } else {
      reader.read()
      if (cur == 't') someTrue
      else someFalse
    }
  }

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
      someTrue
    } else if (cur == 'f') {
      reader.read()
      someFalse
    } else {
      reader.read(4)
      None
    }
  }

  def toTuple(value: Boolean): PostgresTuple = {
    new BooleanTuple(value)
  }

  private class BooleanTuple(val value: Boolean) extends PostgresTuple {
    val charValue: Char = if (value) 't' else 'f'
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write(charValue)
    }

    override def buildTuple(quote: Boolean): String = {
      if (quote) {
        val buf = new Array[Char](3)
        buf(0) = '\''
        buf(1) = charValue
        buf(2) = '\''
        new String(buf, 0, buf.length)
      } else String.valueOf(charValue)
    }
  }

}
