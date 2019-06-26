package net.revenj.database.postgres.converters

import net.revenj.database.postgres.PostgresWriter

import scala.collection.concurrent.TrieMap

trait PostgresTuple {
  val mustEscapeRecord: Boolean

  val mustEscapeArray: Boolean

  def insertRecord(writer: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit

  def insertArray(writer: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
    insertRecord(writer, escaping, mappings)
  }

  def buildTuple(sw: PostgresWriter, quote: Boolean): Unit = {
    if (quote) {
      sw.write('\'')
      insertRecord(sw, "", PostgresTuple.QUOTES)
      sw.write('\'')
    } else insertRecord(sw, "", None)
  }

  def buildTuple(quote: Boolean): String = {
    //TODO: try-with-resource management
    val sw = PostgresTuple.threadWriter.get
    try {
      sw.reset()
      buildTuple(sw, quote)
      sw.toString
    } finally {
      sw.close()
    }
  }
}
object PostgresTuple {
  private val quoteEscape = new TrieMap[String, String]
  private val slashes = {
    val arr = new Array[String](20)
    for (i <- arr.indices) {
      arr(i) = "\\" * (1 << i)
    }
    arr
  }

  private[converters] def prepareMapping(mappings: Option[(PostgresWriter, Char) => Unit]) : (String, PostgresWriter) => Unit = {
    if (mappings.isDefined) quoteMapping(mappings.get)
    else PostgresTuple.noQuoting
  }

  private def quoteMapping(mapping: (PostgresWriter, Char) => Unit)(quote: String, sw: PostgresWriter): Unit = {
    var x = 0
    while (x < quote.length) {
      mapping(sw, quote.charAt(x))
      x += 1
    }
  }

  private def noQuoting(quote: String, sw: PostgresWriter): Unit = {
    sw.write(quote)
  }

  private[converters] def buildNextEscape(input: String, next: Char): String = {
    val sb = new StringBuilder(input.length + 1)
    sb.append(input)
    sb.append(next)
    sb.toString
  }

  val NULL: PostgresTuple = new NullTuple

  private class NullTuple extends PostgresTuple {
    val mustEscapeRecord = false
    val mustEscapeArray = false

    def insertRecord(writer: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {}

    override def insertArray(writer: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      writer.write("NULL")
    }
  }

  protected[converters] val threadWriter = new ThreadLocal[PostgresWriter]() {
    override protected def initialValue: PostgresWriter = {
      new PostgresWriter
    }
  }

  val QUOTES: Option[(PostgresWriter, Char) => Unit] = Some(escapeQuote)

  protected[converters] def escapeQuote(sw: PostgresWriter, c: Char): Unit = {
    if (c == '\'') {
      sw.write('\'')
    }
    sw.write(c)
  }

  protected[converters] def escapeBulkCopy(sw: PostgresWriter, c: Char): Unit = {
    c match {
      case '\\' => sw.write("\\\\")
      case '\t' => sw.write("\\t")
      case '\n' => sw.write("\\n")
      case '\r' => sw.write("\\r")
      case 11 => sw.write("\\v")
      case '\b' => sw.write("\\b")
      case '\f' => sw.write("\\f")
      case _ => sw.write(c)
    }
  }

  def buildQuoteEscape(escaping: String): String = {
    quoteEscape.getOrElseUpdate(escaping, {
      var sb = new StringBuilder
      sb.append('"')
      var j = escaping.length - 1
      while (j >= 0) {
        if (escaping.charAt(j) == '1') {
          val len = sb.length
          var i = 0
          while (i < len) {
            sb.insert(i * 2, sb.charAt(i * 2))
            i += 1
          }
        } else {
          sb = new StringBuilder(sb.toString.replace("\\", "\\\\").replace("\"", "\\\""))
        }
        j -= 1
      }
      sb.toString
    })
  }

  def buildSlashEscape(len: Int): String = {
    if (len < slashes.length) {
      slashes(len)
    } else {
      "\\" * (1 << len)
    }
  }
}
