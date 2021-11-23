package net.revenj.database.postgres

import java.io.Closeable

import net.revenj.patterns.{EagerNotification, Identifiable}

class DatabaseNotificationQueue(
  notifications: EagerNotification,
  transactionConnection: Option[java.sql.Connection],
) extends Closeable {
  private lazy val queue = new java.util.concurrent.LinkedBlockingQueue[net.revenj.patterns.DataChangeNotification.NotifyInfo]()
  private val inQueueMode = transactionConnection.isDefined && !transactionConnection.get.getAutoCommit

  def notifyOrQueue[T <: Identifiable](connection: java.sql.Connection, name: String, insert: scala.collection.Seq[T], update: scala.collection.Seq[(T, T)], delete: scala.collection.Seq[T]): Unit = {
    if (inQueueMode && (transactionConnection.get eq connection)) {
      if (insert != null && insert.nonEmpty) queue.add(net.revenj.patterns.DataChangeNotification.NotifyInfo(name, net.revenj.patterns.DataChangeNotification.Operation.Insert, insert))
      if (update != null && update.nonEmpty) {
        queue.add(net.revenj.patterns.DataChangeNotification.NotifyInfo(name, net.revenj.patterns.DataChangeNotification.Operation.Update, update.map(_._2)))
        val changed = update.filter { case (l, r) => l != null && l.URI != r.URI }
        if (changed.nonEmpty) queue.add(net.revenj.patterns.DataChangeNotification.NotifyInfo(name, net.revenj.patterns.DataChangeNotification.Operation.Change, update.map(_._1)))
      }
      if (delete != null && delete.nonEmpty) queue.add(net.revenj.patterns.DataChangeNotification.NotifyInfo(name, net.revenj.patterns.DataChangeNotification.Operation.Delete, delete))
    } else if (connection.getAutoCommit) {
      if (insert != null && insert.nonEmpty) notifications.notify(net.revenj.patterns.DataChangeNotification.NotifyInfo(name, net.revenj.patterns.DataChangeNotification.Operation.Insert, insert))
      if (update != null && update.nonEmpty) {
        notifications.notify(net.revenj.patterns.DataChangeNotification.NotifyInfo(name, net.revenj.patterns.DataChangeNotification.Operation.Update, update.map(_._2)))
        val changed = update.filter { case (l, r) => l != null && l.URI != r.URI }
        if (changed.nonEmpty) notifications.notify(net.revenj.patterns.DataChangeNotification.NotifyInfo(name, net.revenj.patterns.DataChangeNotification.Operation.Change, update.map(_._1)))
      }
      if (delete != null && delete.nonEmpty) notifications.notify(net.revenj.patterns.DataChangeNotification.NotifyInfo(name, net.revenj.patterns.DataChangeNotification.Operation.Delete, delete))
    }
  }

  def notifyOrQueue[T <: Identifiable](connection: java.sql.Connection, name: String, insert: scala.collection.Seq[T]): Unit = {
    if (inQueueMode && (transactionConnection.get eq connection)) {
      if (insert != null && insert.nonEmpty) queue.add(net.revenj.patterns.DataChangeNotification.NotifyInfo(name, net.revenj.patterns.DataChangeNotification.Operation.Insert, insert))
    } else if (connection.getAutoCommit) {
      if (insert != null && insert.nonEmpty) notifications.notify(net.revenj.patterns.DataChangeNotification.NotifyInfo(name, net.revenj.patterns.DataChangeNotification.Operation.Insert, insert))
    }
  }

  def queueSize: Int = if (inQueueMode) queue.size else 0

  override def close(): Unit = {
    if (inQueueMode) {
      val connection = transactionConnection.get
      if (connection.isClosed || connection.getAutoCommit) {
        val iter = queue.iterator
        while (iter.hasNext) {
          notifications.notify(iter.next())
        }
        queue.clear()
      }
    }
  }
}
