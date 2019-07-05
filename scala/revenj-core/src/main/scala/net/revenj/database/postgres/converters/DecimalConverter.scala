package net.revenj.database.postgres.converters

import net.revenj.Utils
import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object DecimalConverter extends Converter[BigDecimal] {
  override def serializeURI(sw: PostgresBuffer, value: BigDecimal): Unit = {
    sw.addToBuffer(value.bigDecimal.toPlainString)
  }

  override val dbName = "numeric"

  override def default(): BigDecimal = Utils.Zero0

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): BigDecimal = parseDecimal(reader, start, ')')

  private val oneDigits = (0 until 10).map(i => BigDecimal(i)).toArray
  private val zeros = (0 until 10).map(i => BigDecimal(java.math.BigDecimal.valueOf(0, 10 - i))).toArray

  private def parseAsLong(buf: Array[Char], startDot: Int, isNeg: Boolean, start: Int, len: Int) = {
    var dotIndex = startDot
    var simpleNumber = true
    var longValue = 0L
    var i = start
    if (isNeg) {
      while (simpleNumber && i < len) {
        val ch = buf(i)
        i += 1
        if (ch == '.') {
          if (dotIndex >= 0) {
            simpleNumber = false
          }
          dotIndex = i - 1
        } else if (ch >= '0' && ch <= '9') {
          longValue = (longValue << 3) + (longValue << 1) - ch + 48
        } else {
          simpleNumber = false
        }
      }
    } else {
      while (simpleNumber && i < len) {
        val ch = buf(i)
        i += 1
        if (ch == '.') {
          if (dotIndex >= 0) {
            simpleNumber = false
          }
          dotIndex = i - 1
        } else if (ch >= '0' && ch <= '9') {
          longValue = (longValue << 3) + (longValue << 1) + ch - 48
        } else {
          simpleNumber = false
        }
      }
    }
    if (!simpleNumber) {
      BigDecimal(new java.math.BigDecimal(buf, 0, len))
    } else if (longValue == 0 && dotIndex != -1 && len - dotIndex < 10) {
      zeros(11 - len + dotIndex)
    } else {
      if (dotIndex == -1) {
        BigDecimal(new java.math.BigDecimal(longValue))
      } else {
        BigDecimal(java.math.BigDecimal.valueOf(longValue, len - dotIndex - 1))
      }
    }
  }

  private def parseDecimal(reader: PostgresReader, cur: Int, matchEnd: Char): BigDecimal = {
    reader.initBuffer(cur.toChar)
    reader.fillUntil(',', matchEnd)
    reader.read()
    val len = reader.bufferPosition
    if (len == 1) {
      oneDigits(cur - 48)
    } else if (len < 19) {
      parseAsLong(reader.currentBuffer, -1, cur == '-', 0, len)
    } else {
      val buf = reader.currentBuffer
      val isNeg = cur == '-'
      var only0 = true
      val offset = if (isNeg) 1 else 0
      if (buf(offset) == '0' && buf(offset + 1) == '.') {
        var x = 2 + offset
        val maxLong = len - 18
        while (only0 && x < maxLong) {
          only0 = buf(x) == '0'
          x += 1
        }
        if (only0) {
          parseAsLong(buf, 1 + offset, isNeg, maxLong, len)
        } else {
          BigDecimal(new java.math.BigDecimal(reader.currentBuffer, 0, len))
        }
      } else {
        BigDecimal(new java.math.BigDecimal(reader.currentBuffer, 0, len))
      }
    }
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): BigDecimal = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      Utils.Zero0
    } else {
      parseDecimal(reader, cur, '}')
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[BigDecimal] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      Some(parseDecimal(reader, cur, '}'))
    }
  }

  override def toTuple(value: BigDecimal): PostgresTuple = {
    new BigDecimalTuple(value)
  }

  private class BigDecimalTuple(val value: BigDecimal) extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      val jbd = value.bigDecimal
      val scale = jbd.scale
      val precision = jbd.precision
      if (precision == 1 && scale >= 0 && jbd.compareTo(java.math.BigDecimal.ZERO) == 0) {
        sw.write('0')
        if (scale > 0) {
          sw.write('.')
          var x = 0
          while (x < scale) {
            sw.write('0')
            x += 1
          }
        }
      } else if (precision < 19 && scale >= 0) {
        if (scale == 0) {
          val offset = NumberConverter.serialize(jbd.longValue(), sw.tmp)
          sw.write(sw.tmp, offset, 21)
        } else {
          val longValue = jbd.unscaledValue().longValue()
          val offset = NumberConverter.serialize(longValue, sw.tmp)
          if (scale < precision) {
            sw.write(sw.tmp, offset, 21 - scale)
            sw.write('.')
            sw.write(sw.tmp, 21 - scale, 21)
          } else {
            if (longValue < 0) {
              sw.write('-')
            }
            sw.write('0')
            sw.write('.')
            var x = precision
            while (x < scale) {
              sw.write('0')
              x += 1
            }
            sw.write(sw.tmp, 21 - precision, 21)
          }
        }
      } else {
        sw.write(jbd.toPlainString)
      }
    }

    override def buildTuple(quote: Boolean): String = {
      value.bigDecimal.toPlainString
    }
  }

}
