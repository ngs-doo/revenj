package net.revenj.database.postgres

import java.io.IOException
import java.sql.Connection
import javax.sql.DataSource

import com.dslplatform.compiler.client.parameters._
import com.dslplatform.compiler.client.{Context, Main}
import example.test._
import example.test.postgres.{AbcListRepository, AbcRepository}
import net.revenj.database.postgres.DbCheck.MyService
import net.revenj.extensibility.Container
import net.revenj.patterns.{DataContext, DomainEventHandler, UnitOfWork}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import ru.yandex.qatools.embed.service.PostgresEmbeddedService

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

class DbCheck extends Specification with BeforeAfterAll with ScalaCheck {
  sequential

  var tryDb: Try[PostgresEmbeddedService] = _

  def beforeAll() = {
    tryDb = DbCheck.setupDatabase()
  }
  def afterAll() = {
    if (tryDb.isSuccess) {
      tryDb.get.stop()
    }
  }
  val jdbcUrl = "jdbc:postgresql://localhost:5555/revenj?user=revenj&password=revenj"
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val duration = Duration.Inf

  "can start" >> {
    "db initialized" >> {
      tryDb.isSuccess === true
    }
    "resolve repo" >> {
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      val repoAbc = container.resolve[AbcRepository]
      val repoAbcList = container.resolve[AbcListRepository]
      val oldAbcs = Await.result(repoAbc.search(), Duration.Inf)
      val oldLists = Await.result(repoAbcList.search(), Duration.Inf)
      val abc = Abc(s = "defg", abc1 = oldAbcs.lastOption)
      if (oldAbcs.nonEmpty) {
        abc.abc2.enqueue(oldAbcs.last, oldAbcs.head)
      }
      abc.ii = Array(1, 2, 3)
      abc.iii = Some(Array(2, 3, 4))
      abc.iiii = Array(Some(2), None, Some(5))
      abc.en = En.B
      abc.en2 = Some(En.C)
      abc.en3 = mutable.LinkedList(En.B)
      abc.ss = Some("xxx")
      abc.sss = List("a", "b", "C")
      abc.ssss = Some(List(Some("x"), None))
      abc.ent1.i = 555
      abc.tt = Some(List(Some(abc.t)))
      val bytes = Array(1, 2, 3, 4).map(_.toByte)
      abc.v = Val(x = Some(5), f = 2.2f, ff = Set(Some(4.5f), None, Some(6.6f)), aa = Some(Another()), en = En.C, bytes = bytes, bb = List(bytes, bytes))
      abc.vv = Some(abc.v)
      abc.vvv = IndexedSeq(abc.v, abc.v)
      abc.ent2 = Array(Ent2(AbcID = abc.ID))
      val uri = Await.result(repoAbc.insert(abc), Duration.Inf)
      container.close()
      uri === abc.URI
    }
    "data contex usage" >> {
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      val ctx = container.resolve[DataContext]
      val oldAbcs = Await.result(ctx.search[Abc](), Duration.Inf)
      val oldLists = Await.result(ctx.search[AbcList](), Duration.Inf)
      val abcSql = Await.result(ctx.search[AbcSql](), Duration.Inf)
      val abc = Abc(s = "defg", abc1 = oldAbcs.lastOption)
      if (oldAbcs.nonEmpty) {
        abc.abc2.enqueue(oldAbcs.last, oldAbcs.head)
      }
      abc.ii = Array(1, 2, 3)
      abc.iii = Some(Array(2, 3, 4))
      abc.iiii = Array(Some(2), None, Some(5))
      abc.en = En.B
      abc.en2 = Some(En.C)
      abc.en3 = mutable.LinkedList(En.B)
      abc.ss = Some("xxx")
      abc.sss = List("a", "b", "C")
      abc.ssss = Some(List(Some("x"), None))
      abc.ent1.i = 555
      abc.tt = Some(List(Some(abc.t), None, Some(abc.t.plusDays(1))))
      val bytes = Array(1, 2, 4).map(_.toByte)
      abc.v = Val(x = Some(5), f = 2.2f, ff = Set(Some(4.5f), None, Some(6.6f)), aa = Some(Another()), en = En.C, bytes = bytes, bb = List(bytes, bytes))
      abc.vv = Some(abc.v)
      abc.vvv = IndexedSeq(abc.v, abc.v)
      abc.ent2 = Array(Ent2(AbcID = abc.ID))
      val uri = abc.URI
      Await.result(ctx.create(abc), Duration.Inf)
      container.close()
      abc.URI !== uri
    }
    "unit of work usage" >> {
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      val ctx = container.resolve[UnitOfWork]
      val oldAbcs = Await.result(ctx.search[Abc](), Duration.Inf)
      val oldLists = Await.result(ctx.search[AbcList](), Duration.Inf)
      val abc = Abc(s = "defg", abc1 = oldAbcs.lastOption)
      if (oldAbcs.nonEmpty) {
        abc.abc2.enqueue(oldAbcs.last, oldAbcs.head)
      }
      abc.ii = Array(1, 2, 3)
      abc.iii = Some(Array(2, 3, 4))
      abc.iiii = Array(Some(2), None, Some(5))
      abc.en = En.B
      abc.en2 = Some(En.C)
      abc.en3 = mutable.LinkedList(En.B)
      abc.ss = Some("xxx")
      abc.sss = List("a", "b", "C")
      abc.ssss = Some(List(Some("x"), None))
      abc.ent1.i = 555
      val bytes = Array(1, 4).map(_.toByte)
      abc.v = Val(x = Some(5), f = 2.2f, ff = Set(Some(4.5f), None, Some(6.6f)), aa = Some(Another()), en = En.C, bytes = bytes, bb = List(bytes, bytes))
      abc.vv = Some(abc.v)
      abc.vvv = IndexedSeq(abc.v, abc.v)
      abc.ent2 = Array(Ent2(AbcID = abc.ID))
      Await.result(ctx.create(abc), Duration.Inf)
      val find = Await.result(ctx.find[Abc](abc.URI), Duration.Inf).get
      container.close()
      ctx.commit(Duration.Inf).isSuccess === true
      find.URI === abc.URI
    }
    "persistable sql" >> {
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      val ctx = container.resolve[DataContext]
      val abcWrite = Await.result(ctx.search[AbcWrite](), Duration.Inf)
      abcWrite.headOption match {
        case Some(first) =>
          first.en = En.C
          first.ii = Array(12, 3) ++ first.ii
          Await.result(ctx.update(abcWrite), Duration.Inf)
          val find = Await.result(ctx.find[AbcWrite](first.URI), Duration.Inf).get
          container.close()
          find.URI === first.URI
        case _ =>
          1 === 1
      }
    }
    "context from connection" >> {
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      container.registerClass(classOf[MyService], singleton = false)
      val ds = container.resolve[DataSource]
      val conn = ds.getConnection
      val service = container.resolve[MyService]
      val ctx = service.factory(conn)
      val abc = Abc(s = "ctx")
      val uri = abc.URI
      Await.result(ctx.create(abc), Duration.Inf)
      conn.close()
      container.close()
      abc.URI !== uri
    }
    "can submit events" >> {
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      val ctx = container.resolve[UnitOfWork]
      val ev = TestMe(x = 100, ss = Array("1", "3"), vv = Val(x = Some(5)), vvv = Some(List(Some(Val(x = Some(3))))))
      val total = Await.result(ctx.count[TestMe](), Duration.Inf)
      DbCheck.EventHandlerCounters.resetCounters()
      Await.result(ctx.submit(ev), Duration.Inf)
      ev.URI.length !== 0
      val newTotal = Await.result(ctx.count[TestMe](), Duration.Inf)
      val all = Await.result(ctx.search[TestMe](), Duration.Inf)
      val found = Await.result(ctx.find[TestMe](ev.URI), Duration.Inf)
      found.isDefined === true
      ev.URI === found.get.URI
      ctx.commit(Duration.Inf).isSuccess === true
      container.close()
      newTotal == total + 1
      DbCheck.EventHandlerCounters.simpleCounter === 1
      DbCheck.EventHandlerCounters.arrayCounter === 1
      DbCheck.EventHandlerCounters.funcCounter === 1
      DbCheck.EventHandlerCounters.arrayFuncCounter === 1
    }
    "search with spec" >> {
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      val ctx = container.resolve[UnitOfWork]
      val x = (new java.util.Date().getTime / 10000).asInstanceOf[Int]
      val ev = TestMe(x = x)
      Await.result(ctx.submit(ev), Duration.Inf)
      val find = Await.result(ctx.search(TestMe.Filter(x, x)), Duration.Inf)
      ctx.commit(Duration.Inf).isSuccess === true
      container.close()
      find.size === 1
      find.head.x === x
    }
    "report test" >> {
      val container = example.Boot.configure(jdbcUrl).asInstanceOf[Container]
      val ctx = container.resolve[UnitOfWork]
      val x = (new java.util.Date().getTime / 10000).asInstanceOf[Int]
      val ev = TestMe(x = x)
      Await.result(ctx.submit(ev), Duration.Inf)
      val rep = ReportMe(x = x)
      val result = Await.result(ctx.populate(rep), Duration.Inf)
      ctx.commit(Duration.Inf).isSuccess === true
      container.close()
      result.events.exists(_.URI == ev.URI) === true
    }
  }
}

object DbCheck {

  class ExampleEventHandler extends DomainEventHandler[TestMe] {
    override def handle(domainEvent: TestMe): Unit = {
      if (domainEvent.URI.isEmpty) {
        EventHandlerCounters.simpleCounter += 1
      }
    }
  }
  class ExampleArrayEventHandler extends DomainEventHandler[Array[TestMe]] {
    override def handle(events: Array[TestMe]): Unit = {
      if (!events.exists(_.URI.nonEmpty)) {
        EventHandlerCounters.arrayCounter += 1
      }
    }
  }
  class ExampleFuncEventHandler extends DomainEventHandler[Function0[TestMe]] {
    override def handle(event: Function0[TestMe]): Unit = {
      if (event().URI.nonEmpty) {
        EventHandlerCounters.funcCounter += 1
      }
    }
  }
  class ExampleFuncArrayEventHandler extends DomainEventHandler[Function0[Array[TestMe]]] {
    override def handle(events: Function0[Array[TestMe]]): Unit = {
      if (!events().exists(_.URI.isEmpty)) {
        EventHandlerCounters.arrayFuncCounter += 1
      }
    }
  }
  object EventHandlerCounters {
    var simpleCounter = 0
    var arrayCounter = 0
    var funcCounter = 0
    var arrayFuncCounter = 0
    def resetCounters(): Unit = {
      simpleCounter = 0
      arrayCounter = 0
      funcCounter = 0
      arrayFuncCounter = 0
    }
  }

  class MyService(val factory: Connection => DataContext)

  private class TestContext extends Context {
    val error = new StringBuilder

    override def show(values: String*): Unit = {}

    override def log(value: String): Unit = {}

    override def log(value: Array[Char], len: Int): Unit = {}

    override def error(value: String): Unit = {
      error.append(value)
    }

    override def error(ex: Exception): Unit = {
      error.append(ex.getMessage)
    }
  }

  def setupDatabase(): Try[PostgresEmbeddedService] = {
    Try {
      val postgres = new PostgresEmbeddedService("localhost", 5555, "revenj", "revenj", "revenj", "target/db", true, 5000)
      postgres.start()
      Thread.sleep(200)
      val context = new TestContext
      context.put(Download.INSTANCE, "")
      context.put(Force.INSTANCE, "")
      context.put(ApplyMigration.INSTANCE, "")
      context.put(DisablePrompt.INSTANCE, "")
      context.put(PostgresConnection.INSTANCE, "localhost:5555/revenj?user=revenj&password=revenj")
      val file = getClass.getResource("/model.dsl")
      context.put(DslPath.INSTANCE, file.getFile)
      val params = Main.initializeParameters(context, ".")
      if (!Main.processContext(context, params)) {
        Thread.sleep(2000)
        context.error.setLength(0)
        if (!Main.processContext(context, params)) {
          throw new IOException("Unable to migrate database: " + context.error.toString)
        }
      }
      postgres
    }
  }
}