package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object ShortConverter extends Converter[Short] {
  override def serializeURI(sw: PostgresBuffer, value: Short): Unit = {
    val offset = NumberConverter.serialize(value, sw.tempBuffer)
    sw.addToBuffer(sw.tempBuffer, offset, 6)
  }

  override val dbName = "int2"

  override def default(): Short = 0

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Short = parseShort(reader, start, ')')

  private def parseShort(reader: PostgresReader, start: Int, matchEnd: Char): Short = {
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
    res.toShort
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Short = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      0
    } else {
      parseShort(reader, cur, '}')
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Short] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      Some(parseShort(reader, cur, '}'))
    }
  }

  override def toTuple(value: Short): PostgresTuple = IntConverter.toTuple(value)
}
