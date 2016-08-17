package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object LongConverter extends Converter[Long] {
  override def serializeURI(sw: PostgresBuffer, value: Long): Unit = {
    if (value == Long.MinValue) {
      sw.addToBuffer("-9223372036854775808")
    } else {
      val offset = NumberConverter.serialize(value, sw.tempBuffer)
      sw.addToBuffer(sw.tempBuffer, offset, 21)
    }
  }

  val dbName = "int8"

  def default() = 0L

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Long = parseLong(reader, start, ')')

  private def parseLong(reader: PostgresReader, start: Int, matchEnd: Char): Long = {
    var res = 0L
    var cur = start
    if (cur == '-') {
      cur = reader.read()
      do {
        res = (res << 3) + (res << 1) - (cur - '0')
        cur = reader.read()
      } while (cur != -1 && cur != ',' && cur != matchEnd)
    } else {
      do {
        res = (res << 3) + (res << 1) + (cur - '0')
        cur = reader.read()
      } while (cur != -1 && cur != ',' && cur != matchEnd)
    }
    res
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Long = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      0L
    } else {
      parseLong(reader, cur, '}')
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Long] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      Some(parseLong(reader, cur, '}'))
    }
  }

  private val MinTuple: PostgresTuple = new ValueTuple("-9223372036854775808", false, false, false)

  override def toTuple(value: Long): PostgresTuple = {
    if (value == Long.MinValue) {
      MinTuple
    } else {
      new LongTuple(value)
    }
  }

  private class LongTuple(val value: Long) extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      val offset = NumberConverter.serialize(value, sw.tmp)
      sw.write(sw.tmp, offset, 21)
    }

    override def buildTuple(quote: Boolean): String = {
      java.lang.Long.toString(value)
    }
  }

}
