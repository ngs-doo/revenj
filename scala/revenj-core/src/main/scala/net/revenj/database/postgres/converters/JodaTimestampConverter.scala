package net.revenj.database.postgres.converters

import java.sql.PreparedStatement

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}
import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}
import org.postgresql.util.PGobject

object JodaUtcTimestampConverter extends JodaTimestampConverter(true)

object JodaLocalTimestampConverter extends JodaTimestampConverter(false)

class JodaTimestampConverter(asUtc: Boolean) extends Converter[DateTime] {

  import JodaTimestampConverter._

  val dbName = "timestamptz"

  def default() = MIN_DATE_TIME_UTC

  override def serializeURI(sw: PostgresBuffer, value: DateTime): Unit = {
    val len = serialize(sw.tempBuffer, 0, value)
    sw.addToBuffer(sw.tempBuffer, len)
  }

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): DateTime = parseDateTime(reader, context, asUtc)

  override def parseCollectionItem(reader: PostgresReader, context: Int): DateTime = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      MIN_DATE_TIME_UTC
    } else {
      parseDateTime(reader, context, asUtc)
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[DateTime] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      Some(parseDateTime(reader, context, asUtc))
    }
  }

  def toTuple(value: DateTime): PostgresTuple = {
    new OffsetTimestampTuple(value)
  }
}

object JodaTimestampConverter {
  private val MIN_DATE_TIME_UTC = DateTime.parse("0001-01-01T00:00:00Z")
  private val TIMESTAMP_REMINDER = Array[Int](100000, 10000, 1000, 100, 10, 1)

  def setParameter(sw: PostgresBuffer, ps: PreparedStatement, index: Int, value: DateTime): Unit = {
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
    NumberConverter.write2(value.getMonthOfYear, buffer, pos + 5)
    NumberConverter.write2(value.getDayOfMonth, buffer, pos + 8)
    NumberConverter.write2(value.getHourOfDay, buffer, pos + 11)
    NumberConverter.write2(value.getMinuteOfHour, buffer, pos + 14)
    NumberConverter.write2(value.getSecondOfMinute, buffer, pos + 17)
    val millis = value.getMillisOfSecond
    var end = pos + 19
    if (millis != 0) {
      buffer(pos + 19) = '.'
      NumberConverter.write3(millis, buffer, 20)
      end = pos + 22
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
  private val ZERO_OFFSET = 82 * 60

  def serialize(buffer: Array[Char], start: Int, value: DateTime): Int = {
    val offset = value.getZone.getOffset(value.getMillis) / 1000
    if (value.getYear < 1884) {
      val at122 = value.toLocalDateTime.plusSeconds(ZERO_OFFSET - offset)
      val pos = serialize(buffer, start, at122)
      buffer(pos - 1) = '1'
      buffer(pos) = ':'
      buffer(pos + 1) = '2'
      buffer(pos + 2) = '2'
      pos + 3
    } else {
      val offsetHours = offset / 3600
      val offsetDiff = offset - offsetHours * 3600
      if (offsetDiff != 0) serializeNormalized(buffer, start, value.toLocalDateTime.minusSeconds(offsetDiff), offsetHours)
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
    NumberConverter.write2(value.getMonthOfYear, buffer, pos + 5)
    NumberConverter.write2(value.getDayOfMonth, buffer, pos + 8)
    NumberConverter.write2(value.getHourOfDay, buffer, pos + 11)
    NumberConverter.write2(value.getMinuteOfHour, buffer, pos + 14)
    NumberConverter.write2(value.getSecondOfMinute, buffer, pos + 17)
    val millis = value.getMillisOfSecond
    var end = pos + 19
    if (millis != 0) {
      buffer(pos + 19) = '.'
      NumberConverter.write3(millis, buffer, 20)
      end = pos + 22
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

  private def parseDateTime(reader: PostgresReader, context: Int, asUtc: Boolean): DateTime = {
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
      DateTime.parse(new String(buf, 0, len))
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
        if (asUtc) new DateTime(year, month, date, hour, minutes, seconds, nano / 1000, DateTimeZone.UTC).plusSeconds(if (pos) -offsetSeconds else offsetSeconds)
        else if (offsetSeconds != 0) new DateTime(year, month, date, hour, minutes, seconds, nano / 1000, DateTimeZone.forOffsetMillis((if (pos) offsetSeconds else -offsetSeconds) * 1000))
        else new DateTime(year, month, date, hour, minutes, seconds, nano / 1000, DateTimeZone.UTC)
      } else if (len == 20 && buf(19) == 'Z') new DateTime(year, month, date, hour, minutes, seconds, 0, DateTimeZone.UTC)
      else if (len == 22) {
        val pos = buf(19) == '+'
        val offset = NumberConverter.read2(buf, 20)
        if (asUtc) new DateTime(year, month, date, hour, minutes, seconds, 0, DateTimeZone.UTC).plusHours(if (pos) -offset else offset)
        else if (offset != 0) new DateTime(year, month, date, hour, minutes, seconds, 0, DateTimeZone.forOffsetHours(if (pos) offset else -offset))
        else new DateTime(year, month, date, hour, minutes, seconds, 0, DateTimeZone.UTC)
      } else if (len == 25) {
        val pos = buf(19) == '+'
        val offsetSeconds = NumberConverter.read2(buf, 20) * 3600 + NumberConverter.read2(buf, 23) * 60
        if (asUtc) new DateTime(year, month, date, hour, minutes, seconds, 0, DateTimeZone.UTC).plusSeconds(if (pos) -offsetSeconds else offsetSeconds)
        else new DateTime(year, month, date, hour, minutes, seconds, 0, DateTimeZone.forOffsetMillis((if (pos) offsetSeconds else -offsetSeconds ) * 1000))
      } else {
        buf(10) = 'T'
        DateTime.parse(new String(buf, 0, len))
      }
    }
  }

  private class OffsetTimestampTuple(val value: DateTime) extends PostgresTuple {
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
