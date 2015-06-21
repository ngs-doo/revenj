package org.revenj.patterns

trait IDomainEvent[T] {
  def apply(): Unit
}
