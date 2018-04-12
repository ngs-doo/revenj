package net.revenj.security

import net.revenj.patterns.DataSource

trait GlobalPermission extends DataSource {
  def name: String
  def isAllowed: Boolean
}

trait RolePermission extends DataSource {
  def name: String
  def roleID: String
  def isAllowed: Boolean
}
