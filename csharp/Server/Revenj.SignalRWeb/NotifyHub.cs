using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNet.SignalR;
using Revenj.DomainPatterns;

namespace Revenj.SignalRWeb
{
	public class NotifyHub : Hub
	{
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
			var thread = new Thread(SendMessages);
			thread.Start();
		}

		internal static void Stop()
		{
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
					Console.Write(ex.Message);
				}
			} while (IsRunning || Messages.Count > 0);
		}

		public override Task OnDisconnected()
		{
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
			return base.OnDisconnected();
		}

		public void Listen(string domainObject)
		{
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
			var cid = Context.ConnectionId;
			if (rl.Register(cid, ids => NotifyDomainObjectChanges(cid, domainObject, ids)))
				Clients.Caller.Success("Registered for " + domainObject);
			else
				Clients.Caller.Error("Error registering for " + domainObject);
		}

		public void WatchSingle(string domainObject, string uri)
		{
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
			var cid = Context.ConnectionId;
			if (rl.Register(cid, ids => { if (ids.Contains(uri)) NotifySingleUriChange(cid); }))
				Clients.Caller.Success("Registered for " + domainObject);
			else
				Clients.Caller.Error("Error registering for " + domainObject);
		}

		public void WatchCollection(string domainObject, string[] uris)
		{
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
			var cid = Context.ConnectionId;
			var set = new HashSet<string>(uris);
			if (rl.Register(cid, ids => { if (set.Overlaps(ids)) NotifyCollectionUriChange(cid, set.Intersect(uris).ToArray()); }))
				Clients.Caller.Success("Registered for " + domainObject);
			else
				Clients.Caller.Error("Error registering for " + domainObject);
		}

		public void WatchSpecification(string domainObject, string specification, string json)
		{
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
			var cid = Context.ConnectionId;
			if (rl.Register(cid, specType, json, id => NotifySpecificationMatch(cid, id)))
				Clients.Caller.Success("Registered for " + specification + " in " + domainObject);
			else
				Clients.Caller.Error("Error registering for " + domainObject);
		}

		public void UnListen(string domainObject)
		{
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
			if (dict.TryRemove(Context.ConnectionId, out registration) && registration != null)
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
			Messages.Add(() => Clients.Client(clientID).Notify(name, ids));
		}

		void NotifySingleUriChange(string clientID)
		{
			Messages.Add(() => Clients.Client(clientID).Changed());
		}

		void NotifyCollectionUriChange(string clientID, string[] ids)
		{
			Messages.Add(() => Clients.Client(clientID).Found(ids));
		}

		void NotifySpecificationMatch(string clientID, string id)
		{
			Messages.Add(() => Clients.Client(clientID).Matched(id));
		}
	}
}
