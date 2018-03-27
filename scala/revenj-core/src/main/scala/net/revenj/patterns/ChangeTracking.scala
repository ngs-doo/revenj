package net.revenj.patterns

trait ChangeTracking[T] extends Comparable[T] {
  def getOriginalValue: Option[T]
}
