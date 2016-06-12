package net.revenj.database.postgres

class PostgresWriter extends PostgresBuffer with AutoCloseable {
  private var buffer = new Array[Char](64)
  val tmp = new Array[Char](64)
  private var position = 0

  def create(): PostgresWriter = {
    new PostgresWriter
  }

  def close() {
    position = 0
  }

  def reset() {
    position = 0
  }

  def write(input: String) {
    val len = input.length
    if (position + len >= buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2 + len)
    }
    input.getChars(0, len, buffer, position)
    position += len
  }

  def write(c: Byte) {
    if (position == buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2)
    }
    buffer(position) = c.toChar
    position += 1
  }

  def write(c: Char) {
    if (position == buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2)
    }
    buffer(position) = c
    position += 1
  }

  def write(buf: Array[Char]) {
    if (position + buf.length >= buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2 + buf.length)
    }
    var i = 0
    while (i < buf.length) {
      buffer(position + i) = buf(i)
      i += 1
    }
    position += buf.length
  }

  def write(buf: Array[Char], len: Int) {
    if (position + len >= buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2 + len)
    }
    var i = 0
    while (i < len) {
      buffer(position + i) = buf(i)
      i += 1
    }
    position += len
  }

  def write(buf: Array[Char], off: Int, end: Int) {
    if (position + end >= buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2 + end)
    }
    var i = off
    while (i < end) {
      buffer(position + i - off) = buf(i)
      i += 1
    }
    position += end - off
  }

  def writeBuffer(len: Int) {
    if (position + len >= buffer.length) {
      buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2 + len)
    }
    var i = 0
    while (i < len) {
      buffer(position + i) = tmp(i)
      i += 1
    }
    position += len
  }

  override def toString: String = new String(buffer, 0, position)

  def writeSimpleUriList(sb: StringBuilder, uris: Array[String]) {
    sb.append('\'')
    var uri = uris(0)
    var ind = uri.indexOf('\'')
    if (ind == -1) {
      sb.append(uri)
    } else {
      var i = 0
      while (i < uri.length) {
        val c = uri.charAt(i)
        if (c == '\'') {
          sb.append("''")
        } else {
          sb.append(c)
        }
        i += 1
      }
    }
    var x = 1
    while (x < uris.length) {
      uri = uris(x)
      sb.append("','")
      ind = uri.indexOf('\'')
      if (ind == -1) {
        sb.append(uri)
      } else {
        var i = 0
        while (i < uri.length) {
          val c = uri.charAt(i)
          if (c == '\'') {
            sb.append("''")
          } else {
            sb.append(c)
          }
          i += 1
        }
      }
      x += 1
    }
    sb.append('\'')
  }

  def writeSimpleUri(sb: StringBuilder, uri: String) {
    sb.append('\'')
    val ind: Int = uri.indexOf('\'')
    if (ind == -1) {
      sb.append(uri)
    } else {
      var i = 0
      while (i < uri.length) {
        val c = uri.charAt(i)
        if (c == '\'') {
          sb.append("''")
        } else {
          sb.append(c)
        }
        i += 1
      }
    }
    sb.append('\'')
  }

  private def findEscapedChar(input: String) = {
    var i = 0
    var escapeAt = -1
    while (escapeAt == -1 && i < input.length) {
      val c = input.charAt(i)
      if (c == '\\' || c == '/' || c == '\'') {
        escapeAt = i
      }
      i += 1
    }
    escapeAt
  }

  def writeCompositeUriList(sb: StringBuilder, uris: Array[String]) {
    sb.append("('")
    var uri = uris(0)
    var i = 0
    var ind = findEscapedChar(uri)
    if (ind == -1) {
      sb.append(uri)
    } else {
      while (i < uri.length) {
        val c = uri.charAt(i)
        if (c == '\\') {
          i += 1
          sb.append(uri.charAt(i))
        } else if (c == '/') {
          sb.append("','")
        } else if (c == '\'') {
          sb.append("''")
        } else {
          sb.append(c)
        }
        i += 1
      }
    }
    var x = 1
    while (x < uris.length) {
      sb.append("'),('")
      uri = uris(x)
      ind = findEscapedChar(uri)
      if (ind == -1) {
        sb.append(uri)
      } else {
        i = 0
        while (i < uri.length) {
          val c = uri.charAt(i)
          if (c == '\\') {
            i += 1
            sb.append(uri.charAt(i))
          } else if (c == '/') {
            sb.append("','")
          } else if (c == '\'') {
            sb.append("''")
          } else {
            sb.append(c)
          }
          i += 1
        }
      }
      x += 1
    }
    sb.append("')")
  }

  def writeCompositeUri(sb: StringBuilder, uri: String) {
    sb.append("('")
    var i = 0
    val ind: Int = findEscapedChar(uri)
    if (ind == -1) {
      sb.append(uri)
    } else {
      while (i < uri.length) {
        val c: Char = uri.charAt(i)
        if (c == '\\') {
          i += 1
          sb.append(uri.charAt(i))
        } else if (c == '/') {
          sb.append("','")
        } else if (c == '\'') {
          sb.append("''")
        } else {
          sb.append(c)
        }
        i += 1
      }
    }
    sb.append("')")
  }

  def tempBuffer: Array[Char] = tmp

  def initBuffer() {
    reset()
  }

  def initBuffer(c: Char) {
    reset()
    write(c)
  }

  def addToBuffer(c: Char) {
    write(c)
  }

  def addToBuffer(buf: Array[Char]) {
    write(buf)
  }

  def addToBuffer(buf: Array[Char], len: Int) {
    write(buf, len)
  }

  def addToBuffer(buf: Array[Char], off: Int, end: Int) {
    write(buf, off, end)
  }

  def addToBuffer(input: String) {
    write(input)
  }

  def bufferToString(): String = {
    val result = toString
    position = 0
    result
  }
}
