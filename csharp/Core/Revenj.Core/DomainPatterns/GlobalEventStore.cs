using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Threading;

namespace Revenj.DomainPatterns
{
	internal class GlobalEventStore : IDisposable
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Server");

		private readonly IServiceProvider Locator;
		private readonly ConcurrentDictionary<Type, Action<List<IEvent>>> EventStores =
			new ConcurrentDictionary<Type, Action<List<IEvent>>>(1, 17);
		private readonly BlockingCollection<EventInfo> EventQueue = new BlockingCollection<EventInfo>(new ConcurrentQueue<EventInfo>());
		private bool IsDisposed;
		private readonly Thread Loop;

		struct EventInfo
		{
			public readonly Type Type;
			public readonly IEvent Event;

			public EventInfo(Type type, IEvent domainEvent)
			{
				this.Type = type;
				this.Event = domainEvent;
			}
		}

		public GlobalEventStore(IServiceProvider locator)
		{
			Contract.Requires(locator != null);

			this.Locator = locator;
			AppDomain.CurrentDomain.ProcessExit += (s, ea) => { IsDisposed = true; EventQueue.Add(new EventInfo(null, null)); };
			AppDomain.CurrentDomain.DomainUnload += (s, ea) => { IsDisposed = true; EventQueue.Add(new EventInfo(null, null)); };
			Loop = new Thread(WaitForEvents);
			Loop.Start();
		}

		public void Queue<T>(T domainEvent) where T : IEvent
		{
			var info = new EventInfo(typeof(T), domainEvent);
			EventQueue.Add(info);
		}

		private static Func<IServiceProvider, Action<List<IEvent>>> ResolveMethod = ResolveAndSetupStore<IEvent>;

		private static Action<List<IEvent>> ResolveAndSetupStore<TEvent>(IServiceProvider locator)
			where TEvent : IEvent
		{
			try
			{
				var store = locator.Resolve<IEventStore<TEvent>>();
				return events => store.Submit(events.Cast<TEvent>());
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(
					TraceEventType.Error,
					5503,
					"Failed to resolve event store for: {0}. Queued event will not be submitted. Error: {1}",
					typeof(TEvent).FullName,
					ex);
				return _ => { };
			}
		}

		private Action<List<IEvent>> ResolveStore(Type type)
		{
			Action<List<IEvent>> store;
			if (!EventStores.TryGetValue(type, out store))
			{
				var eventMethod = ResolveMethod.Method.GetGenericMethodDefinition().MakeGenericMethod(type);
				store = (Action<List<IEvent>>)eventMethod.Invoke(null, new object[] { Locator });
				EventStores.TryAdd(type, store);
			}
			return store;
		}

		private void WaitForEvents(object o)
		{
			var bulk = new List<IEvent>(1000);
			Type lastType = null;
			var info = default(EventInfo);
			while (!IsDisposed)
			{
				try
				{
					if (bulk.Count == 0)
					{
						info = EventQueue.Take();
						bulk.Add(info.Event);
					}
					lastType = info.Type;
					if (lastType == null)
						break;
					int i = 0;
					while (i++ < 1000 && EventQueue.Count > 0)
					{
						info = EventQueue.Take();
						if (info.Type != lastType)
							break;
						bulk.Add(info.Event);
					}
					var action = ResolveStore(lastType);
					try { action(bulk); }
					finally { bulk.Clear(); }
					if (info.Type != lastType)
						bulk.Add(info.Event);
				}
				catch (Exception ex)
				{
					TraceSource.TraceEvent(
						TraceEventType.Error,
						5505,
						"Error during event processing: {0}",
						ex);
				}
			}
		}

		public void Dispose()
		{
			IsDisposed = true;
			try
			{
				if (Loop.IsAlive)
					Loop.Abort();
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(
					TraceEventType.Error,
					5508,
					"Error during event loop closing: {0}",
					ex);
			}
		}
	}
}
