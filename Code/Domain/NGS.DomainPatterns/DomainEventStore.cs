using System;
using System.Collections.Concurrent;
using System.Diagnostics.Contracts;

namespace NGS.DomainPatterns
{
	public class DomainEventStore : IDomainEventStore
	{
		private readonly IServiceLocator Locator;
		private readonly ConcurrentDictionary<Type, Func<object, string>> EventStores = new ConcurrentDictionary<Type, Func<object, string>>(1, 17);

		public DomainEventStore(IServiceLocator locator)
		{
			Contract.Requires(locator != null);

			this.Locator = locator;
		}

		public string Submit<TEvent>(TEvent domainEvent)
		{
			Func<object, string> store;
			if (!EventStores.TryGetValue(typeof(TEvent), out store))
			{
				IDomainEventStore<TEvent> domainEventStore;
				try
				{
					domainEventStore = Locator.Resolve<IDomainEventStore<TEvent>>();
				}
				catch (Exception ex)
				{
					throw new ArgumentException(string.Format(@"Can't find domain event store for {0}.
Is {0} a domain event and does it have registered store", typeof(TEvent).FullName), ex);
				}
				store = it => domainEventStore.Submit((TEvent)it);
				EventStores.TryAdd(typeof(TEvent), store);
			}
			return store(domainEvent);
		}
	}
}
