package net.revenj.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import example.test._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class JsonCheck extends Specification with ScalaCheck {
  private val json = new ObjectMapper()
    .registerModule(DefaultScalaModule)
    .registerModule(new JodaModule)

  "can deserialize" >> {
    "jackson" >> {
      val res = json.writeValueAsString(Struct(i=5))
//      println(res)
      val str = json.readValue(res, classOf[Struct])
      str.i === 5
    }
    "collection of enums" >> {
      val res = json.writeValueAsString(Val(en3=List(En.B, En.C)))
//      println(res)

      val str = json.readValue(res, classOf[Val])
      str.en3 ==== List(En.B, En.C)
      str.enSize ==== 2

      val str2 = json.readValue(res.replace("\"enSize\":2", "\"enSize\":9001"), classOf[Val])
      str.en3 ==== List(En.B, En.C)
      str2.enSize ==== 2
    }
  }
}
