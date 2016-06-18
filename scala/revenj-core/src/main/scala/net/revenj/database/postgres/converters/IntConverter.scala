package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object IntConverter extends Converter[Int] {
  def serializeURI(sw: PostgresBuffer, value: Int): Unit = {
    if (value == Integer.MIN_VALUE) {
      sw.addToBuffer("-2147483648")
    } else {
      val offset = NumberConverter.serialize(value, sw.tempBuffer)
      sw.addToBuffer(sw.tempBuffer, offset, 11)
    }
  }

  def serializeURI(sw: PostgresBuffer, value: Option[Int]): Unit = {
    if (value.isDefined) {
      serializeURI(sw, value.get)
    }
  }

  val dbName = "int4"

  override def parseRaw(reader: PostgresReader, context: Int, canBeNull: Boolean): Int = parse(reader)

  def parseOption(reader: PostgresReader): Option[Int] = {
    val cur = reader.read()
    if (cur == ',' || cur == ')') {
      None
    } else {
      Some(parseInt(reader, cur, ')'))
    }
  }

  def parse(reader: PostgresReader): Int = {
    val cur: Int = reader.read()
    if (cur == ',' || cur == ')') {
      0
    } else {
      parseInt(reader, cur, ')')
    }
  }

  private def parseInt(reader: PostgresReader, start: Int, matchEnd: Char): Int = {
    var res = 0
    var cur = start
    if (cur == '-') {
      cur = reader.read()
      do {
        res = (res << 3) + (res << 1) - (cur - 48)
        cur = reader.read()
      } while (cur != -1 && cur != ',' && cur != matchEnd)
    } else {
      do {
        res = (res << 3) + (res << 1) + (cur - 48)
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

  private val MIN_TUPLE: PostgresTuple = new ValueTuple("-2147483648", false, false, false)

  override def toTuple(value: Int): PostgresTuple = {
    if (value == Integer.MIN_VALUE) {
      MIN_TUPLE
    } else {
      new IntTuple(value)
    }
  }

  private[converters] class IntTuple(val value: Int) extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      val offset = NumberConverter.serialize(value, sw.tmp)
      sw.write(sw.tmp, offset, 11)
    }

    override def buildTuple(quote: Boolean): String = {
      value.toString
    }
  }
}
