package net.revenj.patterns

import scala.concurrent.Future

trait DataCache[T <: Identifiable] extends Repository[T] {
  def invalidate(uris: Seq[String]): Future[Unit]
}

trait DataSourceCache[T <: Identifiable] extends DataCache[T] with SearchableRepository[T] {
  def invalidateAll(): Future[Unit]
}

trait Cacheable {
  def getRelationships: Map[Class[_ <: Identifiable], Seq[String]]
}