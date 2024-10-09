package net.revenj.security

import java.security.Principal

import scala.reflect.runtime.universe._

trait PermissionManager {
  def canAccess(identifier: String)(implicit user: Principal): Boolean

  def canAccess[T : TypeTag](implicit user: Principal): Boolean = canAccess(typeOf[T].toString)

  def invalidate(): Unit
}
