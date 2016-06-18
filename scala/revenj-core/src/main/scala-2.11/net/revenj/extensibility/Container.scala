package net.revenj.extensibility

import net.revenj.patterns.ServiceLocator

import scala.reflect.runtime.universe.TypeTag

trait Container extends ServiceLocator {
  def registerClass[T <: S, S](singleton: Boolean)(implicit tt: TypeTag[T], ts: TypeTag[S])

  def registerInstance[T: TypeTag](service: T, handleClose: Boolean)

  def registerFactory[T: TypeTag](factory: Container => T, singleton: Singleton)

  def createScope(): Container
}
