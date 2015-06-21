package org.revenj.patterns

import java.util.EventListener
import scala.reflect.runtime.universe.TypeTag

object NotifyOperation extends Enumeration {
  type NotifyOperation = Value
  val Insert, Update, Change, Delete = Value

  def apply(from: String) = {
    if (from == "Insert") {
      Insert
    } else if (from == "Update") {
      Update
    } else if (from == "Change") {
      Change
    } else if (from == "Delete") {
      Delete
    } else {
      sys.error("Unknown from: " + from)
    }
  }
}

case class NotifyInfo(name: String, operation: NotifyOperation.Value, uri: IndexedSeq[String])

trait IDataChangeNotification {
  def listen[TT: TypeTag](handler: IndexedSeq[String] => Unit): EventListener
  def listen(handler: NotifyInfo => Unit): EventListener
  def unregister(obs: EventListener): Unit
}
