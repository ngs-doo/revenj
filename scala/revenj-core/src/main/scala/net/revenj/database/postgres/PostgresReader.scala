package net.revenj.database.postgres

import java.io.IOException

import net.revenj.patterns.ServiceLocator

class PostgresReader(private var serviceLocator: Option[ServiceLocator]) extends PostgresBuffer with AutoCloseable {
  private var input: String = ""
  private var length: Int = 0
  private var positionInInput: Int = 0
  private var lastChar: Int = 0
  private var buffer = new Array[Char](48)
  private var positionInBuffer: Int = 0
  val tmp = new Array[Char](64)

  def this() {
    this(None)
  }

  def locator: Option[ServiceLocator] = serviceLocator

  private[postgres] def reset(locator: ServiceLocator): Unit = {
    positionInBuffer = 0
    positionInInput = 0
    this.serviceLocator = Option(locator)
  }

  def close(): Unit = {
    length = 0
    positionInBuffer = 0
    positionInInput = 0
    lastChar = -1
  }

  def process(input: String): Unit = {
    this.input = input
    this.length = input.length
    positionInInput = 0
    positionInBuffer = 0
    lastChar = 0
  }

  def read(): Int = {
    if (positionInInput >= length) {
      lastChar = -1
    } else {
      lastChar = input.charAt(positionInInput).toInt
      positionInInput += 1
    }
    last
  }

  def read(total: Int): Int = {
    if (total == 0) {
      0
    } else {
      if (total > 1) {
        positionInInput += total - 1
      }
      read()
    }
  }

  def peek: Int = {
    if (positionInInput >= length) {
      -1
    } else {
      input.charAt(positionInInput).toInt
    }
  }

  def last: Int = lastChar

  def tempBuffer: Array[Char] = tmp

  def initBuffer(): Unit = {
    positionInBuffer = 0
  }

  def initBuffer(c: Char): Unit = {
    positionInBuffer = 1
    buffer(0) = c
  }

  def addToBuffer(c: Char): Unit = {
    if (positionInBuffer == buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2)
    }
    buffer(positionInBuffer) = c
    positionInBuffer += 1
  }

  def addToBuffer(buf: Array[Char]): Unit = {
    if (positionInBuffer + buf.length >= buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2 + buf.length)
    }
    var i = 0
    while (i < buf.length) {
      buffer(positionInBuffer + i) = buf(i)
      i += 1
    }
    positionInBuffer += buf.length
  }

  def addToBuffer(buf: Array[Char], len: Int): Unit = {
    if (positionInBuffer + len >= buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2 + len)
    }
    var i = 0
    while (i < len) {
      buffer(positionInBuffer + i) = buf(i)
      i += 1
    }
    positionInBuffer += len
  }

  def addToBuffer(buf: Array[Char], offset: Int, end: Int): Unit = {
    if (positionInBuffer + end >= buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2 + end)
    }
    var i = offset
    while (i < end) {
      buffer(positionInBuffer + i - offset) = buf(i)
      i += 1
    }
    positionInBuffer += end - offset
  }

  def addToBuffer(input: String): Unit = {
    val len = input.length
    if (positionInBuffer + len >= buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2 + len)
    }
    input.getChars(0, len, buffer, positionInBuffer)
    positionInBuffer += len
  }

  def fillUntil(c1: Char, c2: Char): Unit = {
    var i = 0
    i = positionInInput
    var isEnd = false
    while (!isEnd && i < input.length) {
      val c = input.charAt(i)
      isEnd = c == c1 || c == c2
      if (!isEnd) {
        addToBuffer(c)
        i += 1
      }
    }
    positionInInput = i
    if (positionInInput == input.length) {
      throw new IOException("End of input detected")
    }
  }

  def fillUntil(target: Array[Char], offset: Int, c1: Char, c2: Char): Int = {
    var i = positionInInput
    var cur = offset
    var isEnd = false
    while (!isEnd && i < input.length) {
      val c = input.charAt(i)
      isEnd = c == c1 || c == c2
      if (!isEnd) {
        target(cur) = c
        cur += 1
        i += 1
      }
    }
    positionInInput = i
    if (positionInInput == input.length) {
      throw new IOException("End of input detected")
    }
    cur - offset
  }

  def fillTotal(target: Array[Char], offset: Int, count: Int): Unit = {
    var i = 0
    while (i < count) {
      target(i + offset) = input.charAt(positionInInput + i)
      i += 1
    }
    positionInInput += count
  }

  def bufferToString(): String = {
    val len = positionInBuffer
    positionInBuffer = 0
    if (len == 0) {
      ""
    } else {
      new String(buffer, 0, len)
    }
  }

  def bufferToValue[T](converter: (Array[Char], Int, Int) => T): T = converter(buffer, 0, positionInBuffer)

  def bufferMatches(compare: String): Boolean = {
    if (compare.length != positionInBuffer) {
      false
    } else {
      var i = 0
      var isSame = true
      while (isSame && i < compare.length) {
        isSame = buffer(i) == compare.charAt(i)
        i += 1
      }
      isSame
    }
  }

  def bufferHash: Int = {
    val len = positionInBuffer
    var hash = 0x811C9DC5L
    var i = 0
    while (i < len && i < buffer.length) {
      hash = (hash ^ buffer(i)) * 0x1000193
      i += 1
    }
    hash.toInt
  }

}
object PostgresReader {
  def create(implicit locator: ServiceLocator): PostgresReader = {
    new PostgresReader(Option(locator))
  }

  private def findEscapedChar(input: String) = {
    var i = 0
    var found = -1
    while (found == -1 && i < input.length) {
      val c = input.charAt(i)
      if (c == '\\' || c == '/') {
        found = i
      }
      i += 1
    }
    found
  }

  def parseCompositeURI(uri: String, result: Array[String]): Unit = {
    var index = 0
    var i = findEscapedChar(uri)
    if (i == -1) {
      result(0) = uri
    } else {
      val sb = new java.lang.StringBuilder
      sb.append(uri, 0, i)
      while (i < uri.length) {
        val c = uri.charAt(i)
        if (c == '\\') {
          i += 1
          sb.append(uri.charAt(i))
        } else if (c == '/') {
          result(index) = sb.toString
          index += 1
          if (index == result.length) throw new IOException("Invalid URI provided: " + uri + ". Number of expected parts: " + result.length)
          sb.setLength(0)
        } else {
          sb.append(c)
        }
        i += 1
      }
      sb.append(uri, i, uri.length)
      result(index) = sb.toString
    }
  }

}