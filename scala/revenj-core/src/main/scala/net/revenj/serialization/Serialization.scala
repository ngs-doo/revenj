package net.revenj.serialization

import java.lang.reflect.Type

import net.revenj.Utils

import scala.reflect.runtime.universe.TypeTag
import scala.util.Try

trait Serialization[TFormat] {
  def serialize[T: TypeTag](value: T): Try[TFormat]
  private[revenj] def serializeRuntime(value: Any, manifest: Type): Try[TFormat]
  private[revenj] def serializeRuntime(value: Any): Try[TFormat] = {
    serializeRuntime(value, value.getClass)
  }
  def deserialize[T: TypeTag](input: TFormat): Try[T]
  private[revenj] def deserializeRuntime[T](input: TFormat, manifest: Type): Try[T]
  private[revenj] def deserializeRuntime[T](input: TFormat, container: Class[T], argument: Type, arguments: Type*): Try[T] = {
    deserializeRuntime[T](input, Utils.makeGenericType(container, argument, arguments: _*))
  }
}
