package net.revenj.extensibility

import java.lang.reflect.Type

import net.revenj.patterns.ServiceLocator

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

trait Container extends ServiceLocator with AutoCloseable {

  private[revenj] def registerType[T](manifest: Type, implementation: Class[T], singleton: Boolean = false): this.type

  def register[T](singleton: Boolean = false)(implicit manifest: ClassTag[T]): this.type = {
    registerType(manifest.runtimeClass, manifest.runtimeClass, singleton)
  }

  @deprecated("will be removed in next version", "0.2.0")
  def registerClass[T](manifest: Class[T], singleton: Boolean = false): this.type = {
    registerType(manifest, manifest, singleton)
  }

  def registerAs[T, S <: T](singleton: Boolean = false)(implicit manifest: TypeTag[T], implementation: ClassTag[S]): this.type

  def registerInstance[T: TypeTag](service: T, handleClose: Boolean = false): this.type

  def registerFactory[T: TypeTag](factory: Container => T, singleton: Boolean = false): this.type

  def registerGenerics[T: TypeTag](factory: (Container, Array[Type]) => T): this.type

  def createScope(): Container
}
