package net.revenj.database.postgres.converters

import net.revenj.database.postgres.PostgresWriter
import net.revenj.database.postgres.converters.PostgresTuple.buildNextEscape

import scala.collection.concurrent.TrieMap

class RecordTuple(properties: Array[PostgresTuple]) extends PostgresTuple {

  override val mustEscapeRecord = true

  override val mustEscapeArray = true

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
    val newEscaping = RecordTuple.nextEscape(escaping)
    lazy val quote = PostgresTuple.buildQuoteEscape(escaping)
    lazy val mapQuote = PostgresTuple.prepareMapping(mappings)
    val p = properties(0)
    if (p != null) {
      if (p.mustEscapeRecord) {
        mapQuote(quote, sw)
        p.insertRecord(sw, newEscaping, mappings)
        mapQuote(quote, sw)
      } else p.insertRecord(sw, escaping, mappings)
    }
    var i = 1
    while (i < properties.length) {
      sw.write(',')
      val p = properties(i)
      if (p != null) {
        if (p.mustEscapeRecord) {
          mapQuote(quote, sw)
          p.insertRecord(sw, newEscaping, mappings)
          mapQuote(quote, sw)
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

  private val escapingCache = new TrieMap[String, String]()
  private[converters] def nextEscape(input: String) = {
    if (input.length < 16) {
      escapingCache.getOrElseUpdate(input, buildNextEscape(input, '1'))
    } else {
      buildNextEscape(input, '1')
    }
  }

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
