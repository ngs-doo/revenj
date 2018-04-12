package net.revenj.security

import java.security.Principal

class UserPrincipal(name: String, isInRole: String => Boolean) extends Principal {

  def this(name: String, roles: Set[String]) {
    this(name, r => roles.contains(r))
  }

  override def getName: String = name

  def implies(role: String): Boolean = name == role || isInRole.apply(role)
}

