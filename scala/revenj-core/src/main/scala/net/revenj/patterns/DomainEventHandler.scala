package net.revenj.patterns

trait DomainEventHandler[T] {
  def handle(domainEvent: T): Unit
}
