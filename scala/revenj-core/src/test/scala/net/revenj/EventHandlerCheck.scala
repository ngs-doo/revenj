package net.revenj

import net.revenj.patterns.DomainEventHandler
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class EventHandlerCheck extends Specification with ScalaCheck {
  "services loading" >> {
    "will load event handler" >> {
      val plugins = new ServicesPluginLoader(Thread.currentThread.getContextClassLoader)
      val handler1 = plugins.find[DomainEventHandler[TestMe]]
      val handler2 = plugins.find[DomainEventHandler[Array[TestMe]]]
      handler1.size === 1
      handler2.size === 1
    }
  }
}
