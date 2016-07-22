package net.revenj.patterns

import scala.concurrent.Future

trait DomainEventStore[T <: DomainEvent] extends Repository[T] with SearchableRepository[T] {

  def submit(events: Seq[T]): Future[IndexedSeq[String]]

  def submit(event: T): Future[String] = {
    import scala.concurrent.ExecutionContext.Implicits.global
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
