using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Threading;
using System.Threading.Tasks;

namespace Revenj.DomainPatterns
{
	public interface IEvent { }
	/// <summary>
	/// Server command signature.
	/// Command is an external event which was received by the system.
	/// </summary>
	public interface ICommand : IEvent, IValidationErrors
	{
	}
	/// <summary>
	/// Server domain event signature.
	/// Domain event is meaningful business event which has happened and was logged.
	/// </summary>
	public interface IDomainEvent : IIdentifiable, IEvent
	{
		/// <summary>
		/// Queue time
		/// </summary>
		DateTime QueuedAt { get; }
	}
	public interface IAsyncEvent : IDomainEvent
	{
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
	/// Event-sourcing. 
	/// Processed events will be raised and available through the observable Events property.
	/// </summary>
	/// <typeparam name="TEvent">event type</typeparam>
	public interface IEventSource<TEvent> where TEvent : IEvent
	{
		/// <summary>
		/// Processed events
		/// </summary>
		IObservable<TEvent> Events { get; }
	}
	/// <summary>
	/// Event-sourcing.
	/// Processed events will be raised and available for registered event types.
	/// </summary>
	public interface IEventSource
	{
		/// <summary>
		/// Register for specific events.
		/// </summary>
		/// <typeparam name="TEvent">event type</typeparam>
		/// <returns>observable to processed events</returns>
		IObservable<TEvent> Track<TEvent>() where TEvent : IEvent;
	}
	public interface IEventStore<TEvent> where TEvent : IEvent
	{
		/// <summary>
		/// Submit events (commands or domain events) to the store.
		/// Submission will return unique identifier.
		/// </summary>
		/// <param name="events">events</param>
		/// <returns>submission identifiers</returns>
		string[] Submit(IEnumerable<TEvent> events);
		Task<string[]> SubmitAsync(IEnumerable<TEvent> events, CancellationToken cancellationToken);
	}
	//TODO: IDomainEvent signature?!
	/// <summary>
	/// Domain event store.
	/// Events can only be submitted. Submitted events can't be changed.
	/// Async events can be marked as processed at a later time.
	/// </summary>
	/// <typeparam name="TEvent">domain event type</typeparam>
	public interface IDomainEventStore<TEvent> : IEventStore<TEvent>,
		IQueryableRepository<TEvent>, IRepository<TEvent>,
		IQueryableRepositoryAsync<TEvent>, IRepositoryAsync<TEvent>
		where TEvent : IDomainEvent
	{
	}
	public interface IAsyncDomainEventStore<TEvent> : IDomainEventStore<TEvent>
		where TEvent : IAsyncEvent
	{
		/// <summary>
		/// Mark unprocessed events as processed.
		/// Use instance based mark
		/// </summary>
		/// <param name="events">events</param>
		void Mark(IEnumerable<TEvent> events);
		Task MarkAsync(IEnumerable<TEvent> events, CancellationToken cancellationToken);
		/// <summary>
		/// Get unprocessed events.
		/// </summary>
		/// <returns>unprocessed events</returns>
		IEnumerable<TEvent> GetQueue();
		Task<IEnumerable<TEvent>> GetQueueAsync(CancellationToken cancellationToken);
	}

	public interface ICommandLog<out T> : IIdentifiable, INestedValue<T>
		where T : ICommand
	{
		DateTime At { get; }
	}

	public interface ICommandStore<TCommand> : IEventStore<TCommand>,
		IRepository<ICommandLog<TCommand>>,// IQueryableRepository<ICommandLog<TCommand>> TODO later
		IRepositoryAsync<ICommandLog<TCommand>>//, IQueryableRepositoryAsync<ICommandLog<TCommand>> TODO later
		where TCommand : ICommand
	{
	}

	/// <summary>
	/// Event store (command or domain event)
	/// </summary>
	public interface IEventStore
	{
		/// <summary>
		/// Submit event to the store.
		/// </summary>
		/// <typeparam name="TEvent">event type</typeparam>
		/// <param name="instance">event</param>
		/// <returns>submission identifier</returns>
		string Submit<TEvent>(TEvent instance)
			where TEvent : IEvent;
		Task<string> SubmitAsync<TEvent>(TEvent instance, CancellationToken cancellationToken)
			where TEvent : IEvent;
		/// <summary>
		/// Queue domain event for out-of-transaction submission to the store
		/// If error happens during submission (loss of power, DB connection problems, event will be lost)
		/// If current transaction is rolled back, event will still be persisted
		/// </summary>
		/// <typeparam name="TEvent">domain event type</typeparam>
		/// <param name="domainEvent">domain event</param>
		void Queue<TEvent>(TEvent domainEvent)
			where TEvent : IEvent;
	}
	/// <summary>
	/// Handle event within the domain (command, aggregate event or an domain event).
	/// When event is processed by the server, all event handlers are invoked to
	/// process it. If one event handler throws an exception, entire submission is canceled.
	/// If Event[] is used, collection of events can be processed at once.
	/// 
	/// Ideally this signature should be called IEventHandler, 
	/// but for historical reasons it's currently named IDomainEventHandler
	/// </summary>
	/// <typeparam name="TEvent">event type</typeparam>
	public interface IDomainEventHandler<TEvent>
	{
		void Handle(TEvent instance);
	}

	public interface IEventStoreAspect<TEvent> where TEvent : IEvent
	{
		TEvent[] Before(TEvent[] events);
		void After(TEvent[] events);
	}

	/// <summary>
	/// Utility for events
	/// </summary>
	public static class EventHelper
	{
		/// <summary>
		/// Submit single event to the store.
		/// Redirects call to the collection API.
		/// </summary>
		/// <typeparam name="TEvent">event type</typeparam>
		/// <param name="store">event store</param>
		/// <param name="instance">raise event</param>
		/// <returns>event identifier</returns>
		public static string Submit<TEvent>(this IEventStore<TEvent> store, TEvent instance)
			where TEvent : IEvent
		{
			Contract.Requires(store != null);
			Contract.Requires(instance != null);

			var uris = store.Submit(new[] { instance });
			if (uris != null && uris.Length == 1)
				return uris[0];
			return null;
		}
		public static Task<string> SubmitAsync<TEvent>(this IEventStore<TEvent> store, TEvent instance, CancellationToken cancellationToken)
			where TEvent : IEvent
		{
			Contract.Requires(store != null);
			Contract.Requires(instance != null);

			return store.SubmitAsync(new[] { instance }, cancellationToken).ContinueWith<string>(res =>
			{
				var uris = res.Result;
				if (uris != null && uris.Length == 1)
					return uris[0];
				return null;
			}, TaskContinuationOptions.OnlyOnRanToCompletion);
		}
		/// <summary>
		/// Mark single domain event as processed.
		/// Redirects call to the collection API.
		/// </summary>
		/// <typeparam name="TEvent">domain event type</typeparam>
		/// <param name="store">domain event store</param>
		/// <param name="domainEvent">mark domain event as processed</param>
		public static void Mark<TEvent>(this IAsyncDomainEventStore<TEvent> store, TEvent domainEvent)
			where TEvent : IAsyncEvent
		{
			Contract.Requires(store != null);
			Contract.Requires(domainEvent != null);
			Contract.Requires(domainEvent.ProcessedAt == null);

			store.Mark(new[] { domainEvent });
		}
	}
}
