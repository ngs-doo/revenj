package net.revenj.extensibility

import monix.reactive.Observable

trait SystemState {
  def isBooting: Boolean

  def isReady: Boolean

  def ready: Observable[Container]

  def change: Observable[SystemState.SystemEvent]

  def notify(value: SystemState.SystemEvent): Unit
}

object SystemState {

  case class SystemEvent(id: String, detail: String)

}
