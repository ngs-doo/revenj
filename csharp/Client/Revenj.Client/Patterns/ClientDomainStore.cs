using System.Threading.Tasks;
using Revenj.DomainPatterns;

namespace Revenj
{
	internal class ClientDomainStore : IDomainEventStore
	{
		private readonly IDomainProxy DomainProxy;

		public ClientDomainStore(IDomainProxy domainProxy)
		{
			this.DomainProxy = domainProxy;
		}

		public Task<string> Submit<T>(T domainEvent)
			where T : class, IDomainEvent
		{
			return DomainProxy.Submit(domainEvent);
		}

		public Task<TAggregate> Submit<TEvent, TAggregate>(TEvent domainEvent, string uri)
			where TEvent : class, IDomainEvent<TAggregate>
			where TAggregate : class, IAggregateRoot
		{
			return DomainProxy.Submit<TEvent, TAggregate>(domainEvent, uri);
		}
	}
}
