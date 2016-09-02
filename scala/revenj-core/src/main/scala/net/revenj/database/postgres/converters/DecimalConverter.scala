package net.revenj.database.postgres.converters

import net.revenj.Utils
import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object DecimalConverter extends Converter[BigDecimal] {
  override def serializeURI(sw: PostgresBuffer, value: BigDecimal): Unit = {
    sw.addToBuffer(value.bigDecimal.toPlainString)
  }

  val dbName = "numeric"

  def default() = Utils.Zero0

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): BigDecimal = parseDecimal(reader, start, ')')

  private def parseDecimal(reader: PostgresReader, cur: Int, matchEnd: Char): BigDecimal = {
    reader.initBuffer(cur.toChar)
    reader.fillUntil(',', matchEnd)
    reader.read()
    BigDecimal(reader.bufferToString()) //TODO: ctor from char, int, int
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): BigDecimal = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      Utils.Zero0
    } else {
      parseDecimal(reader, cur, '}')
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[BigDecimal] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      Some(parseDecimal(reader, cur, '}'))
    }
  }

  override def toTuple(value: BigDecimal): PostgresTuple = {
    new BigDecimalTuple(value)
  }

  private class BigDecimalTuple(val value: BigDecimal) extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write(value.bigDecimal.toPlainString)
    }

    override def buildTuple(quote: Boolean): String = {
      value.bigDecimal.toPlainString
    }
  }

}
