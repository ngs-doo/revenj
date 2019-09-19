package net.revenj.patterns

import java.time.OffsetDateTime

trait History[T <: ObjectHistory] extends Identifiable {
  def snapshots: scala.collection.Seq[Snapshot[T]]
}

trait Snapshot[T <: ObjectHistory] extends Identifiable {
  def at: OffsetDateTime
  def action: String
  def value: T
}
