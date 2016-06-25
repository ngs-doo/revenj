package net.revenj.database.postgres.converters

import java.util

import example.test._
import example.test.postgres.AbcRepository
import net.revenj.extensibility.Container
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DbCheck extends Specification with ScalaCheck {
  "can start" >> {
    "resolve repo" >> {
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val duration = Duration.Inf
      val container = example.Boot.configure("jdbc:postgresql://localhost/revenj3?user=postgres").asInstanceOf[Container]
      val repo = container.resolve[AbcRepository]
      val oldAbcs = Await.result(repo.search(), Duration.Inf)
      val abc = Abc(s = "defg", abc1 = oldAbcs.lastOption)
      if (oldAbcs.nonEmpty) {
        abc.abc2.enqueue(oldAbcs.last, oldAbcs.head)
      }
      abc.ii = Array(1,2,3)
      abc.iii = Some(Array(2,3,4))
      abc.iiii = Array(Some(2), None, Some(5))
      abc.en = En.B
      abc.en2 = Some(En.C)
      abc.en3 = mutable.LinkedList(En.B)
      abc.ss = Some("xxx")
      abc.sss = List("a","b","C")
      abc.ssss = Some(List(Some("x"), None))
      abc.ent1.i = 555
      abc.v = Val(x = Some(5), f = 2.2f, ff = Set(Some(4.5f), None, Some(6.6f)), aa = Some(Another()), en = En.C)
      abc.vv = Some(abc.v)
      abc.vvv = IndexedSeq(abc.v, abc.v)
      abc.ent2 = Array(Ent2(AbcID = abc.ID))
      val uri = Await.result(repo.insert(abc), Duration.Inf)
      container.close()
      uri !== null
    }
  }
}
