package net.revenj.database.postgres.converters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import example.test._
import example.test.postgres.AbcRepository
import net.revenj.extensibility.Container
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class JsonCheck extends Specification with ScalaCheck {
  "can deserialize" >> {
    "jackson" >> {
      val json = new ObjectMapper().registerModule(new DefaultScalaModule)
      val res = json.writeValueAsString(Struct(i=5))
      println(res)
      val str = json.readValue(res, classOf[Struct])
      str.i === 5
    }
  }
}
