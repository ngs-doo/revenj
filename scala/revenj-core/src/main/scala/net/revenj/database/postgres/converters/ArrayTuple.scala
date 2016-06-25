package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresReader, PostgresWriter}

import scala.collection.mutable.ArrayBuffer

class ArrayTuple(private val elements: Array[PostgresTuple]) extends PostgresTuple {
  private val escapeRecord = elements.length > 1 || elements(0) != null && elements(0).mustEscapeRecord

  val mustEscapeRecord = escapeRecord

  val mustEscapeArray = true

  override def buildTuple(sw: PostgresWriter, quote: Boolean): Unit = {
    val mappings = {
      if (quote) {
        sw.write('\'')
        PostgresTuple.QUOTES
      } else None
    }
    sw.write('{')
    val e = elements(0)
    if (e != null) {
      if (e.mustEscapeArray) {
        sw.write('"')
        e.insertArray(sw, "0", mappings)
        sw.write('"')
      } else e.insertArray(sw, "", mappings)
    } else sw.write("NULL")
    var i = 1
    while (i < elements.length) {
      sw.write(',')
      val e = elements(i)
      if (e != null) {
        if (e.mustEscapeArray) {
          sw.write('"')
          e.insertArray(sw, "0", mappings)
          sw.write('"')
        } else e.insertArray(sw, "", mappings)
      } else sw.write("NULL")
      i += 1
    }
    sw.write('}')
    if (quote) {
      sw.write('\'')
    }
  }

  def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
    sw.write('{')
    val newEscaping = escaping + "0"
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
    val e = elements(0)
    if (e != null) {
      if (e.mustEscapeArray) {
        mapQuote()
        e.insertArray(sw, newEscaping, mappings)
        mapQuote()
      } else e.insertArray(sw, escaping, mappings)
    } else sw.write("NULL")
    var i = 1
    while (i < elements.length) {
      sw.write(',')
      val e = elements(i)
      if (e != null) {
        if (e.mustEscapeArray) {
          mapQuote()
          e.insertArray(sw, newEscaping, mappings)
          mapQuote()
        } else e.insertArray(sw, escaping, mappings)
      } else sw.write("NULL")
      i += 1
    }
    sw.write('}')
  }

  override def insertArray(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
    throw new RuntimeException("Should not happen. Insert array called on array tuple. Nested arrays are invalid construct.")
  }
}

object ArrayTuple {
  val EMPTY: PostgresTuple = new EmptyArrayTuple
  val NULL: PostgresTuple = new NullTuple

  private class EmptyArrayTuple extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write("{}")
    }

    override def insertArray(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      throw new RuntimeException("Should not happen. Insert array called on array tuple. Nested arrays are invalid construct.")
    }

    override def buildTuple(quote: Boolean): String = if (quote) "'{}'" else "{}"
  }

  private class NullTuple extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {}

    override def insertArray(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write("NULL")
    }

    override def buildTuple(quote: Boolean): String = {
      "NULL"
    }
  }

  def apply(elements: Array[PostgresTuple]): PostgresTuple = {
    if (elements == null) {
      NULL
    } else if (elements.length == 0) {
      EMPTY
    } else {
      new ArrayTuple(elements)
    }
  }

  def createIndexed[T](elements: IndexedSeq[T], converter: T => PostgresTuple): PostgresTuple = {
    if (elements == null) {
      NULL
    } else if (elements.isEmpty) {
      EMPTY
    } else {
      val tuples = new Array[PostgresTuple](elements.size)
      var i = 0
      while (i < elements.size) {
        tuples(i) = converter(elements(i))
        i += 1
      }
      new ArrayTuple(tuples)
    }
  }

  def createIndexedOption[T](elements: IndexedSeq[Option[T]], converter: T => PostgresTuple): PostgresTuple = {
    if (elements == null) {
      NULL
    } else if (elements.isEmpty) {
      EMPTY
    } else {
      val tuples = new Array[PostgresTuple](elements.size)
      var i = 0
      while (i < elements.size) {
        elements(i) match {
          case Some(el) => tuples(i) = converter(el)
          case _ => tuples(i) = PostgresTuple.NULL
        }
        i += 1
      }
      new ArrayTuple(tuples)
    }
  }

  def createSeq[T](elements: Seq[T], converter: T => PostgresTuple): PostgresTuple = {
    if (elements == null) {
      NULL
    } else if (elements.isEmpty) {
      EMPTY
    } else {
      val tuples = new Array[PostgresTuple](elements.size)
      var i = 0
      val it = elements.iterator
      while (it.hasNext) {
        tuples(i) = converter(it.next())
        i += 1
      }
      new ArrayTuple(tuples)
    }
  }

  def createSeqOption[T](elements: Seq[Option[T]], converter: T => PostgresTuple): PostgresTuple = {
    if (elements == null) {
      NULL
    } else if (elements.isEmpty) {
      EMPTY
    } else {
      val tuples = new Array[PostgresTuple](elements.size)
      var i = 0
      val it = elements.iterator
      while (it.hasNext) {
        it.next() match {
          case Some(el) => tuples(i) = converter(el)
          case _ => tuples(i) = PostgresTuple.NULL
        }
        i += 1
      }
      new ArrayTuple(tuples)
    }
  }

  def createSet[T](elements: Set[T], converter: T => PostgresTuple): PostgresTuple = {
    if (elements == null) {
      NULL
    } else if (elements.isEmpty) {
      EMPTY
    } else {
      val tuples = new Array[PostgresTuple](elements.size)
      var i = 0
      val it = elements.iterator
      while (it.hasNext) {
        tuples(i) = converter(it.next())
        i += 1
      }
      new ArrayTuple(tuples)
    }
  }

  def createSetOption[T](elements: Set[Option[T]], converter: T => PostgresTuple): PostgresTuple = {
    if (elements == null) {
      NULL
    } else if (elements.isEmpty) {
      EMPTY
    } else {
      val tuples = new Array[PostgresTuple](elements.size)
      var i = 0
      val it = elements.iterator
      while (it.hasNext) {
        it.next() match {
          case Some(el) => tuples(i) = converter(el)
          case _ => tuples(i) = PostgresTuple.NULL
        }
        i += 1
      }
      new ArrayTuple(tuples)
    }
  }

  def parse[T](reader: PostgresReader, context: Int, converter: (PostgresReader, Int) => T, default: () => T): Option[ArrayBuffer[T]] = {
    var cur = reader.read()
    if (cur == ',' || cur == ')') {
      None
    } else {
      val escaped: Boolean = cur != '{'
      if (escaped) {
        reader.read(context)
      }
      cur = reader.peek
      if (cur == '}') {
        if (escaped) {
          reader.read(context + 2)
        } else {
          reader.read(2)
        }
        Some(ArrayBuffer.empty[T])
      } else {
        val result = new ArrayBuffer[T]()
        val arrayContext: Int = Math.max(context << 1, 1)
        val recordContext: Int = arrayContext << 1
        while (cur != -1 && cur != '}') {
          cur = reader.read()
          if (cur == 'N') {
            cur = reader.read(4)
            result += default()
          } else {
            val innerEscaped: Boolean = cur != '('
            if (innerEscaped) {
              reader.read(arrayContext)
            }
            result += converter(reader, recordContext)
            if (innerEscaped) {
              cur = reader.read(arrayContext + 1)
            } else {
              cur = reader.read()
            }
          }
        }
        if (escaped) {
          reader.read(context + 1)
        } else {
          reader.read()
        }
        Some(result)
      }
    }
  }

  def parseOption[T](reader: PostgresReader, context: Int, converter: (PostgresReader, Int) => T): Option[ArrayBuffer[Option[T]]] = {
    var cur = reader.read()
    if (cur == ',' || cur == ')') {
      None
    } else {
      val escaped: Boolean = cur != '{'
      if (escaped) {
        reader.read(context)
      }
      cur = reader.peek
      if (cur == '}') {
        if (escaped) {
          reader.read(context + 2)
        } else {
          reader.read(2)
        }
        Some(new ArrayBuffer[Option[T]](0))
      } else {
        val result = new ArrayBuffer[Option[T]]()
        val arrayContext: Int = Math.max(context << 1, 1)
        val recordContext: Int = arrayContext << 1
        while (cur != -1 && cur != '}') {
          cur = reader.read()
          if (cur == 'N') {
            cur = reader.read(4)
            result += None
          } else {
            val innerEscaped: Boolean = cur != '('
            if (innerEscaped) {
              reader.read(arrayContext)
            }
            result += Some(converter(reader, recordContext))
            if (innerEscaped) {
              cur = reader.read(arrayContext + 1)
            } else {
              cur = reader.read()
            }
          }
        }
        if (escaped) {
          reader.read(context + 1)
        } else {
          reader.read()
        }
        Some(result)
      }
    }
  }
}
