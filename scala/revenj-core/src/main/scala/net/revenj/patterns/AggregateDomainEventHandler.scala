package net.revenj.patterns

trait AggregateDomainEventHandler[TAgg <: AggregateRoot, TDE <: AggregateDomainEvent[TAgg]] {
  def handle(domainEvent: TDE, aggregate: TAgg): Unit
}
