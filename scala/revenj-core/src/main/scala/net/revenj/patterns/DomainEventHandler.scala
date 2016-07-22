package net.revenj.patterns

import scala.concurrent.Future

trait DomainEventHandler[T] {
  def handle(domainEvent: T): Future[Unit]
}
