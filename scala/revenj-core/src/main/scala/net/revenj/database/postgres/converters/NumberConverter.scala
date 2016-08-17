package net.revenj.database.postgres.converters

import net.revenj.database.postgres.PostgresWriter

object NumberConverter {
  private val NUMBERS = {
    val arr = new Array[Int](100)
    for (i <- arr.indices) {
      val first = ((i / 10) + '0').asInstanceOf[Char]
      val second = ((i % 10) + '0').asInstanceOf[Char]
      val offset = if (i < 10) 1 else 0
      arr(i) = (offset << 24) + (first << 8) + second
    }
    arr
  }

  private[converters] def write2(number: Int, buffer: Array[Char], start: Int): Unit = {
    val pair = NUMBERS(number)
    buffer(start) = (pair >> 8).toChar
    buffer(start + 1) = pair.toByte.toChar
  }

  private[converters] def write2(number: Int, sw: PostgresWriter): Unit = {
    val pair = NUMBERS(number)
    sw.write((pair >> 8).toChar)
    sw.write(pair.toByte)
  }

  private[converters] def write3(number: Int, buffer: Array[Char], start: Int): Unit = {
    val div = number / 100
    buffer(start) = (div + '0').toChar
    val rem = number - div * 100
    val pair2 = NUMBERS(rem)
    buffer(start + 1) = (pair2 >> 8).toChar
    buffer(start + 2) = pair2.toByte.toChar
  }

  private[converters] def write4(number: Int, buffer: Array[Char], start: Int): Unit = {
    val div = number / 100
    val pair1 = NUMBERS(div)
    buffer(start) = (pair1 >> 8).toChar
    buffer(start + 1) = pair1.toByte.toChar
    val rem = number - div * 100
    val pair2 = NUMBERS(rem)
    buffer(start + 2) = (pair2 >> 8).toChar
    buffer(start + 3) = pair2.toByte.toChar
  }

  private[converters] def write4(number: Int, sw: PostgresWriter): Unit = {
    val div = number / 100
    val pair1 = NUMBERS(div)
    sw.write((pair1 >> 8).toChar)
    sw.write(pair1.toByte.toChar)
    val rem = number - div * 100
    val pair2 = NUMBERS(rem)
    sw.write((pair2 >> 8).toChar)
    sw.write(pair2.toByte)
  }

  private[converters] def read2(source: Array[Char], start: Int): Int = {
    val first = source(start) - '0'
    (first << 3) + (first << 1) + source(start + 1) - '0'
  }

  private[converters] def read4(source: Array[Char], start: Int): Int = {
    val first = source(start) - '0'
    val second = source(start + 1) - '0'
    val third = source(start + 2) - '0'
    first * 1000 + second * 100 + (third << 3) + (third << 1) + source(start + 3) - '0'
  }

  def tryParsePositiveInt(number: String): Option[Int] = {
    if (number.length == 0 || number.charAt(0) < '0' || number.charAt(0) > '9') {
      None
    } else {
      var value = 0
      var i = 0
      while (i < number.length) {
        value = (value << 3) + (value << 1) + number.charAt(i) - '0'
        i += 1
      }
      Some(value)
    }
  }

  def parseLong(number: String): Long = {
    var value = 0L
    if (number.charAt(0) == '-') {
      var i = 1
      while (i < number.length) {
        value = (value << 3) + (value << 1) - number.charAt(i) + '0'
        i += 1
      }
    } else {
      var i = 0
      while (i < number.length) {
        value = (value << 3) + (value << 1) + number.charAt(i) - '0'
        i += 1
      }
    }
    value
  }

  def serialize(value: Int, buf: Array[Char]): Int = {
    if (value == Int.MinValue) {
      "-2147483648".getChars(0, 11, buf, 0)
      0
    } else if (value == 0) {
      buf(10) = '0'
      10
    } else {
      var q = 0
      var r = 0
      var charPos = 10
      var offset = 0
      var i = 0
      if (value < 0) {
        i = -value
        offset = 0
      } else {
        i = value
        offset = 1
      }
      var v = 0
      while (charPos > 0 && i != 0) {
        q = i / 100
        r = i - ((q << 6) + (q << 5) + (q << 2))
        i = q
        v = NUMBERS(r)
        buf(charPos) = v.toByte.toChar
        charPos -= 1
        buf(charPos) = (v >> 8).toChar
        charPos -= 1
      }
      val zeroBased = v >> 24
      buf(charPos + zeroBased) = '-'
      charPos + offset + zeroBased
    }
  }

  def serialize(value: Long, buf: Array[Char]): Int = {
    if (value == Long.MinValue) {
      "-9223372036854775808".getChars(0, 20, buf, 1)
      1
    } else if (value == 0) {
      buf(20) = '0'
      20
    } else {
      var q = 0L
      var r = 0
      var charPos = 20
      var offset = 0
      var i = 0L
      if (value < 0) {
        i = -value
        offset = 0
      } else {
        i = value
        offset = 1
      }
      var v = 0
      while (charPos > 0 && i != 0) {
        q = i / 100
        r = (i - ((q << 6) + (q << 5) + (q << 2))).toInt
        i = q
        v = NUMBERS(r)
        buf(charPos) = v.toByte.toChar
        charPos -= 1
        buf(charPos) = (v >> 8).toChar
        charPos -= 1
      }
      val zeroBased = v >> 24
      buf(charPos + zeroBased) = '-'
      charPos + offset + zeroBased
    }
  }

  def parsePositive(source: Array[Char], start: Int, end: Int): Int = {
    var res = 0
    var i = start
    while (i < source.length && i != end) {
      res = res * 10 + (source(i) - '0')
      i += 1
    }
    res
  }
}
