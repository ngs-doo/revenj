using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Server domain event signature.
	/// Domain event is meaningful business event which has happened and was logged.
	/// </summary>
	public interface IDomainEvent : IIdentifiable
	{
		/// <summary>
		/// Queue time
		/// </summary>
		DateTime QueuedAt { get; }
		/// <summary>
		/// When domain event was processed
		/// </summary>
		DateTime? ProcessedAt { get; }
	}
	/// <summary>
	/// Domain event which is bound to an aggregate.
	/// Async events will be replayed on the aggregate during aggregate reconstruction.
	/// </summary>
	/// <typeparam name="TAggregate">aggregate root type</typeparam>
	public interface IDomainEvent<TAggregate> : IDomainEvent
		where TAggregate : IAggregateRoot
	{
		/// <summary>
		/// Apply domain event on an aggregate root
		/// </summary>
		/// <param name="aggregate">aggregate root instance</param>
		void Apply(TAggregate aggregate);
	}
	/// <summary>
	/// Domain event-sourcing. 
	/// Processed domain events will be raised and available through the observable Events property.
	/// </summary>
	/// <typeparam name="TEvent">domain event type</typeparam>
	public interface IDomainEventSource<TEvent>
	{
		/// <summary>
		/// Processed domain events
		/// </summary>
		IObservable<TEvent> Events { get; }
	}
	/// <summary>
	/// Domain event-sourcing.
	/// Processed domain events will be raised and available for registered event types.
	/// </summary>
	public interface IDomainEventSource
	{
		/// <summary>
		/// Register for specific domain events.
		/// </summary>
		/// <typeparam name="TEvent">domain event type</typeparam>
		/// <returns>observable to processed domain events</returns>
		IObservable<TEvent> Track<TEvent>();
	}
	//TODO: IDomainEvent signature?!
	/// <summary>
	/// Domain event store.
	/// Events can only be submitted. Submitted events can't be changed.
	/// Async events can be marked as processed at a later time.
	/// </summary>
	/// <typeparam name="TEvent">domain event type</typeparam>
	public interface IDomainEventStore<TEvent> : IQueryableRepository<TEvent>, IRepository<TEvent>
		where TEvent : IDomainEvent
	{
		/// <summary>
		/// Submit domain events to the store.
		/// After submission event will get an unique identifier.
		/// </summary>
		/// <param name="events">domain events</param>
		/// <returns>event identifiers</returns>
		string[] Submit(IEnumerable<TEvent> events);
		/// <summary>
		/// Mark unprocessed events as processed.
		/// </summary>
		/// <param name="uris">event identifiers</param>
		void Mark(IEnumerable<string> uris);
		/// <summary>
		/// Get unprocessed events.
		/// </summary>
		/// <returns>unprocessed events</returns>
		IEnumerable<TEvent> GetQueue();
	}
	/// <summary>
	/// Domain event store.
	/// Events can only be submitted. Submitted events can't be changed.
	/// Async events can be marked as processed at a latter time.
	/// </summary>
	public interface IDomainEventStore
	{
		/// <summary>
		/// Submit domain event to the store.
		/// After submission event will get an unique identifier
		/// </summary>
		/// <typeparam name="TEvent">domain event type</typeparam>
		/// <param name="domainEvent">domain event</param>
		/// <returns>event identifier</returns>
		string Submit<TEvent>(TEvent domainEvent)
			where TEvent : IDomainEvent;
		/// <summary>
		/// Queue domain event for out-of-transaction submission to the store
		/// If error happens during submission (loss of power, DB connection problems, event will be lost)
		/// If current transaction is rolled back, event will still be persisted
		/// </summary>
		/// <typeparam name="TEvent">domain event type</typeparam>
		/// <param name="domainEvent">domain event</param>
		void Queue<TEvent>(TEvent domainEvent)
			where TEvent : IDomainEvent;
	}
	/// <summary>
	/// Handle domain event.
	/// When domain event is processed by the server, all domain event handlers are invoked to
	/// process it. If one domain event handler throws an exception, entire submission is canceled.
	/// If Event[] is used, collection of events can be processed at once.
	/// </summary>
	/// <typeparam name="TEvent">domain event type</typeparam>
	public interface IDomainEventHandler<TEvent>
	{
		/// <summary>
		/// Handle domain event submission.
		/// </summary>
		/// <param name="input">processing domain event(s)</param>
		void Handle(TEvent input);
	}

	/// <summary>
	/// Utility for domain events
	/// </summary>
	public static class DomainEventHelper
	{
		/// <summary>
		/// Submit single domain event to the store.
		/// Redirects call to the collection API.
		/// </summary>
		/// <typeparam name="TEvent">domain event type</typeparam>
		/// <param name="store">domain event store</param>
		/// <param name="domainEvent">raise domain event</param>
		/// <returns>event identifier</returns>
		public static string Submit<TEvent>(this IDomainEventStore<TEvent> store, TEvent domainEvent)
			where TEvent : IDomainEvent
		{
			Contract.Requires(store != null);
			Contract.Requires(domainEvent != null);

			var uris = store.Submit(new[] { domainEvent });
			if (uris != null && uris.Length == 1)
				return uris[0];
			return null;
		}
		/// <summary>
		/// Mark single domain event as processed.
		/// Redirects call to the collection API.
		/// </summary>
		/// <typeparam name="TEvent">domain event type</typeparam>
		/// <param name="store">domain event store</param>
		/// <param name="domainEvent">mark domain event as processed</param>
		public static void Mark<TEvent>(this IDomainEventStore<TEvent> store, TEvent domainEvent)
			where TEvent : IDomainEvent
		{
			Contract.Requires(store != null);
			Contract.Requires(domainEvent != null);
			Contract.Requires(domainEvent.ProcessedAt == null);

			store.Mark(new[] { domainEvent.URI });
		}
	}
}
