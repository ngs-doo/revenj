package net.revenj

import java.lang.reflect.ParameterizedType

import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.{MulticastStrategy, Observable}
import monix.reactive.subjects.{ConcurrentSubject, PublishSubject}
import net.revenj.extensibility.Container
import net.revenj.patterns.DataChangeNotification._

import scala.concurrent.Future

private [revenj] class ChangeNotification[T](manifest: Class[T], notifications: PostgresDatabaseNotification, reactive: Option[Scheduler]) {
  private implicit val scheduler: Scheduler = reactive.getOrElse(monix.execution.Scheduler.Implicits.global)
  private val syncSubject = PublishSubject[TrackInfo[T]]()
  private val asyncSubject = ConcurrentSubject[TrackInfo[T]](MulticastStrategy.publish)(scheduler)
  private val syncSubscription = notifications.track[T](manifest).subscribe(syncSubject)
  private val asyncSubscription = notifications.track[T](manifest).subscribe(asyncSubject)
  private val lazySeqChanges = syncSubject.map(_.result)
  private val lazyFlattenChanges = syncSubject.flatMap { it =>
    val results = new Array[Function0[Future[T]]](it.uris.size)
    var i = 0
    while (i < results.length) {
      val cur = i
      results(i) = () => it.result().flatMap(r => Future.successful(r(cur)))
      i += 1
    }
    Observable(results:_*)
  }
  private val futureSeqChanges = asyncSubject.map(_.result())
  private val futureFlattenChanges = asyncSubject.flatMap { it =>
    val results = new Array[Future[T]](it.uris.size)
    var i = 0
    while (i < results.length) {
      val cur = i
      results(i) = it.result().flatMap(r => Future.successful(r(cur)))
      i += 1
    }
    Observable(results:_*)
  }
  private val taskSeqChanges = asyncSubject.map ( it => Task.defer({ Task.fromFuture(it.result()) }) )
  private val taskFlattenChanges = asyncSubject.flatMap { it =>
    val results = new Array[Task[T]](it.uris.size)
    var i = 0
    while (i < results.length) {
      val cur = i
      results(i) = Task.defer({Task.fromFuture(it.result().flatMap(r => Future.successful(r(cur))))})
      i += 1
    }
    Observable(results:_*)
  }

  def close(): Unit = {
    syncSubscription.cancel()
    asyncSubscription.cancel()
  }
}

private [revenj] object ChangeNotification {

  private def buildNotification(pt: ParameterizedType, notification: PostgresDatabaseNotification, reactive: Option[Scheduler]): Option[ChangeNotification[_]] = {
    val genArgs = pt.getActualTypeArguments
    if (genArgs.length == 1) {
      genArgs.head match {
        case clazz: Class[_] =>
          Some(new ChangeNotification(clazz, notification, reactive))
        case genCol: ParameterizedType if genCol.getActualTypeArguments.length == 1 && genCol.getRawType.isInstanceOf[Class[_]] =>
          (genCol.getRawType, genCol.getActualTypeArguments.head) match {
            case (seq: Class[_], elem: Class[_]) if classOf[scala.collection.Seq[_]].isAssignableFrom(seq) =>
              Some(new ChangeNotification(elem, notification, reactive))
            case _ =>
              None
          }
        case _ =>
          None
      }
    } else {
      None
    }
  }

  def registerContainer(container: Container, notification: PostgresDatabaseNotification): Unit = {
    lazy val reactive = container.tryResolve[Scheduler].toOption
    container.registerGenerics[Observable[_]]((locator, arguments) =>
      if (arguments.length == 1 && arguments.head.isInstanceOf[ParameterizedType]) {
        val pt = arguments.head.asInstanceOf[ParameterizedType]
        if (pt.getRawType == classOf[Future[_]]) {
          buildNotification(pt, notification, reactive) match {
            case Some(cn) if pt.getActualTypeArguments.head.isInstanceOf[Class[_]] =>
              cn.futureFlattenChanges
            case Some(cn) =>
              cn.futureSeqChanges
            case _ =>
              throw new RuntimeException("Invalid arguments for Observable[Future[T]]. Supported formats: Observable[Future[Seq[T]]] and Observable[Future[T]]")
          }
        } else if (pt.getRawType == classOf[Function0[_]]) {
          pt.getActualTypeArguments.head match {
            case npt: ParameterizedType =>
              buildNotification(npt, notification, reactive) match {
                case Some(cn) if npt.getActualTypeArguments.head.isInstanceOf[Class[_]] =>
                  cn.lazyFlattenChanges
                case Some(cn) =>
                  cn.lazySeqChanges
                case _ =>
                  throw new RuntimeException("Invalid arguments for Observable[Function0[T]]. Supported formats: Observable[Function0[Future[Seq[T]]]] and Observable[Function0[Future[T]]]")
              }
            case _ =>
              throw new RuntimeException("Invalid arguments for Observable[Function0[T]]. Supported formats: Observable[Function0[Future[Seq[T]]]] and Observable[Function0[Future[T]]]")
          }
        } else if (pt.getRawType == classOf[Task[_]]) {
          buildNotification(pt, notification, reactive) match {
            case Some(cn) if pt.getActualTypeArguments.head.isInstanceOf[Class[_]] =>
              cn.taskFlattenChanges
            case Some(cn) =>
              cn.taskSeqChanges
            case _ =>
              throw new RuntimeException("Invalid arguments for Observable[Task[T]]. Supported formats: Observable[Task[Seq[T]]] and Observable[Task[T]]")
          }
        } else {
          throw new RuntimeException("Invalid arguments for Observable[T]. Supported formats: Observable[Future[Seq[T]]], Observable[Task[T]] and Observable[Task[Seq[T]]]")
        }
      } else throw new RuntimeException("Invalid arguments for Observable[T]. Supported formats: Observable[Future[Seq[T]]], Observable[Future[T]], Observable[Function0[Future[Seq[T]]]], Observable[Function0[Future[T]]], Observable[Task[T]] and Observable[Task[Seq[T]]]")
    )
  }
}
