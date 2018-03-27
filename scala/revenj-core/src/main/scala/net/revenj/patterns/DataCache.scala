package net.revenj.patterns

trait DataCache[T] extends Repository[T] {
  def invalidate(uris: Seq[String]): Unit
}
