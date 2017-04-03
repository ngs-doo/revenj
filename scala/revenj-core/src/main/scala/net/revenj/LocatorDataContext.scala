package net.revenj

import java.sql.{Connection, SQLException}

import net.revenj.extensibility.Container
import net.revenj.patterns._

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.reflect.runtime.universe._


private[revenj] class LocatorDataContext(
  scope: Container,
  manageConnection: Boolean,
  connection: Option[Connection],
  mirror: Mirror) extends UnitOfWork {

  private implicit val ctx = scala.concurrent.ExecutionContext.Implicits.global

  private lazy val searchRepositories = new TrieMap[Class[_], SearchableRepository[_ <: DataSource]]()
  private lazy val lookupRepositories = new TrieMap[Class[_], Repository[_ <: Identifiable]]()
  private lazy val persistableRepositories = new TrieMap[Class[_], PersistableRepository[_ <: AggregateRoot]]()
  private lazy val eventStores = new TrieMap[Class[_], DomainEventStore[_ <: DomainEvent]]()
  private var hasChanges: Boolean = false
  private var closed: Boolean = false
  private val changes = new mutable.HashSet[Future[Any]]

  private def getSearchableRepository[T <: DataSource : TypeTag](manifest: Class[_]): SearchableRepository[T] = {
    if (closed) throw new RuntimeException("Unit of work has been closed")
    searchRepositories.getOrElseUpdate(manifest, {
      persistableRepositories.get(manifest) match {
        case Some(repo) => repo.asInstanceOf[SearchableRepository[T]]
        case _ => scope.resolve[SearchableRepository[T]]
      }
    }).asInstanceOf[SearchableRepository[T]]
  }

  private def getLookupRepository[T <: Identifiable : TypeTag](manifest: Class[_]): Repository[T] = {
    if (closed) throw new RuntimeException("Unit of work has been closed")
    lookupRepositories.getOrElseUpdate(manifest, {
      persistableRepositories.get(manifest) match {
        case Some(repo) => repo.asInstanceOf[Repository[T]]
        case _ => scope.resolve[Repository[T]]
      }
    }).asInstanceOf[Repository[T]]
  }

  private def getPersistableRepository[T <: AggregateRoot : TypeTag](manifest: Class[_]): PersistableRepository[T] = {
    if (closed) throw new RuntimeException("Unit of work has been closed")
    persistableRepositories.getOrElseUpdate(manifest, {
      scope.resolve[PersistableRepository[T]]
    }).asInstanceOf[PersistableRepository[T]]
  }

  private def getEventStore[T <: DomainEvent : TypeTag](manifest: Class[_]): DomainEventStore[T] = {
    if (closed) throw new RuntimeException("Unit of work has been closed")
    eventStores.getOrElseUpdate(manifest, {
      scope.resolve[DomainEventStore[T]]
    }).asInstanceOf[DomainEventStore[T]]
  }

  private def findManifest[T: TypeTag]: Class[_] = {
    typeOf[T] match {
      case TypeRef(_, sym, args) => mirror.runtimeClass(sym.asClass)
      case _ => throw new ReflectiveOperationException("Unable to find class type for " + typeOf[T])
    }
  }

  def trackChange[T: TypeTag](result: Future[T]): Future[T] = {
    changes.synchronized {
      changes += result
    }
    result.onComplete(_ => changes.synchronized {
      changes -= result
    })
    result
  }

  override def find[T <: Identifiable : TypeTag](uri: String): Future[Option[T]] = {
    getLookupRepository[T](findManifest[T]).find(uri)
  }

  override def find[T <: Identifiable : TypeTag](uris: Seq[String]): Future[IndexedSeq[T]] = {
    getLookupRepository[T](findManifest[T]).find(uris)
  }

  override def search[T <: DataSource : TypeTag](filter: Option[Specification[T]] = None, limit: Option[Int] = None, offset: Option[Int] = None): Future[IndexedSeq[T]] = {
    getSearchableRepository[T](findManifest[T]).search(filter, limit, offset)
  }

  override def count[T <: DataSource : TypeTag](filter: Option[Specification[T]] = None): Future[Long] = {
    getSearchableRepository[T](findManifest[T]).count(filter)
  }

  override def exists[T <: DataSource : TypeTag](filter: Option[Specification[T]] = None): Future[Boolean] = {
    getSearchableRepository[T](findManifest[T]).exists(filter)
  }

  override def create[T <: AggregateRoot : TypeTag](aggregates: Seq[T]): Future[Unit] = {
    if (aggregates.isEmpty) {
      Future.successful(())
    } else {
      val result = getPersistableRepository[T](findManifest[T]).insert(aggregates).map(_ => ())
      hasChanges = true
      trackChange(result)
    }
  }

  override def updatePairs[T <: AggregateRoot : TypeTag](pairs: Seq[(T, T)]): Future[Unit] = {
    if (pairs.isEmpty) {
      Future.successful(())
    } else {
      val result = getPersistableRepository[T](findManifest[T]).persist(Nil, pairs, Nil).map(_ => ())
      hasChanges = true
      trackChange(result)
    }
  }

  override def delete[T <: AggregateRoot : TypeTag](aggregates: Seq[T]): Future[Unit] = {
    if (aggregates.isEmpty) {
      Future.successful(())
    } else {
      val result = getPersistableRepository[T](findManifest[T]).delete(aggregates)
      hasChanges = true
      trackChange(result)
    }
  }

  override def submit[T <: DomainEvent : TypeTag](events: Seq[T]): Future[Unit] = {
    if (events.isEmpty) {
      Future.successful(())
    } else {
      val result = getEventStore[T](findManifest[T]).submit(events).map(_ => ())
      hasChanges = true
      trackChange(result)
    }
  }

  override def populate[T: TypeTag](report: Report[T]): Future[T] = {
    report.populate(scope)
  }

  override def commit(): Future[Unit] = {
    Future
      .sequence[Any, Set](changes.toSet)
      .map(_ => {
        connection.get.commit()
        hasChanges = false
      })
  }

  override def rollback(): Future[Unit] = changes.synchronized {
    Future.sequence(
      changes.map {
        _.recover {
          case t: Throwable => ()
        }
      }
    ) map { _ =>
      connection.get.rollback()
      hasChanges = false
    }
  }

  override def close(): Unit = {
    connection
      .filter(!_.isClosed && manageConnection && !closed)
      .foreach(conn => {
        val waitForChanges = if (hasChanges) Future.successful(()) else rollback()
        val rollbackAndClose = waitForChanges.map(_ => {
          conn.setAutoCommit(true)
          conn.close()
        })
        Await.result(rollbackAndClose, Duration.Inf)
      })
    if (connection.isDefined) {
      scope.close()
    }
    closed = true
  }
}

private[revenj] object LocatorDataContext {

  import scala.reflect.runtime.universe._

  def asDataContext(container: Container, loader: ClassLoader): DataContext = {
    new LocatorDataContext(container, false, None, runtimeMirror(loader))
  }

  def asDataContext(connection: Connection, container: Container, loader: ClassLoader): DataContext = {
    val scope = container.createScope()
    scope.registerInstance(connection, handleClose = false)
    new LocatorDataContext(scope, false, Some(connection), runtimeMirror(loader))
  }

  def asUnitOfWork(container: Container, loader: ClassLoader): UnitOfWork = {
    val dataSource = container.resolve[javax.sql.DataSource]
    var connection: Connection = null
    try {
      connection = dataSource.getConnection
      connection.setAutoCommit(false)
    } catch {
      case e: SQLException =>
        try {
          if (connection != null) connection.close()
        } catch {
          case _: SQLException =>
        }
        connection = dataSource.getConnection
        connection.setAutoCommit(false)
    }
    val scope = container.createScope()
    scope.registerInstance(connection, handleClose = false)
    new LocatorDataContext(scope, true, Some(connection), runtimeMirror(loader))
  }
}
