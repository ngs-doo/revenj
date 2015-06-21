package org.revenj.patterns

import java.util.EventListener
import scala.collection.mutable.ArrayBuffer
import org.pgscala._
import org.postgresql._
import java.lang.Throwable
import scala.collection.mutable.HashMap
import scala.reflect.runtime.universe._
import org.joda.time.DateTime
import org.slf4j.Logger

class PostgresNotification (
    private val pgFactory: PGSessionFactory
  , private val logger: Logger
  ) extends IDataChangeNotification {

  private val typedListeners = new HashMap[String, ArrayBuffer[TypedListener]]()
  private val genericListeners = new ArrayBuffer[GenericListener]()

  def setUpNotification() {
    val conn = {
      pgFactory.ds.getConnection() match {
        case pg: PGConnection =>
          pg
        case _ =>
          sys.error("PG connection!")
      }
    }

    val stmt = conn.createStatement()
    stmt.execute("LISTEN events; LISTEN aggregate_roots;")
    stmt.close()

    val pooling = new Pooling(conn)
    new Thread(pooling).start()
  }

  setUpNotification()

  case class TypedListener(raise: IndexedSeq[String] => Unit) extends EventListener
  case class GenericListener(raise: NotifyInfo => Unit) extends EventListener

  class Pooling(conn: java.sql.Connection with PGConnection) extends Runnable {

    override def run() {
      var running = true
      while (running) {
        try {
          val stmt = conn.createStatement()
          stmt.execute(";")
          stmt.close()
          conn.getNotifications() match {
            case notifications: Array[PGNotification] =>
              logger.info("NOTIFICATIONS RECEIVED: " + notifications.size + " @ " + DateTime.now)

              notifications foreach { n =>
                val param = n.getParameter()
                logger.debug("Param: {}", param)

                val ident = param.substring(0, param.indexOf(':'))
                logger.trace("  Ident: {}", ident)

                val op = param.substring(ident.length + 1, param.indexOf(':', ident.length + 1))
                logger.trace("  Op: {}", op)

                val values = param.substring(ident.length + op.length + 2)
                val ids = postgres.Utils.parseIndexedSeq(values, identity)
                logger.trace("  Values: {}", ids)

                val lst = typedListeners.get(ident).getOrElse(ArrayBuffer.empty)
                lst foreach { l => l.raise(ids) }
                val ni = NotifyInfo(ident, NotifyOperation(op), ids)
                genericListeners foreach { g => g.raise(ni) }
              }

            case _ =>
              logger.trace("NO NEW NOTIFICATIONS: " + DateTime.now)
          }
        } catch {
          case ex: Throwable =>
            logger.error("Exception was thrown: ", ex)
            running = false
            Thread.sleep(1000)
            setUpNotification()
        }

        Thread.sleep(500)
      }
    }
  }

  def listen[TT](ids: IndexedSeq[String] => Unit)(implicit ev: TypeTag[TT]): EventListener = {
    ev.tpe match {
      case TypeRef(_, tpe, _) =>
        val clazz = ev.mirror.runtimeClass(ev.tpe)
        val name = clazz.getName()
        //TODO better matching (events inside aggregates)
        val target = name.substring(name.substring(0, name.lastIndexOf('.')).lastIndexOf('.') + 1)
        val l = new TypedListener(ids)
        val buffer = typedListeners.get(target).getOrElse({
          val nb = ArrayBuffer[TypedListener]()
          typedListeners += target -> nb
          nb
        })
        buffer += l
        l
      case _ =>
        sys.error("Unknown type")
    }
  }

  def listen(handler: NotifyInfo => Unit): EventListener = {
    val l = GenericListener(handler)
    genericListeners += l
    l
  }

  def unregister(l: EventListener) {
    l match {
      case tl: TypedListener =>
        typedListeners.values foreach { lst =>
          lst -= tl
        }
      case gl: GenericListener =>
        genericListeners -= gl
      case _ =>
    }
  }
}
