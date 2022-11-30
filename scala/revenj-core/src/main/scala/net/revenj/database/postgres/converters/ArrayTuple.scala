package net.revenj.database.postgres.converters

import net.revenj.database.postgres.PostgresWriter.{CollectionDiff, NewTuple}
import net.revenj.database.postgres.converters.PostgresTuple.buildNextEscape
import net.revenj.database.postgres.{PostgresReader, PostgresWriter}
import net.revenj.patterns.{Equality, Identifiable}
import org.postgresql.util.PGobject

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

class ArrayTuple(private val elements: Array[PostgresTuple]) extends PostgresTuple {
  private val escapeRecord = elements.length > 1 || elements(0) != null && elements(0).mustEscapeRecord

  override val mustEscapeRecord: Boolean = escapeRecord

  override val mustEscapeArray: Boolean = true

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
    val newEscaping = ArrayTuple.nextEscape(escaping)
    lazy val quote = PostgresTuple.buildQuoteEscape(escaping)
    lazy val mapQuote = PostgresTuple.prepareMapping(mappings)
    val e = elements(0)
    if (e != null) {
      if (e.mustEscapeArray) {
        mapQuote(quote, sw)
        e.insertArray(sw, newEscaping, mappings)
        mapQuote(quote, sw)
      } else e.insertArray(sw, escaping, mappings)
    } else sw.write("NULL")
    var i = 1
    while (i < elements.length) {
      sw.write(',')
      val e = elements(i)
      if (e != null) {
        if (e.mustEscapeArray) {
          mapQuote(quote, sw)
          e.insertArray(sw, newEscaping, mappings)
          mapQuote(quote, sw)
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

  private val escapingCache = new TrieMap[String, String]()
  private[converters] def nextEscape(input: String) = {
    if (input.length < 16) {
      escapingCache.getOrElseUpdate(input, buildNextEscape(input, '0'))
    } else {
      buildNextEscape(input, '0')
    }
  }

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

  def createIndexed[T](elements: scala.collection.IndexedSeq[T], converter: T => PostgresTuple): PostgresTuple = {
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

  def createIndexedOption[T](elements: scala.collection.IndexedSeq[Option[T]], converter: T => PostgresTuple): PostgresTuple = {
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

  def createSeq[T](elements: scala.collection.Seq[T], converter: T => PostgresTuple): PostgresTuple = {
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

  def createSeqOption[T](elements: scala.collection.Seq[Option[T]], converter: T => PostgresTuple): PostgresTuple = {
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

  def createSet[T](elements: scala.collection.Set[T], converter: T => PostgresTuple): PostgresTuple = {
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

  def createSetOption[T](elements: scala.collection.Set[Option[T]], converter: T => PostgresTuple): PostgresTuple = {
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

  private val someEmptyCollection = Some(scala.collection.IndexedSeq.empty[Nothing])
  private val someEmptyOptionCollection = Some(scala.collection.IndexedSeq.empty[Option[Nothing]])

  def parse[T](reader: PostgresReader, context: Int, converter: (PostgresReader, Int) => T, default: () => T): Option[scala.collection.IndexedSeq[T]] = {
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
        someEmptyCollection
      } else {
        val result = new mutable.ArrayBuffer[T](4)
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

  def parseOption[T](reader: PostgresReader, context: Int, converter: (PostgresReader, Int) => T): Option[scala.collection.IndexedSeq[Option[T]]] = {
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
        someEmptyOptionCollection
      } else {
        val result = new mutable.ArrayBuffer[Option[T]](4)
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

  def toParameter(sw: PostgresWriter, arrayType: String, data: Iterable[Array[PostgresTuple]]): PGobject = {
    sw.reset()
    val array = new ArrayTuple(data.map(a => RecordTuple(a)).toArray)
    val pgo = new PGobject
    pgo.setType(arrayType)
    array.buildTuple(sw, false)
    pgo.setValue(sw.bufferToString())
    sw.reset()
    pgo
  }

  private def emptyPGO(arrayType: String): PGobject = {
    val pgo = new PGobject
    pgo.setType(arrayType)
    pgo
  }

  def toParameterSimple[T](
    sw: PostgresWriter,
    collection: scala.collection.Seq[T],
    arrayType: String,
    toTuple: T => PostgresTuple
  ): PGobject = {
    if (collection.nonEmpty) {
      toParameter(
        sw,
        arrayType,
        collection.zipWithIndex.map { case (item, ind) =>
          Array[PostgresTuple](
            IntConverter.toTuple(ind),
            toTuple(item))
        })
    } else {
      emptyPGO(arrayType)
    }
  }

  def toParameterPair[T](
    sw: PostgresWriter,
    collection: scala.collection.Seq[(T, T)],
    arrayType: String,
    toTupleUpdate: T => PostgresTuple,
    toTupleTable: T => PostgresTuple
  ): PGobject = {
    if(collection.nonEmpty) {
      toParameter(
        sw,
        arrayType,
        collection.zipWithIndex.map { case ((oldValue, newValue), ind) =>
          Array[PostgresTuple](
            IntConverter.toTuple(ind),
            if (oldValue == null) PostgresTuple.NULL else toTupleUpdate(oldValue),
            if (newValue == null) PostgresTuple.NULL else toTupleTable(newValue))
        })
    } else {
      emptyPGO(arrayType)
    }
  }

  def toParameterNew[T](
    sw: PostgresWriter,
    collection: scala.collection.Seq[NewTuple[T]],
    arrayType: String,
    toTuple: T => PostgresTuple
  ): PGobject = {
    if(collection.nonEmpty) {
      toParameter(
        sw,
        arrayType,
        collection.map { tuple =>
          Array[PostgresTuple](
            IntConverter.toTuple(tuple.index),
            IntConverter.toTuple(tuple.element),
            toTuple(tuple.value))
        })
    } else {
      emptyPGO(arrayType)
    }
  }

  def toParameterDiff[T <: Equality[T] with Identifiable](
    sw: PostgresWriter,
    collection: scala.collection.Seq[CollectionDiff[T]],
    arrayType: String,
    toTuple: T => PostgresTuple
  ): PGobject = {
    if(collection.nonEmpty) {
      toParameter(
        sw,
        arrayType,
        collection.map { it =>
          Array[PostgresTuple](
            IntConverter.toTuple(it.index),
            IntConverter.toTuple(it.element),
            if (it.oldValue.isEmpty) PostgresTuple.NULL else toTuple(it.oldValue.get),
            if (it.changedValue.isEmpty) PostgresTuple.NULL else toTuple(it.changedValue.get),
            if (it.newValue.isEmpty) PostgresTuple.NULL else toTuple(it.newValue.get),
            BoolConverter.toTuple(it.isNew))
        })
    } else {
      emptyPGO(arrayType)
    }
  }

}
