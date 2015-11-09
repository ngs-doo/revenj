package org.revenj

import java.io.IOException

import _root_.gen.model.Boot
import gen.model.adt.User
import org.revenj.extensibility.Container

import org.junit._
import org.revenj.patterns._
import org.revenj.postgres.jinq.ScalaSpecification
import org.revenj.postgres.jinq.transform.{LambdaAnalysis, MetamodelUtil, LambdaInfo}

class TestLambda {

  private var container: Container = null

  @Before
  @throws(classOf[IOException])
  def initContainer {
    container = Boot.configure("jdbc:postgresql://localhost/revenj").asInstanceOf[Container]
  }

  @After
  def closeContainer() {
    container.close
  }

  class RichQuery[T <: DataSource](query: Query[T]) {
    def where(predicate: T => Boolean) = query.filter(new ScalaSpecification[T](predicate))
  }

  implicit def where[T <: DataSource](query: Query[T]): RichQuery[T] = new RichQuery[T](query)

  @Test
  def testSimpleLambda {
    val ctx = container.resolve(classOf[DataContext])
    val result = ctx.query(classOf[User]).where(it => it.getUsername == "doesn't exists").findAny()
    Assert.assertFalse(result.isPresent)
  }
}