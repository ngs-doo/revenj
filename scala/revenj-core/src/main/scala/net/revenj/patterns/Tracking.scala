package net.revenj.patterns

trait ChangeTracking[T] extends Comparable[T] {
  def getOriginalValue: Option[T]
}

trait DependencyTracking {
  def getDependencies: Map[Class[_ <: Identifiable], Seq[Any]]
}