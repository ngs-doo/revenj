package net.revenj.database.postgres.converters

import java.io.IOException

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader}

object StringConverter extends Converter[String] {
  override def serializeURI(sw: PostgresBuffer, value: String): Unit = {
    if (value != null) sw.addToBuffer(value)
  }

  def serializeCompositeURI(sw: PostgresBuffer, value: String): Unit = {
    if (value != null) {
      var i = 0
      while (i < value.length) {
        val c = value.charAt(i)
        if (c == '\\' || c == '/') {
          sw.addToBuffer('\\')
        }
        sw.addToBuffer(c)
        i += 1
      }
    }
  }

  val dbName = "varchar"
  def default() = ""

  def skip(reader: PostgresReader, context: Int): Unit = {
    var cur = reader.read()
    if (cur != ',' && cur != ')') {
      if (cur != '"' && cur != '\\') {
        reader.initBuffer()
        reader.fillUntil(',', ')')
        reader.read()
      } else {
        cur = reader.read(context)
        while (cur >= 0) {
          if (cur == '\\' || cur == '"') {
            cur = reader.read(context)
            if (cur == ',' || cur == ')') {
              cur = -2
            } else {
              cur = reader.read(context)
            }
          } else cur = reader.read()
        }
        if (cur == -1) {
          throw new IOException("Unable to find end of string")
        }
      }
    }
  }

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): String = {
    if (start != '"' && start != '\\') {
      reader.initBuffer(start.toChar)
      reader.fillUntil(',', ')')
      reader.read()
      reader.bufferToString()
    } else {
      parseEscapedString(reader, context, ')')
    }
  }

  private[converters] def parseEscapedString(reader: PostgresReader, context: Int, matchEnd: Char): String = {
    var cur = reader.read(context)
    reader.initBuffer()
    do {
      if (cur == '\\' || cur == '"') {
        cur = reader.read(context)
        if (cur == ',' || cur == matchEnd) {
          return reader.bufferToString()
        }
        var i: Int = 0
        while (i < context - 1) {
          cur = reader.read()
          i += 1
        }
      }
      reader.addToBuffer(cur.toChar)
      reader.fillUntil('\\', '"')
      cur = reader.read()
    } while (cur != -1)
    throw new IOException("Unable to find end of string")
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): String = {
    val cur = reader.read()
    if (cur == '"' || cur == '\\') {
      parseEscapedString(reader, context, '}')
    } else {
      reader.initBuffer(cur.toChar)
      reader.fillUntil(',', '}')
      reader.read()
      if (reader.bufferMatches("NULL")) {
        ""
      } else {
        reader.bufferToString()
      }
    }
  }
  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[String] = {
    val cur = reader.read()
    if (cur == '"' || cur == '\\') {
      Some(parseEscapedString(reader, context, '}'))
    } else {
      reader.initBuffer(cur.toChar)
      reader.fillUntil(',', '}')
      reader.read()
      if (reader.bufferMatches("NULL")) {
        None
      } else {
        Some(reader.bufferToString())
      }
    }
  }

  override def toTuple(value: String): PostgresTuple = ValueTuple.from(value)
}
