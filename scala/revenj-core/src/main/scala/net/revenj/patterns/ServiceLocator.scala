package net.revenj.patterns

import java.lang.reflect.Type

import net.revenj.Utils

import scala.reflect.runtime.universe.TypeTag
import scala.util.Try

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
  def resolve[T: TypeTag]: T = {
    val result = tryResolve[T]
    result.getOrElse(throw result.failed.get)
  }

  def tryResolve[T: TypeTag]: Try[T]

  private[revenj] def resolve(tpe: Type): Try[AnyRef]
  private[revenj] def resolve[T](container: Class[T], argument: Type, arguments: Type*): Try[T] = {
    resolve(Utils.makeGenericType(container, argument, arguments: _*)).map(_.asInstanceOf[T])
  }
}
