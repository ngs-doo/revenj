package net.revenj.server

import net.revenj.extensibility.{Container, InstanceScope, SystemAspect}

class ServerSystemSetup extends SystemAspect {
  override def configure(container: Container): Unit = {
    container.registerAs[WireSerialization, RevenjSerialization](InstanceScope.Singleton)
    container.registerAs[ProcessingEngine, ProcessingEngine](InstanceScope.Singleton)
  }
}
