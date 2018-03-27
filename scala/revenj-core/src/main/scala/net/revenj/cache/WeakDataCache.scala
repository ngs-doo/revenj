package net.revenj.cache

import java.util.concurrent.Callable

import net.revenj.patterns.{DataCache, Identifiable, Repository}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.ref.WeakReference

class WeakDataCache[T <: Identifiable](lazyRepository: Callable[Repository[T]]) extends DataCache[T] {

  private lazy val repository = lazyRepository.call()
  private lazy val cache = WeakReference(new TrieMap[String, T]())

  override def invalidate(uris: Seq[String]): Unit = {
    cache.get match {
      case Some(wr) if uris != null && uris.nonEmpty =>
        uris.foreach(wr.remove)
      case _ =>
    }
  }

  private def findAndCache(uri: String) = {
    repository.find(uri) map { found =>
      if (found.isDefined) {
        val wr = cache()
        wr.put(uri, found.get)
      }
      found
    }
  }

  private def findAndCache(uris: Seq[String]) = {
    val wr = cache()
    repository.find(uris) map { found =>
      found.foreach ( it => wr.put(it.URI, it) )
      found
    }
  }

  override def find(uri: String): Future[Option[T]] = {
    cache.get match {
      case Some(wr) if uri != null =>
        wr.get(uri) match {
          case f@Some(_) => Future.successful(f)
          case _ => findAndCache(uri)
        }
      case _ => findAndCache(uri)
    }

  }
  override def find(uris: Seq[String]): Future[IndexedSeq[T]] = {
    cache.get match {
      case Some(wr) if uris != null && uris.nonEmpty =>
        val found = new ArrayBuffer[T]()
        val missing = uris.flatMap { uri =>
          wr.get(uri) match {
            case Some(f) =>
              found += f
              None
            case _ =>
              Some(uri)
          }
        }
        if (missing.nonEmpty) {
          findAndCache(missing) map { items =>
            found ++ items
          }
        } else {
          Future.successful(found)
        }
      case _ => findAndCache(uris)
    }
  }
}
