package net.revenj.extensibility

import java.lang.reflect.Type

import net.revenj.patterns.ServiceLocator

import scala.reflect.runtime.universe.TypeTag
import scala.util.Try

trait Container extends ServiceLocator with AutoCloseable {

  def registerClass[T: TypeTag](manifest: Class[T], singleton: Boolean = false): this.type

  def registerInstance[T: TypeTag](service: T, handleClose: Boolean = false): this.type

  def registerFactory[T: TypeTag](factory: Container => T, singleton: Boolean = false): this.type

  def resolve(tpe: Type): Try[AnyRef]

  def registerGenerics[T: TypeTag](factory: (Container, Array[Type]) => T): this.type

  def createScope(): Container
}
