package net.revenj.database.postgres.converters

import net.revenj.database.postgres.PostgresWriter

class EnumTuple(val value: String) extends PostgresTuple {
  val mustEscapeRecord = false
  val mustEscapeArray = "NULL" == value

  def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
    sw.write(value)
  }

  override def insertArray(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
    mappings match {
      case Some(m) =>
        var x = 0
        while (x < value.length) {
          m(sw, value.charAt(x))
          x += 1
        }
      case _ => sw.write(value)
    }
  }

  override def buildTuple(quote: Boolean): String = {
    if (quote) "'" + value + "'" else value
  }
}
