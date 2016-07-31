package net.revenj

import net.revenj.patterns.DomainEventHandler
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class EventHandlerCheck extends Specification with ScalaCheck {
  "services loading" >> {
    "will load event handler" >> {
      val plugins = new ServicesPluginLoader(Thread.currentThread.getContextClassLoader)
      val handler = plugins.find[DomainEventHandler[TestMe]]
      handler.size === 1
    }
  }
}
