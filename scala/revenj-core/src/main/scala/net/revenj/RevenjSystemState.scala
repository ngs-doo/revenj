package net.revenj

import monix.reactive.Observable
import monix.reactive.subjects.PublishSubject
import net.revenj.extensibility.{Container, SystemState}

private[revenj] class RevenjSystemState extends SystemState {
  private var systemBooting = true
  private var systemReady = false
  private val changeSubject = PublishSubject[SystemState.SystemEvent]()
  private val startupSubject = PublishSubject[Container]()
  private val changeEvents = changeSubject.map(identity)
  private val startupEvents = startupSubject.map(identity)

  def isBooting: Boolean = systemBooting

  def isReady: Boolean = systemReady

  private[revenj] def started(container: Container): Unit = {
    systemBooting = false
    systemReady = true
    startupSubject.onNext(container)
  }

  def ready: Observable[Container] = startupEvents

  def change: Observable[SystemState.SystemEvent] = changeEvents

  def notify(value: SystemState.SystemEvent): Unit = {
    changeSubject.onNext(value)
  }
}
