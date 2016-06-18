package net.revenj.patterns

import scala.reflect.runtime.universe.TypeTag

/** Service for resolving other services.
  * One locator per project should be used.
  *
  * When multiple projects are used, locator must be passed around
  * to resolve appropriate service.
  *
  * Custom classes can be resolved if their dependencies can be satisfied.
  */
trait ServiceLocator {
  /** Resolve a service registered in the locator.
    *
    * @tparam T Type info
    * @return registered implementation
    */
  def resolve[T: TypeTag]: T

  def tryResolve[T: TypeTag]: Option[T] = {
    try {
      Some(resolve[T])
    } catch {
      case _: Throwable => None
    }
  }
}
