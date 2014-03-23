using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using NGS.DatabasePersistence.Oracle.Converters;
using NGS.DomainPatterns;
using NGS.Logging;
using Oracle.DataAccess.Client;

namespace NGS.DatabasePersistence.Oracle
{
	public class OracleAdvancedQueueNotification : IEagerNotification, IDisposable
	{
		private static readonly string ConsumerName = ConfigurationManager.AppSettings["Oracle.QueueConsumer"] ?? "Local";

		private readonly ConnectionInfo ConnectionInfo;
		private OracleConnection Connection;
		private OracleCommand CommitCommand;
		private OracleAQQueue Queue;
		private readonly Subject<NotifyInfo> Subject = new Subject<NotifyInfo>();
		private bool IsDisposed;
		private readonly Lazy<IDomainModel> DomainModel;
		private readonly ConcurrentDictionary<string, List<Type>> Targets = new ConcurrentDictionary<string, List<Type>>(1, 17);
		private int RetryCount;
		private readonly ConcurrentDictionary<Type, IRepository<IIdentifiable>> Repositories =
			new ConcurrentDictionary<Type, IRepository<IIdentifiable>>(1, 17);
		private readonly IServiceLocator Locator;
		private readonly ILogger Logger;

		public OracleAdvancedQueueNotification(
			ConnectionInfo connectionInfo,
			Lazy<IDomainModel> domainModel,
			IServiceLocator locator,
			ILogFactory logFactory)
		{
			Contract.Requires(connectionInfo != null);
			Contract.Requires(domainModel != null);
			Contract.Requires(locator != null);

			this.ConnectionInfo = connectionInfo;
			this.DomainModel = domainModel;
			this.Locator = locator;
			Logger = logFactory.Create("Oracle notification");
			Notifications = Subject.AsObservable();
			SetUpConnection();
		}

		private void SetUpConnection()
		{
			try
			{
				RetryCount++;
				if (RetryCount > 60)
				{
					Logger.Fatal("Retry count exceeded setting up connection string: " + ConnectionInfo.ConnectionString);
					RetryCount = 30;
				}
				if (Connection != null)
				{
					Connection.StateChange -= Connection_StateChange;
					Queue.MessageAvailable -= Queue_Notification;
					try { Connection.Dispose(); }
					catch (Exception ex)
					{
						Logger.Error(ex.ToString());
					}
				}
				Connection = new OracleConnection(ConnectionInfo.ConnectionString);
				Connection.Open();
				CommitCommand = Connection.CreateCommand();
				CommitCommand.CommandText = "COMMIT";
				Connection.StateChange += Connection_StateChange;
				Queue = new OracleAQQueue("\"-NGS-\".NOTIFY_QUEUE", Connection, OracleAQMessageType.Udt, "-NGS-.NOTIFY_INFO_TYPE");
				Queue.NotificationConsumers = new[] { ConsumerName };
				Queue.DequeueOptions.ConsumerName = ConsumerName;
				Queue.DequeueOptions.DequeueMode = OracleAQDequeueMode.Remove;
				Queue.DequeueOptions.DeliveryMode = OracleAQMessageDeliveryMode.Persistent;
				Queue.DequeueOptions.Visibility = OracleAQVisibilityMode.OnCommit;
				Queue.DequeueOptions.NavigationMode = OracleAQNavigationMode.NextTransaction;
				Queue.MessageAvailable += Queue_Notification;
				var converters = new List<OracleNotifyInfoConverter>();
				try
				{
					var deqOpt =
						new OracleAQDequeueOptions
						{
							Wait = 1,
							ConsumerName = ConsumerName,
							DequeueMode = OracleAQDequeueMode.Remove,
							DeliveryMode = OracleAQMessageDeliveryMode.Persistent,
							Visibility = OracleAQVisibilityMode.OnCommit,
							NavigationMode = OracleAQNavigationMode.NextTransaction
						};
					OracleAQMessage msg;
					while ((msg = Queue.Dequeue(deqOpt)) != null)
					{
						var nic = msg.Payload as OracleNotifyInfoConverter;
						if (nic != null)
							converters.Add(nic);
					}
				}
				catch (OracleException ex)
				{
					var err = ex.Errors.Count > 0 ? ex.Errors[0] : null;
					if (err == null || err.Number != 25228)
						Logger.Info(ex.ToString());
				}
				if (converters.Count > 0)
					ProcessNotifyConverters(converters);
				RetryCount = 0;
			}
			catch (Exception ex)
			{
				RetryCount++;
				Logger.Error(ex.ToString());
			}
		}

		private void Connection_StateChange(object sender, StateChangeEventArgs e)
		{
			if (IsDisposed)
				return;
			if (e.CurrentState == ConnectionState.Closed)
				SetUpConnection();
		}

		private void Queue_Notification(object sender, OracleAQMessageAvailableEventArgs e)
		{
			try
			{
				var converters =
					(from m in Queue.DequeueArray(e.AvailableMessages)
					 let ni = m.Payload as OracleNotifyInfoConverter
					 where ni != null
					 select ni).ToList();

				ProcessNotifyConverters(converters);
			}
			catch (Exception ex)
			{
				Logger.Error(e.QueueName + ": " + e.AvailableMessages + ex.ToString());
			}
		}

		public void Notify(NotifyInfo info) { Subject.OnNext(info); }

		private void ProcessNotifyConverters(List<OracleNotifyInfoConverter> converters)
		{
			foreach (var it in converters)
			{
				switch (it.Operation)
				{
					case "Update":
						Subject.OnNext(new NotifyInfo(it.Source, NotifyInfo.OperationEnum.Update, it.Uris.ToArray()));
						break;
					case "Change":
						Subject.OnNext(new NotifyInfo(it.Source, NotifyInfo.OperationEnum.Change, it.Uris.ToArray()));
						break;
					case "Delete":
						Subject.OnNext(new NotifyInfo(it.Source, NotifyInfo.OperationEnum.Delete, it.Uris.ToArray()));
						break;
					default:
						Subject.OnNext(new NotifyInfo(it.Source, NotifyInfo.OperationEnum.Insert, it.Uris.ToArray()));
						break;
				}
			}
			CommitCommand.ExecuteNonQuery();
		}

		public IObservable<NotifyInfo> Notifications { get; private set; }

		private IRepository<IIdentifiable> GetRepository<T>(string name)
		{
			IRepository<IIdentifiable> repository;
			if (!Repositories.TryGetValue(typeof(T), out repository))
			{
				var source = DomainModel.Value.Find(name);
				repository = Locator.Resolve<IRepository<IIdentifiable>>(typeof(IRepository<>).MakeGenericType(source));
				Repositories.TryAdd(typeof(T), repository);
			}
			return repository;
		}

		public IObservable<KeyValuePair<string[], Lazy<T[]>>> Track<T>()
		{
			var type = typeof(T);
			return
				Notifications.Where(it =>
				{
					List<Type> list;
					if (!Targets.TryGetValue(it.Name, out list))
					{
						list = new List<Type>();
						var domainType = DomainModel.Value.Find(it.Name);
						if (domainType != null)
						{
							list.Add(domainType);
							list.AddRange(domainType.GetInterfaces());
						}
						Targets.TryAdd(it.Name, list);
					}
					return list.Contains(type);
				}).Select(it => new KeyValuePair<string[], Lazy<T[]>>(it.URI, new Lazy<T[]>(() => GetRepository<T>(it.Name).Find(it.URI) as T[])));
		}

		public void Dispose()
		{
			if (IsDisposed)
				return;
			IsDisposed = true;
			try
			{
				if (Connection != null && Connection.State == ConnectionState.Open)
					Connection.Close();
			}
			catch (Exception ex)
			{
				Logger.Error(ex.Message);
			}
			try { Connection.Dispose(); }
			catch (Exception ex)
			{
				Logger.Error(ex.Message);
			}
			Connection = null;
		}
	}
}
