package net.revenj

import java.security.Principal
import java.util.Properties

import monix.reactive.Observable
import net.revenj.patterns.{SearchableRepository, ServiceLocator}
import net.revenj.security.{GlobalPermission, PermissionManager, RolePermission, UserPrincipal}

private[revenj] class RevenjPermissionManager(
  properties: Properties,
  staticGlobalPermissions: Option[Seq[GlobalPermission]],
  staticRolePermissions: Option[Seq[RolePermission]]/*,
  globalChanges: Observable[Function0[GlobalPermission]],
  rolesChanges: Observable[Function0[RolePermission]],
  globalRepository: Function0[Option[SearchableRepository[GlobalPermission]]],
  rolesRepository: Function0[Option[SearchableRepository[RolePermission]]]*/
) extends PermissionManager {

  def this(properties: Properties, locator: ServiceLocator) {
    this(properties,
      locator.resolve[Option[Seq[GlobalPermission]]],
      locator.resolve[Option[Seq[RolePermission]]]/*,
      locator.resolve[Observable[Function0[GlobalPermission]]],
      locator.resolve[Observable[Function0[RolePermission]]],
      locator.resolve[Function0[Option[SearchableRepository[GlobalPermission]]]],
      locator.resolve[Function0[Option[SearchableRepository[RolePermission]]]]*/
    )
  }

  private val defaultPermissions = {
    val permissions = properties.getProperty("revenj.permissions")
    if (permissions != null && permissions.length > 0) {
      if (!permissions.equalsIgnoreCase("open") && !permissions.equalsIgnoreCase("closed")) throw new RuntimeException("Invalid revenj.permission settings found: '" + permissions + "'.\n" + "Allowed values are open and closed")
      "open" == permissions
    } else false
  }

  private var globalPermissions = staticGlobalPermissions.getOrElse(Nil).map(it => it.name -> it.isAllowed).toMap
  private var rolePermissions = staticRolePermissions.getOrElse(Nil).groupBy(_.name)

  private var cache = Map.empty[String, Boolean]
/*
  import monix.execution.Scheduler.Implicits.global

  globalChanges.doOnNext { _ =>
    globalRepository() match {
      case Some(rep) =>
        import scala.concurrent.ExecutionContext.Implicits.global
        rep.search().map { found =>
          globalPermissions = found.map(it => it.name -> it.isAllowed).toMap
        }
      case _ =>
    }
  }.subscribe()

  rolesChanges.doOnNext { _ =>
    rolesRepository() match {
      case Some(rep) =>
        import scala.concurrent.ExecutionContext.Implicits.global
        rep.search().map { found =>
          rolePermissions = found.groupBy(_.name)
        }
    }
  }.subscribe()
*/
  private def checkOpen(parts: Array[String], len: Int): Boolean = {
    if (len < 0) defaultPermissions
    else {
      val name = parts.take(len).mkString(".")
      globalPermissions.get(name) match {
        case Some(found) => found
        case _ => checkOpen(parts, len - 1)
      }
    }
  }

  private def implies(principal: Principal, role: String) = {
    principal match {
      case up: UserPrincipal => up.implies(role)
      case _ => principal.getName == role
    }
  }

  override def canAccess(identifier: String)(implicit user: Principal): Boolean = {
    val target = if (identifier != null) identifier else ""
    val id = if (user != null) user.getName + ":" + target else target
    cache.get(id) match {
      case Some(exists) => exists
      case _ =>
        val parts = target.split("\\.")
        var isAllowed = checkOpen(parts, parts.length)
        if (user != null) {
          var i = parts.length
          while (i >= 0) {
            val subName = parts.take(i).mkString(".")
            rolePermissions.get(subName) match {
              case Some(permissions) =>
                permissions.find(it => implies(user, it.name)) match {
                  case Some(found) =>
                    isAllowed = found.isAllowed
                    i = 0
                  case _ =>
                }
              case _ =>
            }
            i -= 1
          }
        }
        cache = cache + (id -> isAllowed)
        isAllowed
    }
  }
}