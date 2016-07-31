package net.revenj.patterns

import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag

trait DataContext {
  def find[T <: Identifiable : TypeTag](uris: Seq[String]): Future[IndexedSeq[T]]

  def find[T <: Identifiable : TypeTag](uri: String): Future[Option[T]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    find[T](Seq(uri)).map(_.headOption)
  }

  def search[T <: DataSource : TypeTag](filter: Option[Specification[T]] = None, limit: Option[Int] = None, offset: Option[Int] = None): Future[IndexedSeq[T]]
  def search[T <: DataSource : TypeTag](filter: Specification[T]): Future[IndexedSeq[T]] = {
    search(Some(filter), None, None)
  }

  def count[T <: DataSource : TypeTag](filter: Option[Specification[T]] = None): Future[Long]
  def count[T <: DataSource : TypeTag](filter: Specification[T]): Future[Long] = {
    count(Some(filter))
  }

  def exists[T <: DataSource : TypeTag](filter: Option[Specification[T]]): Future[Boolean]
  def exists[T <: DataSource : TypeTag](filter: Specification[T]): Future[Boolean] = {
    exists(Some(filter))
  }

  def create[T <: AggregateRoot : TypeTag](aggregates: Seq[T]): Future[Unit]

  def create[T <: AggregateRoot : TypeTag](aggregate: T): Future[Unit] = {
    create(Seq(aggregate))
  }

  def updatePairs[T <: AggregateRoot : TypeTag](pairs: Seq[(T, T)]): Future[Unit]

  def update[T <: AggregateRoot : TypeTag](oldAggregate: T, newAggregate: T): Future[Unit] = {
    updatePairs(Seq((oldAggregate, newAggregate)))
  }

  def update[T <: AggregateRoot : TypeTag](aggregate: T): Future[Unit] = {
    updatePairs(Seq((null.asInstanceOf[T], aggregate)))
  }

  def update[T <: AggregateRoot : TypeTag](aggregates: Seq[T]): Future[Unit] = {
    updatePairs(aggregates.map(it => (null.asInstanceOf[T], it)))
  }

  def delete[T <: AggregateRoot : TypeTag](aggregates: Seq[T]): Future[Unit]

  def delete[T <: AggregateRoot : TypeTag](aggregate: T): Future[Unit] = {
    delete(Seq(aggregate))
  }

  def submit[T <: DomainEvent : TypeTag](events: Seq[T]): Future[Unit]

  def submit[T <: DomainEvent : TypeTag](event: T): Future[Unit] = {
    submit(Seq(event))
  }

  def populate[T : TypeTag](report: Report[T]): Future[T]
}
