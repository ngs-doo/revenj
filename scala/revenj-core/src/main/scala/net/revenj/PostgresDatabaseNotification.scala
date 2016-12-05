package net.revenj

import java.io.{Closeable, IOException}
import java.sql.{Connection, SQLException, Statement}
import java.util.Properties
import javax.sql.DataSource

import monix.execution.Cancelable
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import monix.reactive.subjects.PublishSubject
import net.revenj.database.postgres.{ConnectionFactory, PostgresReader}
import net.revenj.database.postgres.converters.StringConverter
import net.revenj.extensibility.SystemState
import net.revenj.patterns.DataChangeNotification.{NotifyInfo, Operation, Source, TrackInfo}
import net.revenj.patterns._
import org.postgresql.PGNotification
import org.postgresql.core.{BaseConnection, Notification, PGStream}
import org.postgresql.util.HostSpec

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
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
  private var retryCount = 0
  private var isClosed = false
  private var currentStream: Option[PGStream] = None

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
  if ("disabled" == properties.getProperty("revenj.notifications.status")) {
    isClosed = true
  } else if ("pooling" == properties.getProperty("revenj.notifications.type")) {
    setupPooling()
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = isClosed = true
    }))
  } else {
    setupListening()
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
        stmt.execute("LISTEN events; LISTEN aggregate_roots; LISTEN migration; LISTEN revenj")
        retryCount = 0
        val pooling = new Pooling(bc.get, stmt)
        val thread = new Thread(pooling)
        thread.setDaemon(true)
        thread.start()
      } else cleanupConnection(connection)
    } catch {
      case ex: Exception =>
        try {
          systemState.notify(SystemState.SystemEvent("notification", s"issue: ${ex.getMessage}"))
          Thread.sleep(1000L * retryCount)
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
      systemState.notify(SystemState.SystemEvent("notification", "started"))
      var threadAlive = true
      while (threadAlive && !isClosed) {
        try {
          ping.execute("")
          val messages = connection.getNotifications
          if (messages == null || messages.isEmpty) {
            try {
              Thread.sleep(timeout.toLong)
            } catch {
              case e: InterruptedException =>
                threadAlive = false
                e.printStackTrace()
            }
            if (timeout < maxTimeout) {
              timeout += 1
            }
          } else {
            timeout = 0
            messages foreach { n => processNotification(reader, n) }
          }
        } catch {
          case ex: Throwable =>
            threadAlive = false
            try {
              systemState.notify(SystemState.SystemEvent("notification", s"error: ${ex.getMessage}"))
              Thread.sleep(1000)
            } catch {
              case e: InterruptedException => e.printStackTrace()
            }
            cleanupConnection(connection)
            if (!isClosed) {
              setupPooling()
            }
        }
      }
      if (threadAlive) {
        cleanupConnection(connection)
      }
    }
  }

  def processNotification(reader: PostgresReader, n: PGNotification): Any = {
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
              subject.onNext(DataChangeNotification.NotifyInfo(ident, Operation.Update, Source.Database, ids))
            case "Change" =>
              subject.onNext(DataChangeNotification.NotifyInfo(ident, Operation.Change, Source.Database, ids))
            case "Delete" =>
              subject.onNext(DataChangeNotification.NotifyInfo(ident, Operation.Delete, Source.Database, ids))
            case _ =>
              subject.onNext(DataChangeNotification.NotifyInfo(ident, Operation.Insert, Source.Database, ids))
          }
        case _ =>
      }
    } else {
      systemState.notify(SystemState.SystemEvent(n.getName, n.getParameter))
    }
  }

  private def setupListening(): Unit = {
    retryCount += 1
    if (retryCount > 60) retryCount = 30
    val jdbcUrl = properties.getProperty("revenj.jdbcUrl")
    if (jdbcUrl == null || jdbcUrl.isEmpty) {
      throw new RuntimeException("""Unable to read revenj.jdbcUrl from properties. Listening notification is not supported without it.
Either disable notifications (revenj.notifications.status=disabled), change it to pooling (revenj.notifications.type=pooling) or provide revenj.jdbcUrl to properties.""")
    }
    val pgUrl =
      if (!jdbcUrl.startsWith("jdbc:postgresql:") && jdbcUrl.contains("://")) "jdbc:postgresql" + jdbcUrl.substring(jdbcUrl.indexOf("://"))
      else jdbcUrl
    val parsed = org.postgresql.Driver.parseURL(pgUrl, properties)
    if (parsed == null) throw new RuntimeException("Unable to parse revenj.jdbcUrl")
    try {
      val user = if (properties.containsKey("revenj.user")) properties.getProperty("revenj.user")
      else parsed.getProperty("user", "")
      val password = if (properties.containsKey("revenj.password")) properties.getProperty("revenj.password")
      else parsed.getProperty("password", "")
      val db = parsed.getProperty("PGDBNAME")
      val host = new HostSpec(parsed.getProperty("PGHOST").split(",")(0), parsed.getProperty("PGPORT").split(",")(0).toInt)
      val pgStream = ConnectionFactory.openConnection(host, user, password, db, properties)
      currentStream = Some(pgStream)
      retryCount = 0
      val listening = new Listening(pgStream)
      val thread = new Thread(listening)
      thread.setDaemon(true)
      thread.start()
    } catch {
      case ex: Exception =>
        try {
          systemState.notify(SystemState.SystemEvent("notification", s"issue: ${ex.getMessage}"))
          Thread.sleep(1000L * retryCount)
        } catch {
          case e: InterruptedException =>
            e.printStackTrace()
        }
    }
  }

  private class Listening (stream: PGStream) extends Runnable {
    private val command = "LISTEN events; LISTEN aggregate_roots; LISTEN migration; LISTEN revenj".getBytes("UTF-8")
    stream.sendChar('Q')
    stream.sendInteger4(command.length + 5)
    stream.send(command)
    stream.sendChar(0)
    stream.flush()
    receiveCommand(stream)
    receiveCommand(stream)
    receiveCommand(stream)
    receiveCommand(stream)
    if (stream.receiveChar != 'Z') throw new IOException("Unable to setup Postgres listener")
    private val num = stream.receiveInteger4
    if (num != 5) throw new IOException("unexpected length of ReadyForQuery packet")
    stream.receiveChar()

    private def receiveCommand(pgStream: PGStream): Unit = {
      pgStream.receiveChar
      val len = pgStream.receiveInteger4
      pgStream.skip(len - 4)
    }

    def run(): Unit = {
      val reader = new PostgresReader
      val pgStream = stream
      systemState.notify(SystemState.SystemEvent("notification", "started"))
      var threadAlive = true
      while (threadAlive && !isClosed) {
        try {
          if (!isClosed) {
            pgStream.receiveChar match {
              case 'A' =>
                pgStream.receiveInteger4
                val pidA = pgStream.receiveInteger4
                val msgA = pgStream.receiveString
                val paramA = pgStream.receiveString
                processNotification(reader, new Notification(msgA, pidA, paramA))
              case 'E' =>
                if (!isClosed) {
                  val e_len = pgStream.receiveInteger4
                  val err = pgStream.receiveString(e_len - 4)
                  throw new IOException(err)
                }
              case x =>
                if (!isClosed) {
                  throw new IOException(s"Unexpected packet type $x")
                }
            }
          }
        } catch {
          case ex: Exception =>
            try {
              threadAlive = false
              currentStream = None
              systemState.notify(SystemState.SystemEvent("notification", s"error: ${ex.getMessage}"))
              pgStream.close()
              Thread.sleep(1000)
            } catch {
              case e: Exception => e.printStackTrace()
            }
            if (!isClosed) {
              setupListening()
            }
        }
      }
      if (threadAlive) {
        closeStream(pgStream)
      }
    }
  }

  private def getRepository[T <: Identifiable](manifest: Class[T]): Repository[T] = {
    repositories.getOrElseUpdate(manifest, {
      val clazz = Utils.makeGenericType(classOf[Repository[_]], manifest)
      locator.resolve(clazz).getOrElse(throw new RuntimeException("Unable to resolve Repository[" + manifest + "]"))
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

  class TrackObservable() extends Observable[NotifyInfo] {
    override def unsafeSubscribeFn(subscriber: Subscriber[NotifyInfo]): Cancelable = {
      subject.unsafeSubscribeFn(subscriber)
    }
  }

  private implicit val ctx = ExecutionContext.Implicits.global

  private [revenj] def track[T](manifest: Class[T]): Observable[TrackInfo[T]] = {
    val dm = domainModel.get
    val observable = new TrackObservable().filter { it =>
      val set = targets.getOrElseUpdate(it.name, {
        val ns = new mutable.HashSet[Class[_]]()
        dm.find(it.name) match {
          case Some(dt) =>
            ns += dt
            ns ++= dt.getInterfaces
          case _ =>
        }
        ns.toSet
      })
      set.contains(manifest)
    } map { it =>
      TrackInfo[T](it.uris, new LazyResult[T](it.name, dm, it.uris))
    }
    observable
  }

  private class LazyResult[T](name: String, dm: DomainModel, uris: Seq[String]) extends Function0[Future[IndexedSeq[T]]] {
    private var result: Option[Future[IndexedSeq[T]]] = None

    override def apply(): Future[IndexedSeq[T]] = result match {
      case Some(f) => f
      case _ =>
        val manifest = dm.find(name).getOrElse(throw new RuntimeException(s"Unable to find domain type: $name")).asInstanceOf[Class[_ <: Identifiable]]
        val repository = getRepository(manifest)
        val found = repository.find(uris).map(_.asInstanceOf[IndexedSeq[T]])
        result = Some(found)
        found
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

  private def closeStream(stream: PGStream): Unit = {
    try {
      stream.close()
    } catch {
      case _: Throwable =>
    }
    currentStream = None
  }

  def close(): Unit = {
    isClosed = true
    currentStream match {
      case Some(stream) => closeStream(stream)
      case _ =>
    }
  }

}
