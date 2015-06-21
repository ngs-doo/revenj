package org.revenj.patterns

import scala.reflect.runtime.universe.TypeTag

trait IServiceLocator {
  def resolve[T: TypeTag]: T
  def apply[T: TypeTag] = resolve
}
