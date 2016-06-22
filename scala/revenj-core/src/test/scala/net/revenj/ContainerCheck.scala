package net.revenj

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class ContainerCheck extends Specification with ScalaCheck {
  sequential

  "simple resolutions" >> {
    "types" >> {
      B.counter = 0
      val container = new SimpleContainer(false, ClassLoader.getSystemClassLoader)
      container.registerClass(classOf[A])
      val tryA1 = container.tryResolve[A]
      tryA1.isSuccess === false
      container.registerClass(classOf[B])
      val tryA2 = container.tryResolve[A]
      tryA2.isSuccess === true
      val a = tryA2.get
      a !== null
      1 === B.counter
    }
    "option" >> {
      B.counter = 0
      val container = new SimpleContainer(false, ClassLoader.getSystemClassLoader)
      container.registerClass(classOf[A])
      container.registerClass(classOf[B])
      container.registerClass(classOf[C])
      var c = container.resolve[C]
      c !== null
      1 === B.counter
      container.registerClass(classOf[D])
      c = container.resolve[C]
      c !== null
      2 === B.counter
    }
    "generics" >> {
      B.counter = 0
      val container = new SimpleContainer(false, ClassLoader.getSystemClassLoader)
      container.registerClass(classOf[A])
      container.registerClass(classOf[B])
      container.registerClass(classOf[G[_]])
      val g = container.resolve[G[A]]
      g !== null
      1 === B.counter
      g.instance.isInstanceOf[A] === true
      classOf[A] === g.instance.getClass
    }
    "complex generics" >> {
      B.counter = 0
      val container = new SimpleContainer(false, ClassLoader.getSystemClassLoader)
      container.registerClass(classOf[A])
      container.registerClass(classOf[B])
      container.registerClass(classOf[ComplexGenerics[_, _]])
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
      val container = new SimpleContainer(false, ClassLoader.getSystemClassLoader)
      container.registerClass(classOf[A])
      container.registerClass(classOf[B])
      container.registerClass(classOf[ComplexGenerics[_, _]])
      container.registerClass(classOf[CtorRawGenerics])
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
      val container = new SimpleContainer(false, ClassLoader.getSystemClassLoader)
      container.registerClass(classOf[A])
      container.registerClass(classOf[B])
      container.registerClass(classOf[ComplexGenerics[_, _]])
      container.registerClass(classOf[CtorPartialGenerics[_]])
      val cg = container.resolve[CtorPartialGenerics[A]]
      cg !== null
      2 === B.counter
      cg.generics.instance1.isInstanceOf[A] === true
      classOf[A] === cg.generics.instance1.getClass
      cg.generics.instance2.isInstanceOf[B] === true
      classOf[B] === cg.generics.instance2.getClass
    }
    "pass exception" >> {
      val container = new SimpleContainer(false, ClassLoader.getSystemClassLoader)
      container.registerFactory[ContainerCheck](c => throw new RuntimeException("test me now"))
      val cc = container.tryResolve[ContainerCheck]
      cc.isFailure === true
      cc.failed.get.getMessage.contains("test me now") === true
    }
    "resolve unknown" >> {
      B.counter = 0
      val container = new SimpleContainer(true, ClassLoader.getSystemClassLoader)
      val a = container.resolve[A]
      a !== null
      1 === B.counter
    }
    "resolve later" >> {
      val container = new SimpleContainer(true, ClassLoader.getSystemClassLoader)
      val l = container.resolve[Later]
      l !== null
      val a = l.getA()
      a !== null
    }
    "self reference singleton" >> {
      val container = new SimpleContainer(false, ClassLoader.getSystemClassLoader)
      container.registerClass(classOf[SelfReference], singleton = true)
      val sr = container.resolve[SelfReference]
      val sr2 = sr.getSelf()
      sr === sr2
    }
    "singleton in context" >> {
      val container = new SimpleContainer(false, ClassLoader.getSystemClassLoader)
      container.registerClass(classOf[Single], singleton = true)
      val nested = container.createScope()
      val s1 = nested.resolve[Single]
      val s2 = container.resolve[Single]
      s1 === s2
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