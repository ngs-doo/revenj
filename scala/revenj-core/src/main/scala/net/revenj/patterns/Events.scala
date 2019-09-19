package net.revenj.patterns

import java.time.OffsetDateTime

import scala.concurrent.Future

trait Event

trait Command extends Event with ValidationErrors

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
trait DomainEvent extends Event with Identifiable {

  def queuedAt: OffsetDateTime
}

trait AsyncEvent extends DomainEvent {

  def processedAt: Option[OffsetDateTime]
}

trait CommandLog[T <: Command] extends Identifiable with NestedValue[T] {
  def at: OffsetDateTime
}

trait AggregateDomainEvent[TAgg <: AggregateRoot] extends DomainEvent {
  def apply(aggregate: TAgg): Unit
}

trait DomainEventHandler[T] {
  def handle(domainEvent: T): Unit
}

trait AggregateDomainEventHandler[TAgg <: AggregateRoot, TDE <: AggregateDomainEvent[TAgg]] {
  def handle(domainEvent: TDE, aggregate: TAgg): Unit
}

trait EventStore[T <: Event] {

  def submit(events: scala.collection.Seq[T]): Future[scala.collection.IndexedSeq[String]]

  def submit(event: T): Future[String] = {
    implicit val global = scala.concurrent.ExecutionContext.Implicits.global
    require(event ne null, "null value provided for event")
    submit(Seq(event)).map(_.head)
  }
}

trait DomainEventStore[T <: DomainEvent] extends EventStore[T] with Repository[T] with SearchableRepository[T] {
}

trait AsyncDomainEventStore[T <: DomainEvent] extends DomainEventStore[T] {

  def mark(events: scala.collection.Seq[T]): Future[Unit]

  def mark(event: T): Future[Unit] = {
    require(event ne null, "null value provided for event")
    mark(Seq(event))
  }
}

trait EventStoreAspect[T <: Event] {
  def before(events: scala.collection.Seq[T]): scala.collection.Seq[T] = events
  def after(events: scala.collection.Seq[T]): Unit = {}
}
