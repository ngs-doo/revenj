package net.revenj

import java.time.OffsetDateTime

import net.revenj.patterns.DomainEvent

class TestMe extends DomainEvent {
  override def queuedAt: OffsetDateTime = ???

  override def processedAt: Option[OffsetDateTime] = ???

  override def URI: String = ???
}
