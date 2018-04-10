package net.revenj

import net.revenj.patterns.ReportHandler
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class ReportHandlerCheck extends Specification with ScalaCheck {
  "services loading" >> {
    "will load report handler" >> {
      val plugins = new ServicesPluginLoader(Thread.currentThread.getContextClassLoader)
      val handlers = plugins.find[ReportHandler[ReportMe.Result, ReportMe]]
      handlers.size === 1
    }
  }
}
