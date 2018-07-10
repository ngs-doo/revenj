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
      val version = currentVersion
      n.operation match {
        case Operation.Insert | Operation.Update =>
          n match {
            case nw: NotifyWith[Seq[T]@unchecked] =>
              if (nw.info != null && nw.info.nonEmpty) {
                set(nw.info, version, forceSet = true)
              }
            case _ =>
              implicit val global = scala.concurrent.ExecutionContext.Implicits.global
              repository.find(n.uris).foreach { items => set(items, version, forceSet = false) }
          }
        case Operation.Delete | Operation.Change =>
          remove(n.uris, version, n.isInstanceOf[NotifyWith[_]])
      }
    }.subscribe()(monix.execution.Scheduler.Implicits.global)

  def set(instances: Seq[T]): Unit = set(instances, currentVersion, forceSet = true)
  private def set(instances: Seq[T], oldVersion: Int, forceSet: Boolean): Int = {
    if (instances != null && instances.nonEmpty) {
      val (shouldInvalidateAll, newVersion) = if (forceSet || oldVersion == currentVersion) {
        synchronized {
          val isInvalid = currentVersion != oldVersion
          lastChange = OffsetDateTime.now()
          currentVersion += 1
          instances.foreach(f => cache.put(f.URI, f))
          isInvalid -> currentVersion
        }
      } else {
        true -> currentVersion
      }
      if (shouldInvalidateAll) {
        invalidateAll()
        oldVersion
      } else {
        versionChangeSubject.onNext(currentVersion)
        newVersion
      }
    } else oldVersion
  }

  def remove(uris: Seq[String]): Unit = remove(uris, currentVersion, forceRemove = true)
  private def remove(uris: Seq[String], oldVersion: Int, forceRemove: Boolean): Int = {
    if (uris != null && uris.nonEmpty) {
      val (shouldInvalidateAll, newVersion) = if (forceRemove || oldVersion == currentVersion) {
        synchronized {
          val isInvalid = currentVersion != oldVersion
          lastChange = OffsetDateTime.now()
          currentVersion += 1
          uris.foreach(cache.remove)
          isInvalid -> currentVersion
        }
      } else {
        true -> currentVersion
      }
      if (shouldInvalidateAll) {
        invalidateAll()
        oldVersion
      } else {
        versionChangeSubject.onNext(currentVersion)
        newVersion
      }
    } else oldVersion
  }

  def get(uri: String): Option[T] = if (uri != null) cache.get(uri) else None
  def items: Seq[T] = cache.values.toIndexedSeq

  def version: Int = currentVersion
  def changedOn: OffsetDateTime = lastChange

  override def invalidate(uris: Seq[String]): Future[Unit] = {
    if (uris != null && uris.nonEmpty) {
      implicit val global = scala.concurrent.ExecutionContext.Implicits.global
      val version = currentVersion
      repository.find(uris).map { found =>
        val newVersion = set(found, version, forceSet = false)
        if (newVersion != version) {
          remove(uris.diff(found.map(_.URI)), newVersion, forceRemove = false)
        }
      }
    } else Future.failed(new RuntimeException("invalid uris provided"))
  }

  override def invalidateAll(): Future[Unit] = {
    implicit val global = scala.concurrent.ExecutionContext.Implicits.global
    val version = currentVersion
    repository.search().map { found =>
      val newVersion = set(found, version, forceSet = false)
      if (newVersion != version) {
        remove(cache.keys.toSeq.diff(found.map(_.URI)), newVersion, forceRemove = false)
      }
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
