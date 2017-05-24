package net.revenj.patterns

import java.time.OffsetDateTime

trait Snapshot[T <: ObjectHistory] extends Identifiable {
  def at: OffsetDateTime
  def action: String
  def value: T
}
