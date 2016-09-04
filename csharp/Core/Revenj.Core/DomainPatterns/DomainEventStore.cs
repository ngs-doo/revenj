using System;
using System.Collections.Concurrent;
using System.Diagnostics.Contracts;

namespace Revenj.DomainPatterns
{
	internal class DomainEventStore : IDomainEventStore
	{
		private readonly IServiceProvider Locator;
		private readonly GlobalEventStore GlobalStore;
		private readonly ConcurrentDictionary<Type, Func<object, string>> EventStores = new ConcurrentDictionary<Type, Func<object, string>>(1, 17);

		public DomainEventStore(IServiceProvider locator, GlobalEventStore globalStore)
		{
			Contract.Requires(locator != null);
			Contract.Requires(globalStore != null);

			this.Locator = locator;
			this.GlobalStore = globalStore;
		}

		public string Submit<TEvent>(TEvent domainEvent)
			where TEvent : IDomainEvent
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

		public void Queue<TEvent>(TEvent domainEvent) where TEvent : IDomainEvent
		{
			GlobalStore.Queue(domainEvent);
		}
	}
}
