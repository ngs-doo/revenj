package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object DoubleConverter extends Converter[Double] {

  override def serializeURI(sw: PostgresBuffer, value: Double): Unit = {
    sw.addToBuffer(value.toString)
  }

  val dbName = "real8"

  def default() = 0d

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Double = parseDouble(reader, start, ')')

  private def parseDouble(reader: PostgresReader, cur: Int, matchEnd: Char): Double = {
    reader.initBuffer(cur.toChar)
    reader.fillUntil(',', matchEnd)
    reader.read()
    java.lang.Double.parseDouble(reader.bufferToString())
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Double = {
    val cur = reader.read()
    if (cur == 'N') {
      if (reader.read() == 'U') {
        reader.read(3)
        0
      } else {
        reader.read(2)
        Double.NaN
      }
    } else {
      parseDouble(reader, cur, '}')
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Double] = {
    val cur = reader.read()
    if (cur == 'N') {
      if (reader.read() == 'U') {
        reader.read(3)
        None
      } else {
        reader.read(2)
        Some(Double.NaN)
      }
    } else {
      Some(parseDouble(reader, cur, '}'))
    }
  }

  def toTuple(value: Double): PostgresTuple = {
    new DoubleTuple(value)
  }

  private class DoubleTuple(val value: Double) extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write(java.lang.Double.toString(value))
    }

    override def buildTuple(quote: Boolean): String = {
      java.lang.Double.toString(value)
    }
  }

}
