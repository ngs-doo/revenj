package net.revenj.database.postgres.converters

import java.util.UUID

import net.revenj.Utils
import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}

object UuidConverter extends Converter[UUID] {

  private val Lookup = {
    val result = new Array[Char](256)
    for (i <- 0 until 256) {
      val hi = (i >> 4) & 15
      val lo = i & 15
      result(i) = (((if (hi < 10) '0' + hi else 'a' + hi - 10) << 8) + (if (lo < 10) '0' + lo else 'a' + lo - 10)).toChar
    }
    result
  }
  private val Values = {
    val result = new Array[Byte]('f' + 1 - '0')
    for (c <- '0' to '9') {
      result(c - '0') = (c - '0').toByte
    }
    for (c <- 'a' to 'f') {
      result(c - '0') = (c - 'a' + 10).toByte
    }
    for (c <- 'A' to 'F') {
      result(c - '0') = (c - 'A' + 10).toByte
    }
    result
  }

  override def serializeURI(sw: PostgresBuffer, value: UUID): Unit = {
    serialize(value, sw.tempBuffer, 0)
    sw.addToBuffer(sw.tempBuffer, 36)
  }

  private def serialize(value: UUID, buf: Array[Char], start: Int): Unit = {
    val hi = value.getMostSignificantBits
    val lo = value.getLeastSignificantBits
    val hi1 = (hi >> 32).toInt
    val hi2 = hi.toInt
    val lo1 = (lo >> 32).toInt
    val lo2 = lo.toInt
    var v = (hi1 >> 24) & 255
    var l = Lookup(v)
    buf(start) = (l >> 8).toByte.toChar
    buf(start + 1) = l.toByte.toChar
    v = (hi1 >> 16) & 255
    l = Lookup(v)
    buf(start + 2) = (l >> 8).toByte.toChar
    buf(start + 3) = l.toByte.toChar
    v = (hi1 >> 8) & 255
    l = Lookup(v)
    buf(start + 4) = (l >> 8).toByte.toChar
    buf(start + 5) = l.toByte.toChar
    v = hi1 & 255
    l = Lookup(v)
    buf(start + 6) = (l >> 8).toByte.toChar
    buf(start + 7) = l.toByte.toChar
    buf(start + 8) = '-'
    v = (hi2 >> 24) & 255
    l = Lookup(v)
    buf(start + 9) = (l >> 8).toByte.toChar
    buf(start + 10) = l.toByte.toChar
    v = (hi2 >> 16) & 255
    l = Lookup(v)
    buf(start + 11) = (l >> 8).toByte.toChar
    buf(start + 12) = l.toByte.toChar
    buf(start + 13) = '-'
    v = (hi2 >> 8) & 255
    l = Lookup(v)
    buf(start + 14) = (l >> 8).toByte.toChar
    buf(start + 15) = l.toByte.toChar
    v = hi2 & 255
    l = Lookup(v)
    buf(start + 16) = (l >> 8).toByte.toChar
    buf(start + 17) = l.toByte.toChar
    buf(start + 18) = '-'
    v = (lo1 >> 24) & 255
    l = Lookup(v)
    buf(start + 19) = (l >> 8).toByte.toChar
    buf(start + 20) = l.toByte.toChar
    v = (lo1 >> 16) & 255
    l = Lookup(v)
    buf(start + 21) = (l >> 8).toByte.toChar
    buf(start + 22) = l.toByte.toChar
    buf(start + 23) = '-'
    v = (lo1 >> 8) & 255
    l = Lookup(v)
    buf(start + 24) = (l >> 8).toByte.toChar
    buf(start + 25) = l.toByte.toChar
    v = lo1 & 255
    l = Lookup(v)
    buf(start + 26) = (l >> 8).toByte.toChar
    buf(start + 27) = l.toByte.toChar
    v = (lo2 >> 24) & 255
    l = Lookup(v)
    buf(start + 28) = (l >> 8).toByte.toChar
    buf(start + 29) = l.toByte.toChar
    v = (lo2 >> 16) & 255
    l = Lookup(v)
    buf(start + 30) = (l >> 8).toByte.toChar
    buf(start + 31) = l.toByte.toChar
    v = (lo2 >> 8) & 255
    l = Lookup(v)
    buf(start + 32) = (l >> 8).toByte.toChar
    buf(start + 33) = l.toByte.toChar
    v = lo2 & 255
    l = Lookup(v)
    buf(start + 34) = (l >> 8).toByte.toChar
    buf(start + 35) = l.toByte.toChar
  }

  def serializeURI(buf: Array[Char], pos: Int, value: UUID): Int = {
    serialize(value, buf, pos)
    pos + 36
  }

  val dbName = "uuid"

  def default() = Utils.MinUuid

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): UUID = parseUuid(reader, start, 36)

  private def parseUuid(reader: PostgresReader, start: Int, len: Int) = {
    val buf = reader.tmp
    buf(0) = start.toChar
    reader.fillTotal(buf, 1, len)
    toUuid(buf)
  }

  private def toUuid(buf: Array[Char]): UUID = {
    try {
      var hi = 0L
      var lo = 0L
      var i = 0
      while (i < 8) {
        hi = (hi << 4) + Values(buf(i) - '0')
        i += 1
      }
      i = 9
      while (i < 13) {
        hi = (hi << 4) + Values(buf(i) - '0')
        i += 1
      }
      i = 14
      while (i < 18) {
        hi = (hi << 4) + Values(buf(i) - '0')
        i += 1
      }
      i = 19
      while (i < 23) {
        lo = (lo << 4) + Values(buf(i) - '0')
        i += 1
      }
      i = 24
      while (i < 36) {
        lo = (lo << 4) + Values(buf(i) - '0')
        i += 1
      }
      new UUID(hi, lo)
    }
    catch {
      case ex: ArrayIndexOutOfBoundsException => UUID.fromString(new String(buf, 0, 36))
    }
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): UUID = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      Utils.MinUuid
    } else {
      val uuid = parseUuid(reader, cur, 35)
      reader.read()
      uuid
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[UUID] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      val uuid = parseUuid(reader, cur, 35)
      reader.read()
      Some(uuid)
    }
  }

  def toTuple(value: UUID): PostgresTuple = {
    new UuidTuple(value)
  }

  private class UuidTuple(val value: UUID) extends PostgresTuple {
    val mustEscapeRecord = false

    val mustEscapeArray = false

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      serialize(value, sw.tmp, 0)
      sw.writeBuffer(36)
    }

    override def buildTuple(quote: Boolean): String = {
      if (quote) {
        val buf = new Array[Char](38)
        buf(0) = '\''
        serialize(value, buf, 1)
        buf(37) = '\''
        new String(buf, 0, buf.length)
      } else {
        val buf = new Array[Char](36)
        serialize(value, buf, 0)
        new String(buf, 0, buf.length)
      }
    }
  }

}
