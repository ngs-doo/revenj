package net.revenj.database.postgres.converters

import java.io.IOException
import java.time.OffsetDateTime
import java.util.{Base64, UUID}

import net.revenj.database.postgres.{PostgresReader, PostgresWriter}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.scalacheck._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ConverterCheck extends Specification with ScalaCheck {
  "number parsing" >> {
    "parse Long" >> {
      Prop.forAllNoShrink { longValue: Long =>
        val strValue = longValue.toString
        NumberConverter.parseLong(strValue) ==== longValue
      }
    }
  }

  "number writing" >> {
    "buffer length for Int check" >> {
      Int.MinValue.toString.length ==== 11
    }

    "serialize Int" >> {
      Prop.forAllNoShrink { intValue: Int =>
        val buffer = new Array[Char](11)
        val length = NumberConverter.serialize(intValue, buffer)
        val strValue = intValue.toString.toCharArray
        buffer.drop(length) ==== strValue
      }
    }

    "buffer length for Long check" >> {
      Long.MinValue.toString.length ==== 20
    }

    "serialize Long" >> {
      Prop.forAllNoShrink { longValue: Long =>
        val buffer = new Array[Char](21)
        val length = NumberConverter.serialize(longValue, buffer)
        val strValue = longValue.toString.toCharArray
        buffer.drop(length) ==== strValue
      }
    }
  }

  "edge cases" >> {
    "float" >> {
      val reader = new PostgresReader
      val floats = List(0f, -0.000012345f, -0.00001f, Float.NaN, Float.NegativeInfinity, Float.PositiveInfinity)
      val tuple: PostgresTuple = ArrayTuple.createSeq(floats, FloatConverter.toTuple)
      val value: String = tuple.buildTuple(false)
      reader.process(value)
      val result = FloatConverter.parseCollection(reader, 0)
      floats.zip(result).foreach { case (l, r) =>
          l.equals(r) === true
      }
      floats.size === result.size
      //floats === result.toList // TODO: in scala Float.NaN != Float.NaN
    }

    "uuid" >> {
      val reader = new PostgresReader
      val uuids = List(UUID.randomUUID, UUID.randomUUID, UUID.randomUUID)
      val tuple = ArrayTuple.createSeq(uuids, UuidConverter.toTuple)
      val value = tuple.buildTuple(false)
      reader.process(value)
      val result = UuidConverter.parseCollection(reader, 0)
      uuids === result.toList
    }

    "map" >> {
      val reader = new PostgresReader
      val maps = new ArrayBuffer[Option[Map[String, String]]]
      maps += None
      maps += Some(Map.empty)
      val ab = new mutable.HashMap[String, String]()
      ab += "a" -> "b"
      maps += Some(ab.toMap)
      val cplx = new mutable.HashMap[String, String]()
      cplx += "a ' \\ x" -> "\" b \\ '"
      maps += Some(cplx.toMap)
      val tuple = ArrayTuple.createSeqOption(maps, HstoreConverter.toTuple)
      val value = tuple.buildTuple(false)
      reader.process(value)
      val result = HstoreConverter.parseNullableCollection(reader, 0)
      maps.toList === result.toList
    }

    "binary" >> {
      val reader = new PostgresReader
      val bytes = Base64.getDecoder.decode("gAB/")
      ByteaConverter.serializeURI(reader, bytes)
      val uri = reader.bufferToString()
      uri === "\\x80007f"
    }

    "timestamp utc zone" >> {
      val reader = new PostgresReader
      reader.process("{NULL,\"2015-09-28 13:35:42.973+02:00\",\"1970-01-01 01:00:00+01:00\",\"0001-01-01 00:00:00Z\",\"2038-02-13 00:45:30.647+01:00\"}")
      val values = UtcTimestampConverter.parseNullableCollection(reader, 0)
      values.size === 5
      values.head === None
    }

    "large date" >> {
      val reader = new PostgresReader
      reader.process("{NULL,20215-09-28,1970-01-01}")
      val values = DateConverter.parseNullableCollection(reader, 0)
      values.size === 3
      values.head === None
      values.drop(1).head.get.getYear === 20215
      values.drop(1).head.get.getMonthValue === 9
      values.drop(1).head.get.getDayOfMonth === 28
    }

    "timestamp local zone" >> {
      val reader = new PostgresReader
      reader.process("{NULL,\"2015-09-28 13:35:42.973+02:00\",\"1970-01-01 01:00:00+01:00\",\"0001-01-01 00:00:00Z\",\"2038-02-13 00:45:30.647+01:00\"}")
      val values = LocalTimestampConverter.parseNullableCollection(reader, 0)
      values.size === 5
      values.head === None
    }

    "large timestamp" >> {
      val reader = new PostgresReader
      reader.process("{NULL,\"20315-09-28 13:35:42.973+02:00\",\"1970-01-01 01:00:00+01:00\"}")
      val values = LocalTimestampConverter.parseNullableCollection(reader, 0)
      values.size === 3
      values.head === None
      values.drop(1).head.get.getYear === 20315
      values.drop(1).head.get.getMonthValue === 9
      values.drop(1).head.get.getDayOfMonth === 28
    }

    "timestamp with offset" >> {
      val reader = new PostgresReader
      reader.process("{\"0001-01-01 00:00:00+01:22\"}")
      val values1 = LocalTimestampConverter.parseNullableCollection(reader, 0)
      values1.size === 1
      values1.head.get.getOffset.getTotalSeconds == 4920
      values1.head.get.getMinute === 0
      reader.process("{\"0001-01-01 00:00:00+01:22\"}")
      val values2 = UtcTimestampConverter.parseNullableCollection(reader, 0)
      values2.size === 1
      values2.head.get.getOffset.getTotalSeconds == 0
      values2.head.get.getMinute == 38
    }

    "special timestamp" >> {
      val writer = new PostgresWriter
      val zero = OffsetDateTime.parse("0001-01-01T00:00:00+01:22")
      LocalTimestampConverter.serializeURI(writer, zero)
      val value = writer.bufferToString()
      "0001-01-01 00:00:00+01:22" === value
    }

    "timestamp uri normalization" >> {
      val writer = new PostgresWriter
      val zero = OffsetDateTime.parse("2001-01-01T00:00:00+01:22")
      LocalTimestampConverter.serializeURI(writer, zero)
      val value = writer.bufferToString()
      "2000-12-31 23:38:00+01" === value
    }

    "uri parsing" >> {
      val arr = new Array[String](2)
      PostgresReader.parseCompositeURI("1234/", arr)
      "1234" === arr(0)
      "" === arr(1)
      PostgresReader.parseCompositeURI("123/3456", arr)
      "123" === arr(0)
      "3456" === arr(1)
      PostgresReader.parseCompositeURI("\\\\123\\/34/56", arr)
      "\\123/34" === arr(0)
      "56" === arr(1)
      try {
        PostgresReader.parseCompositeURI("123/34/56", arr)
        1 === 0
      } catch {
        case ex: IOException =>
          ex.getMessage.contains("Number of expected parts: 2") === true
      }
    }

    "composite uri" >> {
      val sb = new StringBuilder
      PostgresWriter.writeCompositeUri(sb, "ab\\\\cd/de''fg")
      "('ab\\cd','de''''fg')" === sb.toString
    }

    "ints" >> {
      val reader = new PostgresReader
      val ints = List(0, 1, -1, Int.MaxValue, Int.MinValue, Int.MaxValue - 1, Int.MinValue + 1)
      val tuple = ArrayTuple.createSeq(ints, IntConverter.toTuple)
      val value = tuple.buildTuple(false)
      reader.process(value)
      val result = IntConverter.parseCollection(reader, 0).toList
      ints === result
    }

    "longs" >> {
      val reader = new PostgresReader
      val longs = List(0L, 1L, -1L, Long.MaxValue, Long.MinValue, Long.MaxValue - 1, Long.MinValue + 1)
      val tuple = ArrayTuple.createSeq(longs, LongConverter.toTuple)
      val value = tuple.buildTuple(false)
      reader.process(value)
      val result = LongConverter.parseCollection(reader, 0).toList
      longs === result
    }
  }
}
