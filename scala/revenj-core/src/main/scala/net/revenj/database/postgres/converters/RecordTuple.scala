package net.revenj.database.postgres.converters

import net.revenj.database.postgres.PostgresWriter

class RecordTuple(properties: Array[PostgresTuple]) extends PostgresTuple {

  val mustEscapeRecord = true

  val mustEscapeArray = true

  override def buildTuple(quote: Boolean): String = {
    val sw = PostgresTuple.threadWriter.get
    sw.reset()
    val mappings = {
      if (quote) {
        sw.write('\'')
        PostgresTuple.QUOTES
      } else None
    }
    sw.write('(')
    val p = properties(0)
    if (p != null) {
      if (p.mustEscapeRecord) {
        sw.write('"')
        p.insertRecord(sw, "1", mappings)
        sw.write('"')
      } else p.insertRecord(sw, "", mappings)
    }
    var i = 1
    while (i < properties.length) {
      sw.write(',')
      val p = properties(i)
      if (p != null) {
        if (p.mustEscapeRecord) {
          sw.write('"')
          p.insertRecord(sw, "1", mappings)
          sw.write('"')
        } else p.insertRecord(sw, "", mappings)
      }
      i += 1
    }
    sw.write(')')
    if (quote) {
      sw.write('\'')
    }
    sw.toString
  }

  def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
    sw.write('(')
    val newEscaping = escaping + '1'
    lazy val quote = PostgresTuple.buildQuoteEscape(escaping)
    lazy val mapQuote = mappings match {
      case Some(m) => () => {
        var x = 0
        while (x < quote.length) {
          m(sw, quote.charAt(x))
          x += 1
        }
      }
      case _ => () => sw.write(quote)
    }
    val p = properties(0)
    if (p != null) {
      if (p.mustEscapeRecord) {
        mapQuote()
        p.insertRecord(sw, newEscaping, mappings)
        mapQuote()
      } else p.insertRecord(sw, escaping, mappings)
    }
    var i = 1
    while (i < properties.length) {
      sw.write(',')
      val p = properties(i)
      if (p != null) {
        if (p.mustEscapeRecord) {
          mapQuote()
          p.insertRecord(sw, newEscaping, mappings)
          mapQuote()
        } else p.insertRecord(sw, escaping, mappings)
      }
      i += 1
    }
    sw.write(')')
  }
}

object RecordTuple {
  val EMPTY: PostgresTuple = new EmptyRecordTuple()
  val NULL: PostgresTuple = new NullTuple()

  def apply(properties: Array[PostgresTuple]): PostgresTuple = {
    if (properties == null) RecordTuple.NULL
    else if (properties.length == 0) RecordTuple.EMPTY
    else new RecordTuple(properties)
  }

  private class EmptyRecordTuple extends PostgresTuple {
    val mustEscapeRecord = true

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write("()")
    }

    override def insertArray(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write("()")
    }

    override def buildTuple(quote: Boolean): String = if (quote) "'()'" else "()"
  }

  private class NullTuple extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {}

    override def insertArray(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write("NULL")
    }

    override def buildTuple(quote: Boolean): String = "NULL"
  }
}
