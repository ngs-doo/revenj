package net.revenj.patterns

trait DataCache[T <: Identifiable] extends Repository[T] {
  def invalidate(uris: Seq[String]): Unit
}

trait DataSourceCache[T <: Identifiable] extends DataCache[T] with SearchableRepository[T] {
  def invalidateAll(): Unit
}

trait Cacheable {
  def calculateRelationships: Map[Class[_], Seq[String]]
}