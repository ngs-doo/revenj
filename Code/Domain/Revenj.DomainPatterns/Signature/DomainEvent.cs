using System;
using System.Collections.Generic;

namespace Revenj.DomainPatterns
{
	//TODO: not implemented yet
	public interface IDomainEventQueue<TAggregate>
		where TAggregate : class, IAggregateRoot
	{
		Dictionary<string, IEnumerable<IDomainEvent<TAggregate>>> GetQueues(IEnumerable<string> uris);
	}

	//TODO remove!?
	[Obsolete("will be removed")]
	public interface IDomainEventRepository<out TEvent> : IQueryableRepository<TEvent>, IRepository<TEvent>
		where TEvent : IDomainEvent
	{
		TEvent Submit(Action<TEvent> initialize);
	}
}
