package net.revenj

import net.revenj.patterns.{EventStoreAspect, ReportAspect}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class AspectCheck extends Specification with ScalaCheck {
  "services loading" >> {
    "will load report aspect" >> {
      val plugins = new ServicesPluginLoader(Thread.currentThread.getContextClassLoader)
      val aspects = plugins.find[ReportAspect[ReportMe.Result, ReportMe]]
      aspects.size === 1
    }
    "will load event aspect" >> {
      val plugins = new ServicesPluginLoader(Thread.currentThread.getContextClassLoader)
      val aspects = plugins.find[EventStoreAspect[TestMe]]
      aspects.size === 1
    }
  }
}
