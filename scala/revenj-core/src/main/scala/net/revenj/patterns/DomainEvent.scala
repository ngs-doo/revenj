package net.revenj.patterns

import java.time.OffsetDateTime

import scala.concurrent.Future

/** Domain event represents an meaningful business event that occurred in the system.
  * It is a message that back-end system knows how to process and that will
  * change the state of the system.
  *
  * They are preferred way of manipulating data instead of simple CUD
  * operations (create, update, delete).
  * Unlike {@link AggregateDomainEvent aggregate domain event} which is tied to a change in a single
  * {@link AggregateRoot aggregate root}, domain event should be used when an action will result
  * in modifications to multiple aggregates, external call (like sending an email)
  * or some other action.
  *
  * By default event will be applied immediately.
  * If {@code async} is used, event will be stored immediately but applied later.
  *
  * DomainEvent is defined in DSL with keyword {@code event}.
  *
  * {{{
  * module Todo {
  *   aggregate Task;
  *   event MarkDone {
  *     Task task;
  *   }
  * }
  * }}}
  */
trait DomainEvent extends Identifiable {

  def queuedAt: OffsetDateTime
  def processedAt: Option[OffsetDateTime]
}

trait DomainEventHandler[T] {
  def handle(domainEvent: T): Unit
}

trait DomainEventStore[T <: DomainEvent] extends Repository[T] with SearchableRepository[T] {

  def submit(events: Seq[T]): Future[IndexedSeq[String]]

  def submit(event: T): Future[String] = {
    implicit val global = scala.concurrent.ExecutionContext.Implicits.global
    require(event ne null, "null value provided for event")
    submit(Seq(event)).map(_.head)
  }

  def mark(uris: Seq[String]): Future[Unit]

  def mark(uri: String): Future[Unit] = {
    require(uri ne null, "null value provided for URI")
    require(uri.length != 0, "empty value provided for URI")
    mark(Seq(uri))
  }
}
