using System.Collections.Generic;

namespace Revenj.DomainPatterns
{
	//TODO: not implemented yet
	public interface IDomainEventQueue<TAggregate>
		where TAggregate : class, IAggregateRoot
	{
		Dictionary<string, IEnumerable<IDomainEvent<TAggregate>>> GetQueues(IEnumerable<string> uris);
	}
}
