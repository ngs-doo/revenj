package org.revenj.patterns

import scala.reflect.ClassTag

trait ISerialization[TFormat] {
  def serialize[T](t: T): TFormat
  def deserialize[T](data: TFormat, locator: IServiceLocator)(implicit ev: ClassTag[T]): T
}
