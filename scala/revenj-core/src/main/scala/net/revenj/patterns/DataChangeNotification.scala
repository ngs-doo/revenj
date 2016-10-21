package net.revenj.patterns

import monix.reactive.Observable

import scala.concurrent.Future
import scala.reflect.ClassTag

trait DataChangeNotification {

  def notifications: Observable[DataChangeNotification.NotifyInfo]

  def track[T : ClassTag](implicit manifest: ClassTag[T]): Observable[DataChangeNotification.TrackInfo[T]]
}

object DataChangeNotification {
  sealed trait Operation
  object Operation {
    case object Insert extends Operation
    case object Update extends Operation
    case object Change extends Operation
    case object Delete extends Operation
  }

  sealed trait Source
  object Source {
    case object Database extends Source
    case object Local extends Source
  }

  case class NotifyInfo(name: String, operation: Operation, source: Source, uris: IndexedSeq[String])
  case class TrackInfo[T](uris: IndexedSeq[String], result: Function0[Future[IndexedSeq[T]]])

}
