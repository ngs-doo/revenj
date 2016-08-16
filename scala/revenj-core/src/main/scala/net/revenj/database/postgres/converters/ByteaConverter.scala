package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object ByteaConverter extends Converter[Array[Byte]] {

  private val charMap = "0123456789abcdef".toCharArray
  private val charLookup = {
    val result = new Array[Int]('f' + 1)
    var i = 0
    while (i < charMap.length) {
      result(charMap(i).toInt) = i
      i += 1
    }
    result
  }

  private val EMPTY_BYTES = new Array[Byte](0)
  private val XX = "\\x".toCharArray

  override def serializeURI(sw: PostgresBuffer, value: Array[Byte]): Unit = {
    sw.addToBuffer(XX)
    var i = 0
    while (i < value.length) {
      val b = value(i)
      sw.addToBuffer(charMap((b >> 4) & 0xf))
      sw.addToBuffer(charMap(b & 0xf))
      i += 1
    }
  }

  val dbName = "bytea"

  def default() = EMPTY_BYTES

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Array[Byte] = {
    val len = if (context == 0) 1 else context + (context << 1)
    parseBytea(reader, len, context)
  }

  private def parseBytea(reader: PostgresReader, len: Int, context: Int): Array[Byte] = {
    var cur = reader.read(len + 1)
    val builder = mutable.ArrayBuilder.make[Byte]()
    builder.sizeHint(1024)
    while (cur != -1 && cur != '\\' && cur != '"') {
      builder += ((charLookup(cur) << 4) + charLookup(reader.read())).toByte
      cur = reader.read()
    }
    reader.read(context)
    builder.result()
  }

  override def parseCollectionOption(reader: PostgresReader, context: Int): Option[ArrayBuffer[Array[Byte]]] = {
    var cur = reader.read()
    if (cur == ',' || cur == ')') {
      None
    } else {
      val escaped = cur != '{'
      if (escaped) {
        reader.read(context)
      }
      val innerContext = context << 1
      val skipInner = innerContext + (innerContext << 1)
      cur = reader.peek
      if (cur == '}') {
        reader.read()
      }
      val list = ArrayBuffer.newBuilder[Array[Byte]]
      while (cur != -1 && cur != '}') {
        cur = reader.read()
        if (cur == 'N') {
          list += EMPTY_BYTES
          reader.read(4)
        } else {
          list += parseBytea(reader, skipInner, innerContext)
        }
        cur = reader.last
      }
      if (escaped) {
        reader.read(context + 1)
      } else {
        reader.read()
      }
      Some(list.result())
    }
  }

  override def parseNullableCollectionOption(reader: PostgresReader, context: Int): Option[ArrayBuffer[Option[Array[Byte]]]] = {
    var cur = reader.read()
    if (cur == ',' || cur == ')') {
      None
    } else {
      val escaped = cur != '{'
      if (escaped) {
        reader.read(context)
      }
      val innerContext = context << 1
      val skipInner = innerContext + (innerContext << 1)
      cur = reader.peek
      if (cur == '}') {
        reader.read()
      }
      val list = ArrayBuffer.newBuilder[Option[Array[Byte]]]
      while (cur != -1 && cur != '}') {
        cur = reader.read()
        if (cur == 'N') {
          list += None
          reader.read(4)
        } else {
          list += Some(parseBytea(reader, skipInner, innerContext))
        }
        cur = reader.last
      }
      if (escaped) {
        reader.read(context + 1)
      } else {
        reader.read()
      }
      Some(list.result())
    }
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Array[Byte] = {
    val innerContext = context + (context << 1)
    parseBytea(reader, innerContext, context)
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Array[Byte]] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      val innerContext = context + (context << 1)
      Some(parseBytea(reader, innerContext, context))
    }
  }

  def toTuple(value: Array[Byte]): PostgresTuple = {
    new ByteaTuple(value)
  }

  private class ByteaTuple(val value: Array[Byte]) extends PostgresTuple {

    val mustEscapeRecord = true

    val mustEscapeArray = true

    private def buildArray(sw: PostgresWriter): Unit = {
      var i = 0
      while (i < value.length) {
        val b = value(i)
        sw.addToBuffer(charMap((b >> 4) & 0xf))
        sw.addToBuffer(charMap(b & 0xf))
        i += 1
      }
    }

    override def buildTuple(sw: PostgresWriter, quote: Boolean): Unit = {
      if (quote) {
        sw.write('\'')
        insertRecord(sw, "", None)
        sw.write('\'')
      }
      else insertRecord(sw, "", None)
    }

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      lazy val pref = PostgresTuple.buildSlashEscape(escaping.length)
      mappings match {
        case Some(m) =>
          var x = 0
          while (x < pref.length) {
            m(sw, pref.charAt(x))
            x += 1
          }
        case _ =>
          sw.write(pref)
      }
      sw.write('x')
      buildArray(sw)
    }

    override def insertArray(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      insertRecord(sw, escaping, mappings)
    }
  }

}
