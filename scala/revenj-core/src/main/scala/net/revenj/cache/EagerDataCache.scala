package net.revenj.cache

import java.time.OffsetDateTime

import monix.reactive.Observable
import monix.reactive.subjects.PublishSubject
import net.revenj.patterns.DataChangeNotification.{NotifyWith, Operation}
import net.revenj.patterns._

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

class EagerDataCache[T <: Identifiable](
  val name: String,
  repository: Repository[T] with SearchableRepository[T],
  dataChanges: DataChangeNotification
) extends DataSourceCache[T] with AutoCloseable {

  protected val cache = new TrieMap[String, T]()
  private var currentVersion = 0
  private val versionChangeSubject = PublishSubject[Int]()
  private var lastChange = OffsetDateTime.now()
  def changes: Observable[Int] = versionChangeSubject.map(identity)

  invalidateAll()

  private val subscription = dataChanges.notifications
    .filter(_.name == name)
    .map { n =>
      n.operation match {
          //TODO: change to URI should result in old values being removed
        case Operation.Insert | Operation.Change | Operation.Update =>
          n match {
            case nw: NotifyWith[Seq[T]@unchecked] =>
              set(nw.info)
            case _ =>
              implicit val global = scala.concurrent.ExecutionContext.Implicits.global
              repository.find(n.uris).foreach(set)
          }
        case Operation.Delete =>
          remove(n.uris)
      }
    }.subscribe()(monix.execution.Scheduler.Implicits.global)

  def set(instances: Seq[T]): Unit = {
    if (instances != null && instances.nonEmpty) {
      synchronized {
        lastChange = OffsetDateTime.now()
        currentVersion += 1
        instances.foreach(f => cache.put(f.URI, f))
      }
      versionChangeSubject.onNext(currentVersion)
    }
  }
  def get(uri: String): Option[T] = if (uri != null) cache.get(uri) else None
  def items: Seq[T] = cache.values.toIndexedSeq
  def remove(uris: Seq[String]): Unit = {
    if (uris != null && uris.nonEmpty) {
      synchronized {
        lastChange = OffsetDateTime.now()
        currentVersion += 1
        uris.foreach(cache.remove)
      }
      versionChangeSubject.onNext(currentVersion)
    }
  }

  def version: Int = currentVersion
  def changedOn: OffsetDateTime = lastChange

  override def invalidate(uris: Seq[String]): Future[Unit] = {
    if (uris != null && uris.nonEmpty) {
      implicit val global = scala.concurrent.ExecutionContext.Implicits.global
      repository.find(uris).map { found =>
        set(found)
        remove(uris.diff(found.map(_.URI)))
      }
    } else Future.failed(new RuntimeException("invalid uris provided"))
  }

  override def invalidateAll(): Future[Unit] = {
    implicit val global = scala.concurrent.ExecutionContext.Implicits.global
    repository.search().map { found =>
      set(found)
      remove(cache.keys.toSeq.diff(found.map(_.URI)))
    }
  }

  override def find(uri: String): Future[Option[T]] = {
    Future.successful(get(uri))
  }

  override def find(uris: Seq[String]): Future[IndexedSeq[T]] = {
    if (uris != null) {
      Future.successful(uris.flatMap(get).toIndexedSeq)
    } else {
      Future.failed(new RuntimeException("invalid uris provided"))
    }
  }

  override def search(specification: Option[Specification[T]], limit: Option[Int], offset: Option[Int]): Future[IndexedSeq[T]] = {
    val items = cache.values.toIndexedSeq
    val filtered = if (specification.isDefined) items.filter(specification.get) else items
    val skipped = if (offset.isDefined) filtered.drop(offset.get) else filtered
    Future.successful(if (limit.isDefined) skipped.take(limit.get) else skipped)
  }

  override def count(specification: Option[Specification[T]]): Future[Long] = {
    val items = cache.values.toIndexedSeq
    Future.successful(if (specification.isDefined) items.count(specification.get).toLong else items.size.toLong)
  }

  override def exists(specification: Option[Specification[T]]): Future[Boolean] = {
    val items = cache.values.toIndexedSeq
    Future.successful(if (specification.isDefined) items.exists(specification.get) else items.nonEmpty)
  }

  def close(): Unit = {
    subscription.cancel()
  }
}
