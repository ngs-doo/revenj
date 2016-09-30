package net.revenj

import java.io.Closeable
import java.sql.{Connection, SQLException, Statement}
import java.util.Properties
import javax.sql.DataSource

import monix.reactive.Observable
import monix.reactive.subjects.PublishSubject
import net.revenj.database.postgres.PostgresReader
import net.revenj.database.postgres.converters.StringConverter
import net.revenj.extensibility.SystemState
import net.revenj.patterns.DataChangeNotification.{NotifyInfo, Operation, TrackInfo}
import net.revenj.patterns._
import org.postgresql.core.BaseConnection

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

private [revenj] class PostgresDatabaseNotification(
  dataSource: DataSource,
  domainModel: Option[DomainModel],
  properties: Properties,
  systemState: SystemState,
  locator: ServiceLocator) extends EagerNotification with Closeable {

  private val subject = PublishSubject[DataChangeNotification.NotifyInfo]()
  private val notificationStream = subject.map(identity)
  private val repositories = new TrieMap[Class[_], AnyRef]
  private val targets = new TrieMap[String, Set[Class[_]]]
  private var retryCount: Int = 0
  private var isClosed: Boolean = false

  private val maxTimeout = {
    val timeoutValue = properties.getProperty("revenj.notifications.timeout")
    if (timeoutValue != null) {
      try {
        timeoutValue.toInt
      } catch {
        case e: NumberFormatException => throw new RuntimeException("Error parsing notificationTimeout setting")
      }
    } else 1000
  }
  if ("disabled" == properties.getProperty("revenj.notifications.status")) isClosed = true
  else {
    setupPooling()
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = isClosed = true
    }))
  }

  private def setupPooling(): Unit = {
    retryCount += 1
    if (retryCount > 60) retryCount = 30
    try {
      var connection = if (dataSource != null) dataSource.getConnection else null
      val bc = connection match {
        case bc: BaseConnection => Some(bc)
        case _ =>
          var tmp: Option[BaseConnection] = None
          try {
            if (connection != null && connection.isWrapperFor(classOf[BaseConnection])) {
              tmp = Some(connection.unwrap(classOf[BaseConnection]))
            }
          } catch {
            case _: AbstractMethodError =>
          }
          if (tmp.isEmpty && properties.containsKey("revenj.jdbcUrl")) {
            val user: String = properties.getProperty("revenj.user")
            val pass: String = properties.getProperty("revenj.password")
            val driver = new org.postgresql.Driver
            val connProps = new Properties(properties)
            if (user != null && pass != null) {
              connProps.setProperty("user", user)
              connProps.setProperty("password", pass)
            }
            cleanupConnection(connection)
            connection = driver.connect(properties.getProperty("revenj.jdbcUrl"), connProps)
            connection match {
              case con: BaseConnection => Some(con)
              case _ => None
            }
          } else tmp
      }
      if (bc.isDefined) {
        val stmt = bc.get.createStatement
        stmt.execute("LISTEN events; LISTEN aggregate_roots; LISTEN migration;")
        retryCount = 0
        val pooling = new Pooling(bc.get, stmt)
        val thread = new Thread(pooling)
        thread.setDaemon(true)
        thread.start()
      } else cleanupConnection(connection)
    } catch {
      case ex: Exception =>
        try {
          Thread.sleep(1000 * retryCount)
        } catch {
          case e: InterruptedException =>
            e.printStackTrace()
        }
    }
  }

  private class Pooling private[revenj](connection: BaseConnection, ping: Statement) extends Runnable {
    def run(): Unit = {
      val reader = new PostgresReader
      var timeout = maxTimeout
      while (!isClosed) {
        try {
          ping.execute("")
          val messages = connection.getNotifications
          if (messages == null || messages.isEmpty) {
            try {
              Thread.sleep(timeout)
            } catch {
              case e: InterruptedException => e.printStackTrace()
            }
            if (timeout < maxTimeout) {
              timeout += 1
            }
          } else {
            timeout = 0
            messages foreach { n =>
              if ("events" == n.getName || "aggregate_roots" == n.getName) {
                val param = n.getParameter
                val ident = param.substring(0, param.indexOf(':'))
                val op = param.substring(ident.length + 1, param.indexOf(':', ident.length + 1))
                val values = param.substring(ident.length + op.length + 2)
                reader.process(values)
                StringConverter.parseCollectionOption(reader, 0) match {
                  case Some(ids) if ids.nonEmpty =>
                    op match {
                      case "Update" =>
                        subject.onNext(DataChangeNotification.NotifyInfo(ident, Operation.Update, ids))
                      case "Change" =>
                        subject.onNext(DataChangeNotification.NotifyInfo(ident, Operation.Change, ids))
                      case "Delete" =>
                        subject.onNext(DataChangeNotification.NotifyInfo(ident, Operation.Delete, ids))
                      case _ =>
                        subject.onNext(DataChangeNotification.NotifyInfo(ident, Operation.Insert, ids))
                    }
                  case _ =>
                }
              } else {
                if ("migration" == n.getName) {
                  systemState.notify(SystemState.SystemEvent("migration", n.getParameter))
                }
              }
            }
          }
        } catch {
          case _: Throwable =>
            try {
              Thread.sleep(1000)
            } catch {
              case e: InterruptedException => e.printStackTrace()
            }
            cleanupConnection(connection)
            setupPooling()
            return
        }
      }
      cleanupConnection(connection)
    }
  }

  private def getRepository[T <: Identifiable](manifest: Class[T]): Repository[T] = {
    repositories.getOrElseUpdate(manifest, {
      val clazz = Utils.makeGenericType(classOf[Repository[_]], manifest)
      locator.resolve(clazz)
    }).asInstanceOf[Repository[T]]
  }

  def notify(info: NotifyInfo): Unit = {
    subject.onNext(info)
  }

  def notifications: Observable[NotifyInfo] = {
    notificationStream
  }

  def track[T : ClassTag](implicit manifest: ClassTag[T]): Observable[TrackInfo[T]] = {
    track[T](manifest.runtimeClass.asInstanceOf[Class[T]])
  }

  private implicit val ctx = ExecutionContext.Implicits.global

  private [revenj] def track[T](manifest: Class[T]): Observable[TrackInfo[T]] = {
    notificationStream filter { it =>
      val set = targets.getOrElseUpdate(it.name, {
        val ns = new mutable.HashSet[Class[_]]()
        domainModel.get.find(it.name) match {
          case Some(dt) =>
            ns += dt
            ns ++= dt.getInterfaces
          case _ =>
        }
        ns.toSet
      })
      set.contains(manifest)
    } map { it =>
      lazy val dm = domainModel.getOrElse({ throw new RuntimeException("Unable to track if domain model is not provided")})
      lazy val sourceManifest = dm.find(it.name).asInstanceOf[Class[_ <: Identifiable]]
      TrackInfo[T](it.uris, () => getRepository(sourceManifest).find(it.uris).map(_.asInstanceOf[IndexedSeq[T]]) )
    }
  }

  private def cleanupConnection(connection: Connection): Unit = {
    try {
      if (connection != null && !connection.isClosed) {
        connection.close()
      }
    } catch {
      case e: SQLException => e.printStackTrace()
    }
  }

  def close(): Unit = {
    isClosed = true
  }

}
