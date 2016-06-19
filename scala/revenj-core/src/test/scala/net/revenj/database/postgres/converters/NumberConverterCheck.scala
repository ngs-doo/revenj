package net.revenj.database.postgres.converters

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import org.scalacheck._

class NumberConverterCheck extends Specification with ScalaCheck {
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
        val buffer = new Array[Char](20)
        val length = NumberConverter.serialize(longValue, buffer)
        val strValue = longValue.toString.toCharArray
        buffer.drop(length) ==== strValue
      }
    }
  }
}
