package net.revenj

import java.util.Properties

import net.revenj.security.{PermissionManager, RolePermission, UserPrincipal}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class PermissionCheck extends Specification with ScalaCheck {
  "static permissions" >> {
    "can check through permission manager" >> {
      val roles = Seq[RolePermission](
        RolePermissionClass("module.Object", "User", false),
        RolePermissionClass("module.Object", "Admin", true)
      )
      val props = new Properties()
      props.setProperty("revenj.notifications.status", "disabled")
      val container = Revenj.setup(null, props, None, None, Nil)
      container.registerInstance(roles)
      val permissions = container.resolve[PermissionManager]
      val user = new UserPrincipal("User", Set("User"))
      val admin = new UserPrincipal("Admin", Set("Admin"))
      false === permissions.canAccess("module.Object")(user)
      true === permissions.canAccess("module.Object")(admin)
    }
  }
}

case class RolePermissionClass(name: String, roleID: String, isAllowed: Boolean) extends RolePermission