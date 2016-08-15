package net.revenj.patterns

import scala.concurrent.Future

/** Service for doing CRUD operations.
  * It can be used for applying changes on {@link AggregateRoot aggregate root}
  * to the remote server.
  *
  * @tparam T type of {@link AggregateRoot aggregate root}
  */
trait PersistableRepository[T <: AggregateRoot]
    extends Repository[T] {

  /** Apply local changes to the persistent store.
    *
    * @param inserts new aggregate roots
    * @param updates pairs for updating old aggregate to new state
    * @param deletes aggregate roots which will be deleted
    * @return       future uris of newly created aggregates
    */
  def persist(
    inserts: Seq[T],
    updates: Seq[(T, T)],
    deletes: Seq[T]): Future[IndexedSeq[String]]

  /** Bulk insert.
    * Create multiple new {@link AggregateRoot aggregates}.
    *
    * @param inserts new aggregate roots
    * @return       future uris of created aggregate roots
    */
  def insert(inserts: Seq[T]): Future[IndexedSeq[String]] = {
    require(inserts ne null, "null value provided for inserts")
    persist(inserts, Seq.empty, Seq.empty)
  }

  /** Insert a single {@link AggregateRoot aggregate}.
    *
    * @param insert new aggregate root
    * @return       future uri of created aggregate root
    */
  def insert(insert: T): Future[String] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    require(insert ne null, "null value provided for insert")
    persist(Seq(insert), Seq.empty, Seq.empty).map(_.head)
  }

  /** Bulk update.
    * Changing state of multiple {@link AggregateRoot aggregates}.
    *
    * @param updates sequence of aggregate roots to update
    * @return       future for error checking
    */
  def update(updates: Seq[T]): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    require(updates ne null, "null value provided for updates")
    persist(Seq.empty, updates.map(it => (null.asInstanceOf[T], it)), Seq.empty).map(_ => ())
  }

  /** Changing state of an aggregate root.
    *
    * @param update aggregate root to update
    * @return       future for error checking
    */
  def update(update: T): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    require(update ne null, "null value provided for update")
    persist(Seq.empty, Seq((null.asInstanceOf[T], update)), Seq.empty).map(_ => ())
  }

  /** Changing state of an aggregate root.
    *
    * @param old old version of aggregate root
    * @param current current version of aggregate root
    * @return       future for error checking
    */
  def update(old: T, current: T): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    require(old ne null, "null value provided for old")
    require(current ne null, "null value provided for current")
    persist(Seq.empty, Seq((old, current)), Seq.empty).map(_ => ())
  }

  /** Bulk delete.
    * Remote multiple {@link AggregateRoot aggregates}.
    *
    * @param deletes aggregate roots to delete
    * @return       future for error checking
    */
  def delete(deletes: Seq[T]): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    require(deletes ne null, "null value provided for deletes")
    persist(Seq.empty, Seq.empty, deletes).map(_ => ())
  }

  /** Deleting an {@link AggregateRoot aggregate}.
    *
    * @param delete aggregate root to delete
    * @return       future for error checking
    */
  def delete(delete: T): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    require(delete ne null, "null value provided for delete")
    persist(Seq.empty, Seq.empty, Seq(delete)).map(_ => ())
  }
}
