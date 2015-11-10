package org.revenj

import java.io.IOException

import _root_.gen.model.Boot
import gen.model.adt.User
import org.revenj.extensibility.Container

import org.junit._
import org.revenj.patterns._
import org.revenj.postgres.jinq.{ScalaSort, ScalaSpecification}

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

  class RichSort[T <: DataSource, V <: java.lang.Comparable[V]](query: Query[T]) {
    def orderBy(fn: T => V)= query.sortedBy(new ScalaSort[T, V](fn))
  }

  implicit def orderBy[T <: DataSource, V <: java.lang.Comparable[V]](query: Query[T]): RichSort[T, V] = new RichSort[T, V](query)

  @Test
  def testSimpleOrder {
    val ctx = container.resolve(classOf[DataContext])
    val result = ctx.query(classOf[User]).orderBy(it => it.getUsername).findAny()
    Assert.assertNotNull(result)
  }
}