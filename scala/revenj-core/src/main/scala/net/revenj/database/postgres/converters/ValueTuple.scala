package net.revenj.database.postgres.converters

import net.revenj.database.postgres.PostgresWriter

class ValueTuple(value: String, hasMarkers: Boolean, val mustEscapeRecord: Boolean, val mustEscapeArray: Boolean) extends PostgresTuple {

  override def buildTuple(quote: Boolean): String = {
    if (value == null) "NULL"
    else if (quote) "'" + value.replace("'", "''") + "'"
    else value
  }

  private def escape(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
    lazy val quoteEscape = PostgresTuple.buildQuoteEscape(escaping)
    lazy val slashEscape = PostgresTuple.buildSlashEscape(escaping.length)
    mappings match {
      case Some(m) =>
        var i = 0
        while (i < value.length) {
          val c = value.charAt(i)
          if (c == '"') {
            var x = 0
            while (x < quoteEscape.length) {
              m(sw, quoteEscape.charAt(x))
              x += 1
            }
          } else if (c == '\\') {
            var x = 0
            while (x < slashEscape.length) {
              m(sw, slashEscape.charAt(x))
              x += 1
            }
          } else m(sw, c)
          i += 1
        }
      case _ =>
        var i = 0
        while (i < value.length) {
          val c = value.charAt(i)
          if (c == '"') {
            sw.write(quoteEscape)
          } else if (c == '\\') {
            sw.write(slashEscape)
          } else sw.write(c)
          i += 1
        }
    }
  }

  def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
    if (hasMarkers) escape(sw, escaping, mappings)
    else mappings match {
      case Some(m) =>
        var x = 0
        while (x < value.length) {
          m(sw, value.charAt(x))
          x += 1
        }
      case _ if value != null => sw.write(value)
      case _ =>
    }
  }

  override def insertArray(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
    if (value == null) sw.write("NULL")
    else if (hasMarkers) escape(sw, escaping, mappings)
    else mappings match {
      case Some(m) =>
        var x = 0
        while (x < value.length) {
          m(sw, value.charAt(x))
          x += 1
        }
      case _ => sw.write(value)
    }
  }
}

object ValueTuple {
  val EMPTY: PostgresTuple = new EmptyValueTuple()

  private class EmptyValueTuple extends PostgresTuple {
    val mustEscapeRecord = true

    val mustEscapeArray = true

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {}

    override def buildTuple(quote: Boolean): String = if (quote) "'\"\"'" else "\"\""
  }

  def apply(value: String) = {
    if (value != null) {
      var hasMarkers = false
      var escapeRecord = value.length == 0
      var escapeArray = value.length == 0 || value == "NULL"
      var hasWhitespace = false
      var i = 0
      while (i < value.length) {
        val c = value.charAt(i)
        if (c == '\\' || c == '"') {
          hasMarkers = true
          escapeRecord = true
          escapeArray = true
          i = value.length
        } else if (c == ',') {
          escapeRecord = true
          escapeArray = true
        } else if (c == '(' || c == ')') {
          escapeRecord = true
        } else if (c == '{' || c == '}') {
          escapeArray = true
        } else if (!hasWhitespace) {
          hasWhitespace = Character.isWhitespace(c)
        }
        i += 1
      }
      new ValueTuple(value, hasMarkers, hasMarkers || escapeRecord || hasWhitespace, hasMarkers || escapeArray || hasWhitespace)
    } else {
      new ValueTuple(null, false, false, true)
    }
  }

  def from(value: String): PostgresTuple = {
    if (value == null) PostgresTuple.NULL
    else if (value.length == 0) EMPTY
    else apply(value)
  }

  def apply(value: String, record: Boolean, array: Boolean): Unit = {
    new ValueTuple(value, hasMarkers = record || array, record, array)
  }
}
