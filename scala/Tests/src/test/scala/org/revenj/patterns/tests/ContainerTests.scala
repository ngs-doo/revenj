package org.revenj.patterns
package tests

import org.scalatest._, junit._
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ContainerTests
    extends FeatureSpec with GivenWhenThen with Matchers {

  feature("simple container operations") {
    scenario("can create container instance") {
      val container = new DependencyContainer

      container should not be (null)
    }
  }


  feature("resolve operations") {
    scenario("reified generics from root") {

      val container = new DependencyContainer
      container.register[AggRep, IRepository[Agg]]
      val rep = container.resolve[IRepository[Agg]]

      rep should not be (null)
    }

    scenario("reified generics dependency") {

      val container = new DependencyContainer
      container.register[AggRep, IRepository[Agg]]
      container.register[AggPersistRep, IPersistableRepository[Agg]]
      val rep = container.resolve[IPersistableRepository[Agg]]

      rep should not be (null)
    }

    scenario("multiple same types") {

      val container = new DependencyContainer
      container.register[AggRep, IRepository[Agg]]
      container.register[SnowRep, IRepository[Snow]]
      val repA = container.resolve[IRepository[Agg]]
      val repS = container.resolve[IRepository[Snow]]

      repA should not be (null)
      repS should not be (null)
      repA should not be (repS)
    }

    scenario("collection types") {

      val container = new DependencyContainer
      container.register[IIdentifiable](Agg("aq"))

      var ii = container.resolve[Array[IIdentifiable]]
      ii.length should be (1)

      container.register[IIdentifiable](Snow("bc"))
      ii = container.resolve[Array[IIdentifiable]]

      ii.length should be (2)
      ii(0) should be (Agg("aq"))
      ii(1) should be (Snow("bc"))
    }
  }

  feature("Container use cases") {
    scenario("Registration of object instances") {
      val container: IContainer = new DependencyContainer
      container.register(Single)
    }
  }
}

  object Single extends IIdentifiable {
    def URI = toString
  }

  case class Agg(URI: String) extends IIdentifiable
  case class Snow(URI: String) extends IIdentifiable
  class AggRep(locator: IServiceLocator) extends IRepository[Agg] {
    def find(uris: Traversable[String]) = {
      uris map(u => Agg(u)) toIndexedSeq
    }
  }
  class SnowRep(locator: IServiceLocator) extends IRepository[Snow] {
    def find(uris: Traversable[String]) = {
      uris map(u => Snow(u)) toIndexedSeq
    }
  }

  class AggPersistRep(locator: IServiceLocator, rep: IRepository[Agg])
      extends IPersistableRepository[Agg] {
    def find(uris: Traversable[String]) = {
      uris map(u => Agg(u)) toIndexedSeq
    }
    def persist(insert: Traversable[Agg],
              update: Traversable[(Agg, Agg)],
              delete: Traversable[Agg]) = {
      insert.map(_.URI).toIndexedSeq
    }
  }
