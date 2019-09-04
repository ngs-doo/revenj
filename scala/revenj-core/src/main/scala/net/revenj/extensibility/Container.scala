package net.revenj.extensibility

import java.lang.reflect.Type

import net.revenj.extensibility.InstanceScope._
import net.revenj.patterns.ServiceLocator

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

trait Container extends ServiceLocator with AutoCloseable {

  @deprecated("avoid using this unbounded method. Prefer bounded ones instead", "0.6.0")
  def registerType[T](manifest: Type, implementation: Class[T], lifetime: InstanceScope = Transient): this.type

  @deprecated("avoid using this unbounded method. Prefer bounded ones instead", "0.7.2")
  def registerInstance[T](manifest: Type, factory: () => T): this.type

  @deprecated("avoid using this unbounded method. Prefer bounded ones instead", "0.9.5")
  def registerInstanceAs[T](manifest: Type, instance: T): this.type

  @deprecated("use register with InstanceScope instead", "0.5.3")
  def register[T](singleton: Boolean)(implicit manifest: ClassTag[T]): this.type = {
    registerType(manifest.runtimeClass, manifest.runtimeClass, if (singleton) Singleton else Transient)
  }

  def register[T](lifetime: InstanceScope = Transient)(implicit manifest: ClassTag[T]): this.type = {
    registerType(manifest.runtimeClass, manifest.runtimeClass, lifetime)
  }

  @deprecated("use registerAs with InstanceScope instead", "0.5.3")
  def registerAs[T, S <: T](singleton: Boolean)(implicit manifest: TypeTag[T], implementation: ClassTag[S]): this.type = {
    registerAs[T, S](if (singleton) Singleton else Transient)
  }

  def registerAs[T, S <: T](lifetime: InstanceScope = Transient)(implicit manifest: TypeTag[T], implementation: ClassTag[S]): this.type

  def registerInstance[T: TypeTag](service: T, handleClose: Boolean = false): this.type

  @deprecated("use registerFunc with InstanceScope instead", "0.5.3")
  def registerFactory[T: TypeTag](factory: Container => T, singleton: Boolean): this.type = {
    registerFunc[T](factory, if (singleton) Singleton else Transient)
  }

  def registerFunc[T: TypeTag](factory: Container => T, lifetime: InstanceScope = Transient): this.type

  @deprecated("avoid using this unbounded method. Prefer bounded ones instead", "0.9.6")
  def registerFuncAs[T](manifest: Type, factory: Container => T, lifetime: InstanceScope = Transient): this.type

  def registerGenerics[T: TypeTag](factory: (Container, Array[Type]) => T, lifetime: InstanceScope = Transient): this.type

  def createScope(): Container

  @deprecated("avoid using this unbounded method. Prefer bounded ones instead", "0.9.6")
  def resolveClass[T](manifest: Class[T]): T
}