package net.revenj.patterns

trait AggregateDomainEvent[TAgg <: AggregateRoot] extends DomainEvent {
  def apply(aggregate: TAgg): Unit
}
