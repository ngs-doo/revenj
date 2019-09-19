package net.revenj.patterns

trait ChangeTracking[T] extends Comparable[T] {
  def getOriginalValue: Option[T]
}

trait DependencyTracking {
  def getDependencies: scala.collection.Map[Class[_ <: Identifiable], scala.collection.Seq[Any]]
}