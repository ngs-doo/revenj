package net.revenj.server

import java.lang.reflect.Type

import net.revenj.serialization.Serialization

import scala.util.{Success, Try}
import scala.reflect.runtime.universe.TypeTag

class PassThroughSerialization extends Serialization[Any] {
  override def serialize[T: TypeTag](value: T): Try[Any] = Success(value)

  override private[revenj] def serializeRuntime(value: Any, manifest: Type): Try[Any] = Success(value)

  override def deserialize[T: TypeTag](input: Any): Try[T] = Success(input.asInstanceOf[T])

  override private[revenj] def deserializeRuntime[T](input: Any, manifest: Type): Try[T] = Success(input.asInstanceOf[T])
}
