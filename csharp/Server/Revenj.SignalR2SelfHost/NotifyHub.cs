using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNet.SignalR;
using Revenj.DomainPatterns;
using System.Diagnostics;

namespace Revenj.SignalR2SelfHost
{
	public class NotifyHub : Hub
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Server");

		private static bool IsRunning = true;
		internal static IDomainModel Model;
		internal static IDataChangeNotification ChangeNotification;
		private static readonly ConcurrentDictionary<Type, ConcurrentDictionary<string, IDisposable>> Listeners =
			new ConcurrentDictionary<Type, ConcurrentDictionary<string, IDisposable>>(1, 127);

		private static readonly ConcurrentDictionary<string, ConcurrentBag<Type>> Connections =
			new ConcurrentDictionary<string, ConcurrentBag<Type>>();

		private static readonly BlockingCollection<Action> Messages = new BlockingCollection<Action>(new ConcurrentQueue<Action>());

		static NotifyHub()
		{
			TraceSource.TraceEvent(TraceEventType.Start, 1101);
			var thread = new Thread(SendMessages);
			thread.Start();
		}

		internal static void Stop()
		{
			TraceSource.TraceEvent(TraceEventType.Stop, 1101);
			IsRunning = false;
		}

		private static void SendMessages()
		{
			do
			{
				try
				{
					var action = Messages.Take();
					action();
				}
				catch (Exception ex)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 1104, "{0}", ex);
					Console.Write(ex.Message);
				}
			} while (IsRunning || Messages.Count > 0);
		}

		public override Task OnDisconnected(bool stopCalled)
		{
			TraceSource.TraceEvent(TraceEventType.Verbose, 1102, "Disconnected: {0}", Context.ConnectionId);
			ConcurrentBag<Type> types;
			if (Connections.TryRemove(Context.ConnectionId, out types))
			{
				foreach (var t in types.ToArray())
				{
					ConcurrentDictionary<string, IDisposable> dict;
					IDisposable disp;
					if (Listeners.TryGetValue(t, out dict) && dict.TryRemove(Context.ConnectionId, out disp))
						disp.Dispose();
				}
			}
			return base.OnDisconnected(stopCalled);
		}

		public void Listen(string domainObject)
		{
			var cid = Context.ConnectionId;
			TraceSource.TraceEvent(TraceEventType.Verbose, 1111, "Listen request: {0} for {1}", cid, domainObject);
			if (!IsRunning)
			{
				Clients.Caller.Error("In shutdown. Try again later");
				return;
			}
			var found = Model.Find(domainObject);
			if (found == null
				|| !typeof(IAggregateRoot).IsAssignableFrom(found) && !typeof(IDomainEvent).IsAssignableFrom(found))
			{
				Clients.Caller.Error("Unknown object " + domainObject);
				return;
			}
			var rl = (IListener)Activator.CreateInstance(typeof(DomainObjectListen<>).MakeGenericType(found));
			if (rl.Register(cid, ids => NotifyDomainObjectChanges(cid, domainObject, ids)))
			{
				TraceSource.TraceEvent(TraceEventType.Verbose, 1111, "Registered: {0} for {1}", cid, domainObject);
				Clients.Caller.Success("Registered for " + domainObject);
			}
			else Clients.Caller.Error("Error registering for " + domainObject);
		}

		public void WatchSingle(string domainObject, string uri)
		{
			var cid = Context.ConnectionId;
			TraceSource.TraceEvent(TraceEventType.Verbose, 1112, "Watch single request: {0} for {1}", cid, domainObject);
			if (!IsRunning)
			{
				Clients.Caller.Error("In shutdown. Try again later");
				return;
			}
			var found = Model.Find(domainObject);
			if (found == null
				|| !typeof(IAggregateRoot).IsAssignableFrom(found) && !typeof(IDomainEvent).IsAssignableFrom(found))
			{
				Clients.Caller.Error("Unknown object " + domainObject);
				return;
			}
			var rl = (IListener)Activator.CreateInstance(typeof(DomainObjectListen<>).MakeGenericType(found));
			if (rl.Register(cid, ids => { if (ids.Contains(uri)) NotifySingleUriChange(cid); }))
			{
				TraceSource.TraceEvent(TraceEventType.Verbose, 1112, "Registered: {0} for {1}", cid, domainObject);
				Clients.Caller.Success("Registered for " + domainObject);
			}
			else
				Clients.Caller.Error("Error registering for " + domainObject);
		}

		public void WatchCollection(string domainObject, string[] uris)
		{
			var cid = Context.ConnectionId;
			TraceSource.TraceEvent(TraceEventType.Verbose, 1113, "Watch collection request: {0} for {1}", cid, domainObject);
			if (!IsRunning)
			{
				Clients.Caller.Error("In shutdown. Try again later");
				return;
			}
			var found = Model.Find(domainObject);
			if (found == null
				|| !typeof(IAggregateRoot).IsAssignableFrom(found) && !typeof(IDomainEvent).IsAssignableFrom(found))
			{
				Clients.Caller.Error("Unknown object " + domainObject);
				return;
			}
			var rl = (IListener)Activator.CreateInstance(typeof(DomainObjectListen<>).MakeGenericType(found));
			var set = new HashSet<string>(uris);
			if (rl.Register(cid, ids => { if (set.Overlaps(ids)) NotifyCollectionUriChange(cid, set.Intersect(uris).ToArray()); }))
			{
				TraceSource.TraceEvent(TraceEventType.Verbose, 1113, "Registered: {0} for {1}", cid, domainObject);
				Clients.Caller.Success("Registered for " + domainObject);
			}
			else
				Clients.Caller.Error("Error registering for " + domainObject);
		}

		public void WatchSpecification(string domainObject, string specification, string json)
		{
			var cid = Context.ConnectionId;
			TraceSource.TraceEvent(TraceEventType.Verbose, 1114, "Watch specification request: {0} for {1}", cid, domainObject);
			if (!IsRunning)
			{
				Clients.Caller.Error("In shutdown. Try again later");
				return;
			}
			var found = Model.Find(domainObject);
			if (found == null
				|| !typeof(IAggregateRoot).IsAssignableFrom(found) && !typeof(IDomainEvent).IsAssignableFrom(found))
			{
				Clients.Caller.Error("Unknown object " + domainObject);
				return;
			}
			var specType = Model.Find(domainObject + "+" + specification);
			if (specType == null || !typeof(ISpecification<>).MakeGenericType(found).IsAssignableFrom(specType))
			{
				Clients.Caller.Error("Unknown specification " + specification);
				return;
			}
			var rl = (IListener)Activator.CreateInstance(typeof(DomainObjectListen<>).MakeGenericType(found));
			if (rl.Register(cid, specType, json, id => NotifySpecificationMatch(cid, id)))
			{
				TraceSource.TraceEvent(TraceEventType.Verbose, 1114, "Registered: {0} for {1}", cid, domainObject);
				Clients.Caller.Success("Registered for " + specification + " in " + domainObject);
			}
			else
				Clients.Caller.Error("Error registering for " + domainObject);
		}

		public void UnListen(string domainObject)
		{
			var cid = Context.ConnectionId;
			TraceSource.TraceEvent(TraceEventType.Verbose, 1113, "Unlisten: {0} for {1}", cid, domainObject);
			var found = Model.Find(domainObject);
			if (found == null
				|| !typeof(IAggregateRoot).IsAssignableFrom(found) && !typeof(IDomainEvent).IsAssignableFrom(found))
			{
				Clients.Caller.Error("Unknown object " + domainObject);
				return;
			}
			ConcurrentDictionary<string, IDisposable> dict;
			if (!Listeners.TryGetValue(found, out dict))
			{
				Clients.Caller.Error("Error unregistering for " + domainObject);
				return;
			}
			IDisposable registration;
			if (dict.TryRemove(cid, out registration) && registration != null)
				registration.Dispose();
		}

		interface IListener
		{
			bool Register(string connectionId, Action<string[]> onChanged);
			bool Register(string connectionId, Type specificationType, string specificationJson, Action<string> onMatched);
		}

		class DomainObjectListen<TDomainObject> : IListener
			where TDomainObject : IIdentifiable
		{
			public bool Register(string connectionId, Action<string[]> onChanged)
			{
				ConcurrentDictionary<string, IDisposable> dict;
				if (!Listeners.TryGetValue(typeof(TDomainObject), out dict))
				{
					dict = new ConcurrentDictionary<string, IDisposable>();
					if (!Listeners.TryAdd(typeof(TDomainObject), dict))
						return false;
				}
				var name = typeof(TDomainObject).FullName;
				var listener = ChangeNotification.Track<TDomainObject>().Subscribe(kv => onChanged(kv.Key));
				if (!dict.TryAdd(connectionId, listener))
					listener.Dispose();
				else
				{
					ConcurrentBag<Type> bag;
					if (!Connections.TryGetValue(connectionId, out bag))
					{
						bag = new ConcurrentBag<Type>();
						Connections.TryAdd(connectionId, bag);
					}
					bag.Add(typeof(TDomainObject));
				}
				return true;
			}

			public bool Register(string connectionId, Type type, string specificationJson, Action<string> onMatched)
			{
				Func<TDomainObject, bool> isMatched;
				try
				{
					ISpecification<TDomainObject> specification = (ISpecification<TDomainObject>)Newtonsoft.Json.JsonConvert.DeserializeObject(specificationJson, type);
					isMatched = specification.IsSatisfied.Compile();
				}
				catch { return false; }
				ConcurrentDictionary<string, IDisposable> dict;
				if (!Listeners.TryGetValue(typeof(TDomainObject), out dict))
				{
					dict = new ConcurrentDictionary<string, IDisposable>();
					if (!Listeners.TryAdd(typeof(TDomainObject), dict))
						return false;
				}
				var name = typeof(TDomainObject).FullName;
				var listener = ChangeNotification.Track<TDomainObject>().Subscribe(kv =>
					{
						foreach (var v in kv.Value.Value)
							if (isMatched(v))
								onMatched(v.URI);
					});
				if (!dict.TryAdd(connectionId, listener))
					listener.Dispose();
				else
				{
					ConcurrentBag<Type> bag;
					if (!Connections.TryGetValue(connectionId, out bag))
					{
						bag = new ConcurrentBag<Type>();
						Connections.TryAdd(connectionId, bag);
					}
					bag.Add(typeof(TDomainObject));
				}
				return true;
			}
		}

		void NotifyDomainObjectChanges(string clientID, string name, string[] ids)
		{
			TraceSource.TraceEvent(TraceEventType.Verbose, 1121, "Notify objects: {0} for {1}", clientID, name);
			Messages.Add(() => Clients.Client(clientID).Notify(name, ids));
		}

		void NotifySingleUriChange(string clientID)
		{
			TraceSource.TraceEvent(TraceEventType.Verbose, 1122, "Notify uri: {0}", clientID);
			Messages.Add(() => Clients.Client(clientID).Changed());
		}

		void NotifyCollectionUriChange(string clientID, string[] ids)
		{
			TraceSource.TraceEvent(TraceEventType.Verbose, 1123, "Notify uris: {0}", clientID);
			Messages.Add(() => Clients.Client(clientID).Found(ids));
		}

		void NotifySpecificationMatch(string clientID, string id)
		{
			TraceSource.TraceEvent(TraceEventType.Verbose, 1124, "Notify specification: {0}", clientID);
			Messages.Add(() => Clients.Client(clientID).Matched(id));
		}
	}
}
