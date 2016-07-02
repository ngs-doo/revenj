package net.revenj.database.postgres.converters

import example.test._
import example.test.postgres.{AbcListRepository, AbcRepository}
import net.revenj.extensibility.Container
import net.revenj.patterns.{DataContext, UnitOfWork}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DbCheck extends Specification with ScalaCheck {
  "can start" >> {
    val jdbcUrl = "jdbc:postgresql://localhost/revenj3?user=postgres"
    "resolve repo" >> {
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val duration = Duration.Inf
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      val repoAbc = container.resolve[AbcRepository]
      val repoAbcList = container.resolve[AbcListRepository]
      val oldAbcs = Await.result(repoAbc.search(), Duration.Inf)
      val oldLists = Await.result(repoAbcList.search(), Duration.Inf)
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
      val uri = Await.result(repoAbc.insert(abc), Duration.Inf)
      container.close()
      uri === abc.URI
    }
    "data contex usage" >> {
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val duration = Duration.Inf
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      val ctx = container.resolve[DataContext]
      val oldAbcs = Await.result(ctx.search[Abc](), Duration.Inf)
      val oldLists = Await.result(ctx.search[AbcList](), Duration.Inf)
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
      Await.result(ctx.create(abc), Duration.Inf)
      container.close()
      abc.URI !== null
    }
    "unit of work usage" >> {
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val duration = Duration.Inf
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      val ctx = container.resolve[UnitOfWork]
      val oldAbcs = Await.result(ctx.search[Abc](), Duration.Inf)
      val oldLists = Await.result(ctx.search[AbcList](), Duration.Inf)
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
      Await.result(ctx.create(abc), Duration.Inf)
      container.close()
      ctx.commit(Duration.Inf).isSuccess === true
    }
  }
}
