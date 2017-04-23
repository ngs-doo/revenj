package net.revenj.extensibility

sealed trait InstanceScope

object InstanceScope {

  object Transient extends InstanceScope

  object Singleton extends InstanceScope

  object Context extends InstanceScope

}
