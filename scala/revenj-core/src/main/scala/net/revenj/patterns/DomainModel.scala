package net.revenj.patterns

trait DomainModel {
  def find(name: String): Option[Class[_]]
}
