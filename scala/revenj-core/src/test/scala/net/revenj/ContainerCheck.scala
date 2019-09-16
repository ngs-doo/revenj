package net.revenj

import java.time.OffsetDateTime

import net.revenj.extensibility.{Container, InstanceScope}
import net.revenj.patterns.{AggregateDomainEvent, AggregateDomainEventHandler, AggregateRoot, DomainEventHandler, ServiceLocator}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.util.Try
import scala.reflect.runtime.universe

class ContainerCheck extends Specification with ScalaCheck {
  sequential

  val cl = classOf[ContainerCheck].getClassLoader

  "simple resolutions" >> {
    "types" >> {
      B.counter = 0
      val container = new SimpleContainer(false, cl)
      container.register[A]()
      val tryA1 = container.tryResolve[A]
      tryA1.isSuccess === false
      container.register[B]()
      val tryA2 = container.tryResolve[A]
      tryA2.isSuccess === true
      val a = tryA2.get
      a !== null
      1 === B.counter
    }
    "option" >> {
      B.counter = 0
      val container = new SimpleContainer(false, cl)
      container.register[A]()
      container.register[B]()
      container.register[C]()
      var c = container.resolve[C]
      c !== null
      1 === B.counter
      container.register[D]()
      c = container.resolve[C]
      c !== null
      2 === B.counter
    }
    "generics" >> {
      B.counter = 0
      val container = new SimpleContainer(false, cl)
      container.register[A]()
      container.register[B]()
      container.register[G[_]]()
      val g = container.resolve[G[A]]
      g !== null
      1 === B.counter
      g.instance.isInstanceOf[A] === true
      classOf[A] === g.instance.getClass
    }
    "complex generics" >> {
      B.counter = 0
      val container = new SimpleContainer(false, cl)
      container.register[A]()
      container.register[B]()
      container.register[ComplexGenerics[_, _]]()
      val cg = container.resolve[ComplexGenerics[A, B]]
      cg !== null
      2 === B.counter
      cg.instance1.isInstanceOf[A] === true
      classOf[A] === cg.instance1.getClass
      cg.instance2.isInstanceOf[B] === true
      classOf[B] === cg.instance2.getClass
    }
    "ctor raw generics" >> {
      B.counter = 0
      val container = new SimpleContainer(false, cl)
      container.register[A]()
      container.register[B]()
      container.register[ComplexGenerics[_, _]]()
      container.register[CtorRawGenerics]()
      val cg = container.resolve[CtorRawGenerics]
      cg !== null
      2 === B.counter
      cg.generics.instance1.isInstanceOf[A] === true
      classOf[A] === cg.generics.instance1.getClass
      cg.generics.instance2.isInstanceOf[B] === true
      classOf[B] === cg.generics.instance2.getClass
    }
    "ctor partial generics" >> {
      B.counter = 0
      val container = new SimpleContainer(false, cl)
      container.register[A]()
      container.register[B]()
      container.register[ComplexGenerics[_, _]]()
      container.register[CtorPartialGenerics[_]]()
      val cg = container.resolve[CtorPartialGenerics[A]]
      cg !== null
      2 === B.counter
      cg.generics.instance1.isInstanceOf[A] === true
      classOf[A] === cg.generics.instance1.getClass
      cg.generics.instance2.isInstanceOf[B] === true
      classOf[B] === cg.generics.instance2.getClass
    }
    "ctor generics and primitives" >> {
      val container = new SimpleContainer(false, cl)
      container.register[B]()
      container.registerInstance(new ComplexGenerics[Int, B](new B(), 6), false)
      container.register[CtorPartialGenerics[_]]()
      val cg = container.resolve[ComplexGenerics[Int, B]]
      cg.instance1 === 6
      val p = container.resolve[CtorPartialGenerics[Int]]
      p.generics.instance1 === 6
      p.generics === cg
    }
    "generics and bad primitive" >> {
      val container = new SimpleContainer(false, cl)
      container.register[B]()
      container.registerInstance(new ComplexGenerics[Int, B](new B(), 6), false)
      container.register[CtorPartialGenerics[_]]()
      val cg = container.resolve[ComplexGenerics[Int, B]]
      cg.instance1 === 6
      val p = container.tryResolve[CtorPartialGenerics[Long]]
      p.isFailure === true
      p.failed.get.getMessage.contains("net.revenj.ComplexGenerics<long, net.revenj.B> and class net.revenj.ComplexGenerics are not registered in the container.") === true
    }
    "generics and option" >> {
      val container = new SimpleContainer(false, cl)
      container.register[B]()
      container.registerInstance(new ComplexGenerics[Int, B](new B(), 6), false)
      container.register[CtorPartialGenerics[_]]()
      val cg = container.resolve[ComplexGenerics[Int, B]]
      cg.instance1 === 6
      val p = container.resolve[Option[CtorPartialGenerics[Long]]]
      p === None
    }
    "pass exception" >> {
      val container = new SimpleContainer(false, cl)
      container.registerFunc[ContainerCheck](_ => throw new RuntimeException("test me now"))
      val cc = container.tryResolve[ContainerCheck]
      cc.isFailure === true
      cc.failed.get.getMessage.contains("test me now") === true
    }
    "resolve unknown" >> {
      B.counter = 0
      val container = new SimpleContainer(true, cl)
      val a = container.resolve[A]
      a !== null
      1 === B.counter
    }
    "resolve later" >> {
      val container = new SimpleContainer(true, cl)
      val l = container.resolve[Later]
      l !== null
      val a = l.getA()
      a !== null
    }
    "self reference singleton" >> {
      val container = new SimpleContainer(false, cl)
      container.register[SelfReference](InstanceScope.Singleton)
      val sr = container.resolve[SelfReference]
      val sr2 = sr.getSelf()
      sr === sr2
    }
    "singleton in context" >> {
      val container = new SimpleContainer(false, cl)
      container.register[Single](InstanceScope.Singleton)
      val nested = container.createScope()
      val s1 = nested.resolve[Single]
      val s2 = container.resolve[Single]
      s1 === s2
    }
    "collection resolution" >> {
      val container = new SimpleContainer(false, cl)
      container.registerAs[DomainEventHandler[Single], Handler1]()
      container.registerAs[DomainEventHandler[Single], Handler2]()
      val found = container.resolve[Array[DomainEventHandler[Single]]]
      found.length === 2
    }
    "collection signature resolution" >> {
      val container = new SimpleContainer(false, cl)
      container.registerAs[DomainEventHandler[Single], Handler1]()
      container.registerAs[DomainEventHandler[Single], Handler2]()
      container.registerAs[DomainEventHandler[Function0[Single]], Handler3]()
      val found1 = container.resolve[Array[DomainEventHandler[Single]]]
      found1.length === 2
      val found2 = container.resolve[Array[DomainEventHandler[Function0[Single]]]]
      found2.length === 1
    }
    "sequence resolution" >> {
      val container = new SimpleContainer(false, cl)
      container.registerAs[DomainEventHandler[Single], Handler1]()
      container.registerAs[DomainEventHandler[Single], Handler2]()
      container.registerAs[DomainEventHandler[Function0[Single]], Handler3]()
      val found = container.resolve[Seq[DomainEventHandler[Single]]]
      found.length === 2
    }
    "complex sequence resolution" >> {
      val container = new SimpleContainer(false, cl)
      container.registerAs[AggregateDomainEventHandler[Agg, AggEvent], AggEventHandler1]()
      container.registerAs[AggregateDomainEventHandler[Agg, AggEvent], AggEventHandler2.type]()
      val found = container.resolve[Seq[AggregateDomainEventHandler[Agg, AggEvent]]]
      found.length === 2
    }
    "factory with singleton" >> {
      val container = new SimpleContainer(false, cl)
      container.registerFunc[Single](_ => new Single, InstanceScope.Singleton)
      val s1 = container.resolve[Single]
      val s2 = container.resolve[Single]
      s1 === s2
    }
    "factory with singleton in scopes" >> {
      val container = new SimpleContainer(false, cl)
      container.registerFunc[Single](_ => new Single, InstanceScope.Singleton)
      val c1 = container.createScope()
      val s1 = c1.resolve[Single]
      val s2 = c1.resolve[Single]
      s1 === s2
      val c2 = container.createScope()
      val s3 = c2.resolve[Single]
      val s4 = c2.resolve[Single]
      s3 === s4
      s1 === s3
    }
    "factory with context in scopes" >> {
      val container = new SimpleContainer(false, cl)
      container.registerFunc[Single](c => new Single, lifetime = InstanceScope.Context)
      val c1 = container.createScope()
      val s1 = c1.resolve[Single]
      val s2 = c1.resolve[Single]
      s1 === s2
      val c2 = container.createScope()
      val s3 = c2.resolve[Single]
      val s4 = c2.resolve[Single]
      s3 === s4
      s1 !== s3
    }
    "circular dependency" >> {
      val container = new SimpleContainer(false, cl)
      container.register[CircularTop](InstanceScope.Context)
      container.register[CircularDep](InstanceScope.Context)
      val tryResolve = Try { container.resolve[CircularTop] }
      tryResolve.isFailure === true
      tryResolve.failed.get.getMessage === "Unable to resolve: class net.revenj.CircularTop. Circular dependencies in signature detected\nResolution chain: class net.revenj.CircularTop -> class net.revenj.CircularDep -> class net.revenj.CircularTop"
    }
    "singleton container" >> {
      val container = new SimpleContainer(false, cl)
      container.register[UsesContainer](InstanceScope.Singleton)
      val scope = container.createScope()
      val uc = scope.resolve[UsesContainer]
      container === uc.container
      scope !== uc.container
    }
    "collection error is propagated" >> {
      val container = new SimpleContainer(false, cl)
      container.register[ErrorInCollection]()
      val tryCol = Try { container.resolve[Array[ErrorInCollection]] }
      tryCol.isFailure === true
      tryCol.failed.get.asInstanceOf[ReflectiveOperationException].getMessage == "Unable to resolve class net.revenj.ErrorInCollection. Error: naah"
    }
    "unbound func" >> {
      val container = new SimpleContainer(false, cl)
      val top = new CircularTop(new CircularDep(null))
      container.registerInstance(classOf[CircularTop], () => top)
      val resolved = container.resolve[CircularTop]
      top === resolved
    }
    "register as" >> {
      val container: Container = new SimpleContainer(false, cl)
      container.registerAs[Service, ServiceImpl](InstanceScope.Context)
      val result1 = container.tryResolve[Service]
      result1.isSuccess === true
      val result2 = container.tryResolve[Service]
      result2 === result1
    }
    "same instance func" >> {
      val container: Container = new SimpleContainer(false, cl)
      container.register[ServiceImpl](InstanceScope.Context)
      container.registerFunc[Service](_.resolve[ServiceImpl], InstanceScope.Context)
      val result1 = container.resolve[Service]
      val result2 = container.resolve[ServiceImpl]
      result1 === result2
    }
    "same instance as context" >> {
      val container: Container = new SimpleContainer(false, cl)
      container.register[ServiceImpl](InstanceScope.Context)
      container.registerAs[Service, ServiceImpl](InstanceScope.Context)
      val result1 = container.resolve[Service]
      val result2 = container.resolve[ServiceImpl]
      val result3 = container.resolve[Service]
      val result4 = container.resolve[ServiceImpl]
      result1 !== result2
      result1 === result3
      result2 === result4
    }
    "same instance as context/transient" >> {
      val container: Container = new SimpleContainer(false, cl)
      container.register[ServiceImpl](InstanceScope.Transient)
      container.registerAs[Service, ServiceImpl](InstanceScope.Context)
      val result1 = container.resolve[Service]
      val result2 = container.resolve[ServiceImpl]
      val result3 = container.resolve[Service]
      val result4 = container.resolve[ServiceImpl]
      result1 !== result2
      result1 === result3
      result2 !== result4
    }
    "generic with any" >> {
      val container: Container = new SimpleContainer(false, cl)
      val g = new G[Any](1)
      container.registerInstance[G[Any]](g)
      val result = container.resolve[G[Any]]
      result.instance === 1
      g === result
    }
    "generics with type alias" >> {
      val container: Container = new SimpleContainer(false, cl)
      val generics = new Generics[Int]
      val g = generics.createSimple(5)
      container.registerInstance(g)
      val result = generics.resolveSimple(container)
      result.instance === 5
      g === result
    }
    "generics with type nested alias" >> {
      val container: Container = new SimpleContainer(false, cl)
      val generics = new Generics[Int]
      val cg = generics.createComplex(5, "a")
      container.registerInstance(cg)
      val resultA = generics.resolveComplex[String](container)
      val resultB = container.resolve[ComplexGenerics[String, G[Int]]]
      resultA === resultB
      resultA.instance1 === "a"
      resultA.instance2.instance === 5
      cg === resultA
    }
    "generics with missing type" >> {
      val container: Container = new SimpleContainer(false, cl)
      val generics: Generics[Int] = new Generics
      val g = generics.createSimple(5)
      container.registerInstance(g)
      val result = generics.resolveSimple(container)
      result.instance === 5
      g === result
    }
    "generics with unknown type" >> {
      val container: Container = new SimpleContainer(false, cl)
      val generics: Generics[_] = new Generics
      val g = new G[Int](5)
      container.registerInstance(g)
      val result = generics.resolveSimple(container)
      result.instance.asInstanceOf[Int] === 5
      g === result
    }
    "context and singleton mixing" >> {
      val container: Container = new SimpleContainer(false, cl)
      container.registerFunc[ServiceImpl](c => new ServiceImpl, lifetime = InstanceScope.Context)
      container.registerFunc[Service](c => c.resolve[ServiceImpl], lifetime = InstanceScope.Context)
      val topLevel = container.resolve[ServiceImpl]
      val scope = container.createScope()
      val scopeLevel1 = scope.resolve[ServiceImpl]
      topLevel !=== scopeLevel1
      val scopeLevel2 = scope.resolve[ServiceImpl]
      scopeLevel1 === scopeLevel2
    }
  }
}

class A(val b: B)

class B {
  B.counter += 1
}

object B {
  var counter: Int = 0
}

class C(val a: A, val d: Option[D])

class D

class G[T](val instance: T)

class ComplexGenerics[T1, T2](val instance2: T2, val instance1: T1)

class CtorRawGenerics(val generics: ComplexGenerics[A, B])

class CtorPartialGenerics[T](val generics: ComplexGenerics[T, B])

class Later(val aFunction: () => A) {
  def getA() = aFunction.apply()
}

class SelfReference(val self: () => SelfReference) {
  def getSelf() = self.apply()
}

class Single

class Handler1 extends DomainEventHandler[Single] {
  override def handle(domainEvent: Single): Unit = ()
}
class Handler2 extends DomainEventHandler[Single] {
  override def handle(domainEvent: Single): Unit = ()
}
class Handler3 extends DomainEventHandler[Function0[Single]] {
  override def handle(domainEvent: Function0[Single]): Unit = ()
}

class Agg extends AggregateRoot {
  def URI = ???
}
class AggEvent extends AggregateDomainEvent[Agg] {
  def URI = ???
  def queuedAt: OffsetDateTime = ???
  def processedAt: Option[OffsetDateTime] = None

  def apply(aggregate: Agg): Unit = ???
}
class AggEventHandler1 extends AggregateDomainEventHandler[Agg, AggEvent] {
  def handle(domainEvent: AggEvent, aggregate: Agg): Unit = ???
}
object AggEventHandler2 extends AggregateDomainEventHandler[Agg, AggEvent] {
  def handle(domainEvent: AggEvent, aggregate: Agg): Unit = ???
}

class CircularTop(val dep: CircularDep)
class CircularDep(val top: CircularTop)

class UsesContainer(val container: Container)

class ErrorInCollection {
  throw new RuntimeException("naah")
}

trait Service
class ServiceImpl extends Service

class Generics[T: universe.TypeTag] {
  def createSimple(value: T): G[T] = {
    new G[T](value)
  }
  def createComplex[T2](value1: T, value2: T2): ComplexGenerics[T2, G[T]] = {
    new ComplexGenerics(new G[T](value1), value2)
  }
  def resolveSimple(locator: ServiceLocator): G[T] = {
    locator.resolve[G[T]]
  }
  def resolveComplex[T3: universe.TypeTag](locator: ServiceLocator): ComplexGenerics[T3, G[T]] = {
    locator.resolve[ComplexGenerics[T3, G[T]]]
  }
}
