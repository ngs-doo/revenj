using System;
using System.Threading.Tasks;

namespace Revenj.DomainPatterns
{
	public interface IDomainEventStore
	{
		Task<string> Submit<TEvent>(TEvent domainEvent)
			where TEvent : class, IDomainEvent;
		Task<TAggregate> Submit<TEvent, TAggregate>(TEvent domainEvent, string uri)
			where TEvent : class, IDomainEvent<TAggregate>
			where TAggregate : class, IAggregateRoot;
	}

	public static class DomainEventStoreHelper
	{
		public static Task<TAggregate> Submit<TEvent, TAggregate>(this IDomainEventStore store, TEvent domainEvent, TAggregate aggregate)
			where TEvent : class, IDomainEvent<TAggregate>
			where TAggregate : class, IAggregateRoot
		{
			if (store == null)
				throw new ArgumentNullException("store can't be null");
			if (domainEvent == null)
				throw new ArgumentNullException("domainEvent can't be null");
			if (aggregate == null)
				throw new ArgumentNullException("aggregate can't be null");
			return store.Submit<TEvent, TAggregate>(domainEvent, aggregate.URI);
		}
	}
}
