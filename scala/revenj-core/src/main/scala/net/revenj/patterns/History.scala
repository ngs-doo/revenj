package net.revenj.patterns

trait History[T <: ObjectHistory] extends Identifiable {
  def snapshots: Seq[Snapshot[T]]
}
