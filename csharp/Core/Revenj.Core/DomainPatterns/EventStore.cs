using System;
using System.Collections.Concurrent;
using System.Diagnostics.Contracts;
using System.Threading;
using System.Threading.Tasks;

namespace Revenj.DomainPatterns
{
	internal class EventStore : IEventStore
	{
		private readonly IServiceProvider Locator;
		private readonly GlobalEventStore GlobalStore;
		private readonly ConcurrentDictionary<Type, object> EventStores = new ConcurrentDictionary<Type, object>(1, 17);

		public EventStore(IServiceProvider locator, GlobalEventStore globalStore)
		{
			Contract.Requires(locator != null);
			Contract.Requires(globalStore != null);

			this.Locator = locator;
			this.GlobalStore = globalStore;
		}

		private IEventStore<TEvent> FindStore<TEvent>() where TEvent : IEvent
		{
			object store;
			if (EventStores.TryGetValue(typeof(TEvent), out store))
				return (IEventStore<TEvent>)store;
			IEventStore<TEvent> domainEventStore;
			try
			{
				domainEventStore = Locator.Resolve<IEventStore<TEvent>>();
			}
			catch (Exception ex)
			{
				throw new ArgumentException(string.Format(@"Can't find event store for {0}.
Is {0} an event and does it have registered store", typeof(TEvent).FullName), ex);
			}
			EventStores.TryAdd(typeof(TEvent), domainEventStore);
			return domainEventStore;
		}

		public string Submit<TEvent>(TEvent instance) where TEvent : IEvent
		{
			return FindStore<TEvent>().Submit(instance);
		}

		public Task<string> SubmitAsync<TEvent>(TEvent instance, CancellationToken cancellationToken) where TEvent : IEvent
		{
			return FindStore<TEvent>().SubmitAsync(new[] { instance }, cancellationToken)
				.ContinueWith(res => res.Result[0], TaskContinuationOptions.OnlyOnRanToCompletion);
		}

		public void Queue<TEvent>(TEvent instance) where TEvent : IEvent
		{
			GlobalStore.Queue(instance);
		}
	}
}
