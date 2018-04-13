package net.revenj.patterns

trait AggregateDomainEvent[TAgg <: AggregateRoot] extends DomainEvent {
  def apply(aggregate: TAgg): Unit
}

trait AggregateDomainEventHandler[TAgg <: AggregateRoot, TDE <: AggregateDomainEvent[TAgg]] {
  def handle(domainEvent: TDE, aggregate: TAgg): Unit
}