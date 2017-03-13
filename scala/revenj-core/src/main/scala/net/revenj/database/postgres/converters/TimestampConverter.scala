package net.revenj.database.postgres.converters

import java.sql.PreparedStatement
import java.time.{LocalDateTime, OffsetDateTime, ZoneOffset}

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}
import org.postgresql.util.PGobject

object UtcTimestampConverter extends TimestampConverter(true)

object LocalTimestampConverter extends TimestampConverter(false)

class TimestampConverter(asUtc: Boolean) extends Converter[OffsetDateTime] {

  import TimestampConverter._

  val dbName = "timestamptz"

  def default() = MIN_DATE_TIME_UTC

  override def serializeURI(sw: PostgresBuffer, value: OffsetDateTime): Unit = {
    val len = serialize(sw.tempBuffer, 0, value)
    sw.addToBuffer(sw.tempBuffer, len)
  }

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): OffsetDateTime = parseOffsetTimestamp(reader, context, asUtc)

  override def parseCollectionItem(reader: PostgresReader, context: Int): OffsetDateTime = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      MIN_DATE_TIME_UTC
    } else {
      parseOffsetTimestamp(reader, context, asUtc)
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[OffsetDateTime] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      Some(parseOffsetTimestamp(reader, context, asUtc))
    }
  }

  def toTuple(value: OffsetDateTime): PostgresTuple = {
    new OffsetTimestampTuple(value)
  }
}

object TimestampConverter {
  private val MIN_LOCAL_DATE_TIME = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0)
  private val MIN_DATE_TIME_UTC = OffsetDateTime.of(MIN_LOCAL_DATE_TIME, ZoneOffset.UTC)
  private val TIMESTAMP_REMINDER = Array[Int](100000, 10000, 1000, 100, 10, 1)

  def setParameter(sw: PostgresBuffer, ps: PreparedStatement, index: Int, value: OffsetDateTime): Unit = {
    val pg = new PGobject
    pg.setType("timestamptz")
    val buf = sw.tempBuffer
    val len = serialize(buf, 0, value)
    pg.setValue(new String(buf, 0, len))
    ps.setObject(index, pg)
  }

  private def serialize(buffer: Array[Char], pos: Int, value: LocalDateTime): Int = {
    val year = value.getYear
    if (year > 9999) throw new IllegalArgumentException(s"Invalid year detected: $value. Only dates up to 9999-12-31 are allowed")
    //TODO: Java supports wider range of dates
    buffer(pos + 4) = '-'
    buffer(pos + 7) = '-'
    buffer(pos + 10) = ' '
    buffer(pos + 13) = ':'
    buffer(pos + 16) = ':'
    NumberConverter.write4(year, buffer, pos)
    NumberConverter.write2(value.getMonthValue, buffer, pos + 5)
    NumberConverter.write2(value.getDayOfMonth, buffer, pos + 8)
    NumberConverter.write2(value.getHour, buffer, pos + 11)
    NumberConverter.write2(value.getMinute, buffer, pos + 14)
    NumberConverter.write2(value.getSecond, buffer, pos + 17)
    val micro = value.getNano / 1000
    var end = pos + 19
    if (micro != 0) {
      buffer(pos + 19) = '.'
      val div = micro / 100
      val rem = micro - div * 100
      NumberConverter.write4(div, buffer, 20)
      NumberConverter.write2(rem, buffer, 24)
      end = pos + 25
      while (buffer(end) == '0') {
        end -= 1
      }
      end += 1
    }
    buffer(end) = '+'
    buffer(end + 1) = '0'
    buffer(end + 2) = '0'
    end + 3
  }

  //TODO: check if 1:22 is universal or must be adjusted to local zone
  private val ZERO_OFFSET = ZoneOffset.ofHoursMinutes(1, 22)

  def serialize(buffer: Array[Char], start: Int, value: OffsetDateTime): Int = {
    val offset = value.getOffset.getTotalSeconds
    if (value.getYear < 1884) {
      val at122 = value.toLocalDateTime.plusSeconds((ZERO_OFFSET.getTotalSeconds - offset).toLong)
      val pos = serialize(buffer, start, at122)
      buffer(pos - 1) = '1'
      buffer(pos) = ':'
      buffer(pos + 1) = '2'
      buffer(pos + 2) = '2'
      pos + 3
    } else {
      val offsetHours = offset / 3600
      val offsetDiff = offset - offsetHours * 3600
      if (offsetDiff != 0) serializeNormalized(buffer, start, value.toLocalDateTime.minusSeconds(offsetDiff.toLong), offsetHours)
      else serializeNormalized(buffer, start, value.toLocalDateTime, offsetHours)
    }
  }

  private def serializeNormalized(buffer: Array[Char], pos: Int, value: LocalDateTime, offsetHours: Int): Int = {
    //TODO: Java supports wider range of dates
    val year = value.getYear
    if (year > 9999) throw new IllegalArgumentException(s"Invalid year detected: $value. Only dates up to 9999-12-31 are allowed")
    buffer(pos + 4) = '-'
    buffer(pos + 7) = '-'
    buffer(pos + 10) = ' '
    buffer(pos + 13) = ':'
    buffer(pos + 16) = ':'
    NumberConverter.write4(year, buffer, pos)
    NumberConverter.write2(value.getMonthValue, buffer, pos + 5)
    NumberConverter.write2(value.getDayOfMonth, buffer, pos + 8)
    NumberConverter.write2(value.getHour, buffer, pos + 11)
    NumberConverter.write2(value.getMinute, buffer, pos + 14)
    NumberConverter.write2(value.getSecond, buffer, pos + 17)
    val micro = value.getNano / 1000
    var end = pos + 19
    if (micro != 0) {
      buffer(pos + 19) = '.'
      val div = micro / 100
      val rem = micro - div * 100
      NumberConverter.write4(div, buffer, 20)
      NumberConverter.write2(rem, buffer, 24)
      end = pos + 25
      while (buffer(end) == '0') {
        end -= 1
      }
      end += 1
    }
    if (offsetHours >= 0) {
      buffer(end) = '+'
      NumberConverter.write2(offsetHours, buffer, end + 1)
    } else {
      buffer(end) = '-'
      NumberConverter.write2(-offsetHours, buffer, end + 1)
    }
    end + 3
  }

  private def parseOffsetTimestamp(reader: PostgresReader, context: Int, asUtc: Boolean): OffsetDateTime = {
    //TODO: BC after date for year < 0 ... not supported by .NET, but supported by Java
    val cur = reader.read(context)
    val buf = reader.tmp
    buf(0) = cur.toChar
    val len = reader.fillUntil(buf, 1, '\\', '"') + 1
    reader.read(context + 1)
    if (buf(10) != ' ') {
      var foundAt = 11
      while (foundAt < buf.length && buf(foundAt) != ' ') {
        foundAt += 1
      }
      if (foundAt == buf.length) {
        throw new RuntimeException("Invalid timestamp value: " + new String(buf, 0, foundAt))
      }
      buf(foundAt) = 'T'
      OffsetDateTime.parse("+" + new String(buf, 0, len))
    } else {
      val year = NumberConverter.read4(buf, 0)
      val month = NumberConverter.read2(buf, 5)
      val date = NumberConverter.read2(buf, 8)
      val hour = NumberConverter.read2(buf, 11)
      val minutes = NumberConverter.read2(buf, 14)
      val seconds = NumberConverter.read2(buf, 17)
      if (buf(19) == '.') {
        var nano = 0
        var max = len - 3
        var offsetSeconds = NumberConverter.read2(buf, len - 2)
        if (buf(max) == ':') {
          max = max - 3
          offsetSeconds = NumberConverter.read2(buf, len - 5) * 60 + offsetSeconds
          if (buf(max) == ':') {
            max = max - 3
            offsetSeconds = NumberConverter.read2(buf, len - 8) * 3600 + offsetSeconds
          } else offsetSeconds = offsetSeconds * 60
        } else offsetSeconds = offsetSeconds * 3600
        var i = 20
        var r = 0
        while (i < max && r < TIMESTAMP_REMINDER.length) {
          nano += TIMESTAMP_REMINDER(r) * (buf(i) - 48)
          i += 1
          r += 1
        }
        val pos = buf(max) == '+'
        if (asUtc) OffsetDateTime.of(year, month, date, hour, minutes, seconds, nano * 1000, ZoneOffset.UTC).plusSeconds((if (pos) -offsetSeconds else offsetSeconds).toLong)
        else if (offsetSeconds != 0) OffsetDateTime.of(year, month, date, hour, minutes, seconds, nano * 1000, ZoneOffset.ofTotalSeconds(if (pos) offsetSeconds else -offsetSeconds))
        else OffsetDateTime.of(year, month, date, hour, minutes, seconds, nano * 1000, ZoneOffset.UTC)
      } else if (len == 20 && buf(19) == 'Z') {
        OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.UTC)
      } else if (len == 22) {
        val pos = buf(19) == '+'
        val offset = NumberConverter.read2(buf, 20)
        if (asUtc) OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.UTC).plusHours((if (pos) -offset else offset).toLong)
        else if (offset != 0) OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.ofHours(if (pos) offset else -offset))
        else OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.UTC)
      } else if (len == 25) {
        val pos = buf(19) == '+'
        val offsetSeconds = NumberConverter.read2(buf, 20) * 3600 + NumberConverter.read2(buf, 23) * 60
        if (asUtc) OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.UTC).plusSeconds((if (pos) -offsetSeconds else offsetSeconds).toLong)
        else OffsetDateTime.of(year, month, date, hour, minutes, seconds, 0, ZoneOffset.ofTotalSeconds(if (pos) offsetSeconds else -offsetSeconds))
      } else {
        buf(10) = 'T'
        OffsetDateTime.parse(new String(buf, 0, len))
      }
    }
  }

  private class OffsetTimestampTuple(val value: OffsetDateTime) extends PostgresTuple {
    val mustEscapeRecord = true

    val mustEscapeArray = true

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      val len = serialize(sw.tmp, 0, value)
      sw.write(sw.tmp, 0, len)
    }

    override def buildTuple(quote: Boolean): String = {
      val buf = new Array[Char](32)
      if (quote) {
        buf(0) = '\''
        val len = serialize(buf, 1, value)
        buf(len) = '\''
        new String(buf, 0, len + 1)
      } else {
        val len = serialize(buf, 1, value)
        new String(buf, 0, len)
      }
    }
  }

}
