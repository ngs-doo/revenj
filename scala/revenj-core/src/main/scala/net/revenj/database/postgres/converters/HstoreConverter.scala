package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object HstoreConverter extends Converter[Map[String, String]] {
  private def toDatabase(value: Map[String, String]): String = {
    if (value.isEmpty) ""
    else {
      val sb = new StringBuilder
      value foreach { case (k, v) =>
        sb.append('"')
        sb.append(k.replace("\\", "\\\\").replace("\"", "\\\""))
        sb.append("\"=>")
        if (v == null) sb.append("NULL, ")
        else {
          sb.append('"')
          sb.append(v.replace("\\", "\\\\").replace("\"", "\\\""))
          sb.append("\", ")
        }
      }
      sb.setLength(sb.length - 2)
      sb.toString
    }
  }

  override def serializeURI(sw: PostgresBuffer, value: Map[String, String]): Unit = {
    val str = toDatabase(value)
    sw.addToBuffer(str)
  }

  def serializeCompositeURI(sw: PostgresBuffer, value: Map[String, String]): Unit = {
    val str = toDatabase(value)
    StringConverter.serializeCompositeURI(sw, str)
  }

  val dbName = "hstore"

  def default() = Map.empty

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Map[String, String] = {
    parseMap(reader, context, if (context > 0) context << 1 else 1, ')')
  }

  private def parseMap(reader: PostgresReader, context: Int, quoteContext: Int, matchEnd: Char): Map[String, String] = {
    var cur = reader.read(quoteContext)
    if (cur == ',' || cur == matchEnd) Map.empty
    else {
      val dict = Map.newBuilder[String, String]
      var i = 0
      while (i < context) {
        cur = reader.read()
        i += 1
      }
      reader.initBuffer()
      var fastReturn = false
      do {
        var fastBreak = false
        do {
          if (cur == '\\' || cur == '"') {
            cur = reader.read(quoteContext)
            fastBreak = cur == '='
            if (!fastBreak) {
              i = 0
              while (i < quoteContext - 1) {
                cur = reader.read()
                i += 1
              }
            }
          }
          if (!fastBreak) {
            reader.addToBuffer(cur.toChar)
            reader.fillUntil('\\', '"')
            cur = reader.read()
          }
        } while (!fastBreak && cur != -1)
        val name = reader.bufferToString()
        cur = reader.read(2)
        if (cur == 'N') {
          dict += name -> null
          cur = reader.read(4)
          if (cur == '\\' || cur == '"') {
            reader.read(context)
            fastBreak = true
            fastReturn = true
          } else if (cur == ',' && reader.peek != ' ') {
            fastBreak = true
            fastReturn = true
          } else {
            do {
              cur = reader.read()
            } while (cur == ' ')
          }
        } else {
          cur = reader.read(quoteContext)
          fastBreak = false
          do {
            if (cur == '\\' || cur == '"') {
              cur = reader.read(quoteContext)
              if (cur == ',') {
                dict += name -> reader.bufferToString()
                do {
                  cur = reader.read()
                } while (cur == ' ')
                cur = reader.read(quoteContext)
                fastBreak = true
              }
              if (!fastBreak) {
                i = 0
                while (i < context) {
                  cur = reader.read()
                  i += 1
                }
                if (cur == ',' || cur == -1 || cur == matchEnd) {
                  dict += name -> reader.bufferToString()
                  fastBreak = true
                  fastReturn = true
                }
                if (!fastReturn) {
                  i = 0
                  while (i < context - 1) {
                    cur = reader.read()
                    i += 1
                  }
                }
              }
            }
            if (!fastBreak) {
              reader.addToBuffer(cur.toChar)
              reader.fillUntil('\\', '"')
              cur = reader.read()
            }
          } while (!fastBreak && cur != -1)
        }
      } while (!fastReturn && cur != -1)
      dict.result()
    }
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Map[String, String] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      Map.empty
    } else {
      parseMap(reader, context, if (context == 0) 1 else context << 1, '}')
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Map[String, String]] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      Some(parseMap(reader, context, if (context == 0) 1 else context << 1, '}'))
    }
  }

  def toTuple(value: Map[String, String]): PostgresTuple = {
    new MapTuple(value)
  }

  private class MapTuple(val value: Map[String, String]) extends PostgresTuple {
    val mustEscapeRecord = true

    val mustEscapeArray = true

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      lazy val esc = PostgresTuple.buildQuoteEscape(escaping)
      lazy val quoteEscape = PostgresTuple.buildQuoteEscape(escaping + "0")
      lazy val slashEscape = PostgresTuple.buildSlashEscape(escaping.length + 1)
      var len = value.size
      mappings match {
        case Some(mapping) =>
          def loopOver(s: String): Unit = {
            var x = 0
            while (x < esc.length) {
              mapping(sw, esc.charAt(x))
              x += 1
            }
            var i = 0
            while (i < s.length) {
              val c = s.charAt(i)
              if (c == '"') {
                x = 0
                while (x < quoteEscape.length) {
                  mapping(sw, quoteEscape.charAt(x))
                  x += 1
                }
              } else if (c == '\\') {
                x = 0
                while (x < slashEscape.length) {
                  mapping(sw, slashEscape.charAt(x))
                  x += 1
                }
              } else mapping(sw, c)
              i += 1
            }
            x = 0
            while (x < esc.length) {
              mapping(sw, esc.charAt(x))
              x += 1
            }
          }
          value foreach { case (k, v) =>
            len -= 1
            loopOver(k)
            sw.write("=>")
            if (v == null) sw.write("NULL")
            else loopOver(v)
            if (len > 0) sw.write(", ")
          }
        case _ =>
          def loopOver(s: String): Unit = {
            sw.write(esc)
            var x = 0
            while (x < s.length) {
              val c = s.charAt(x)
              if (c == '"') sw.write(quoteEscape)
              else if (c == '\\') sw.write(slashEscape)
              else sw.write(c)
              x += 1
            }
            sw.write(esc)
          }
          value foreach { case (k, v) =>
            len -= 1
            loopOver(k)
            sw.write("=>")
            if (v == null) sw.write("NULL")
            else loopOver(v)
            if (len > 0) sw.write(", ")
          }
      }
    }
  }
}
