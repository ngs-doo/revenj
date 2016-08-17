package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object IntConverter extends Converter[Int] {
  override def serializeURI(sw: PostgresBuffer, value: Int): Unit = {
    if (value == Int.MinValue) {
      sw.addToBuffer("-2147483648")
    } else {
      val offset = NumberConverter.serialize(value, sw.tempBuffer)
      sw.addToBuffer(sw.tempBuffer, offset, 11)
    }
  }

  val dbName = "int4"

  def default() = 0

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Int = parseInt(reader, start, ')')

  private def parseInt(reader: PostgresReader, start: Int, matchEnd: Char): Int = {
    var res = 0
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

  override def parseCollectionItem(reader: PostgresReader, context: Int): Int = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      0
    } else {
      parseInt(reader, cur, '}')
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Int] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      Some(parseInt(reader, cur, '}'))
    }
  }

  private val MinTuple: PostgresTuple = new ValueTuple("-2147483648", false, false, false)

  override def toTuple(value: Int): PostgresTuple = {
    if (value == Int.MinValue) {
      MinTuple
    } else {
      new IntTuple(value)
    }
  }

  private class IntTuple(val value: Int) extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      val offset = NumberConverter.serialize(value, sw.tmp)
      sw.write(sw.tmp, offset, 11)
    }

    override def buildTuple(quote: Boolean): String = {
      java.lang.Integer.toString(value)
    }
  }

}
