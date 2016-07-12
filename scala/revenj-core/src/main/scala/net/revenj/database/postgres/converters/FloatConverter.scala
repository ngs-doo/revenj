package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object FloatConverter extends Converter[Float] {

  override def serializeURI(sw: PostgresBuffer, value: Float): Unit = {
    sw.addToBuffer(value.toString)
  }

  val dbName = "real4"

  def default() = 0f

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Float = parseFloat(reader, start, ')')

  private def parseFloat(reader: PostgresReader, cur: Int, matchEnd: Char): Float = {
    reader.initBuffer(cur.toChar)
    reader.fillUntil(',', matchEnd)
    reader.read()
    java.lang.Float.parseFloat(reader.bufferToString())
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Float = {
    val cur = reader.read()
    if (cur == 'N') {
      if (reader.read() == 'U') {
        reader.read(3)
        0
      } else {
        reader.read(2)
        Float.NaN
      }
    } else {
      parseFloat(reader, cur, '}')
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Float] = {
    val cur = reader.read()
    if (cur == 'N') {
      if (reader.read() == 'U') {
        reader.read(3)
        None
      } else {
        reader.read(2)
        Some(Float.NaN)
      }
    } else {
      Some(parseFloat(reader, cur, '}'))
    }
  }

  def toTuple(value: Float): PostgresTuple = {
    new FloatTuple(value)
  }

  private class FloatTuple(val value: Float) extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write(java.lang.Float.toString(value))
    }

    override def buildTuple(quote: Boolean): String = {
      java.lang.Float.toString(value)
    }
  }

}
