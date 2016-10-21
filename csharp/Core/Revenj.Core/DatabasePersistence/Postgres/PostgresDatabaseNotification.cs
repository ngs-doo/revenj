using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using System.Threading;
using Revenj.DatabasePersistence.Postgres.Converters;
using Revenj.DatabasePersistence.Postgres.Npgsql;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres
{
	internal class PostgresDatabaseNotification : IEagerNotification, IDisposable
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Database");

		private NpgsqlConnection Connection;
		private readonly Subject<NotifyInfo> Subject = new Subject<NotifyInfo>();
		private bool IsDisposed;
		private readonly Lazy<IDomainModel> DomainModel;
		private readonly ConcurrentDictionary<string, HashSet<Type>> Targets = new ConcurrentDictionary<string, HashSet<Type>>(1, 17);
		private int RetryCount;
		private readonly ConcurrentDictionary<Type, IRepository<IIdentifiable>> Repositories =
			new ConcurrentDictionary<Type, IRepository<IIdentifiable>>(1, 17);
		private readonly IServiceProvider Locator;
		private readonly BufferedTextReader Reader = new BufferedTextReader(string.Empty, new char[64], new char[8192]);
		private readonly ISystemState SystemState;

		public PostgresDatabaseNotification(
			ConnectionInfo connectionInfo,
			Lazy<IDomainModel> domainModel,
			ISystemState systemState,
			IServiceProvider locator)
		{
			Contract.Requires(connectionInfo != null);
			Contract.Requires(domainModel != null);
			Contract.Requires(locator != null);
			Contract.Requires(systemState != null);

			this.DomainModel = domainModel;
			this.Locator = locator;
			this.SystemState = systemState;
			Notifications = Subject.AsObservable();
			if (ConfigurationManager.AppSettings["Revenj.Notifications"] != "disabled")
				SetUpConnection(connectionInfo.ConnectionString + ";SyncNotification=true");
			AppDomain.CurrentDomain.ProcessExit += (s, ea) => IsDisposed = true;
			AppDomain.CurrentDomain.DomainUnload += (s, ea) => IsDisposed = true;
		}

		private void SetUpConnection(string connectionString)
		{
			RetryCount++;
			if (RetryCount > 60)
			{
				TraceSource.TraceEvent(TraceEventType.Critical, 5130, "Retry count exceeded: {0}", connectionString);
				RetryCount = 30;
			}
			try
			{
				if (Connection != null)
				{
					Connection.StateChange -= Connection_StateChange;
					Connection.Notification -= Connection_Notification;
					try { Connection.Close(); }
					catch (Exception ex)
					{
						TraceSource.TraceEvent(TraceEventType.Error, 5132, "{0}", ex);
					}
					try { Connection.Dispose(); }
					catch (Exception ex)
					{
						TraceSource.TraceEvent(TraceEventType.Error, 5133, "{0}", ex);
					}
				}
				Connection = new NpgsqlConnection(connectionString);
				Connection.StateChange += Connection_StateChange;
				Connection.Notification += Connection_Notification;
				Connection.Open();
				var com = Connection.CreateCommand();
				com.CommandText = "listen events; listen aggregate_roots; listen migration;";
				com.ExecuteNonQuery();
				RetryCount = 0;
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5134, "{0}", ex);
				Thread.Sleep(1000 * RetryCount);
			}
		}

		private void Connection_StateChange(object sender, StateChangeEventArgs e)
		{
			if (IsDisposed)
				return;
			if (e.CurrentState == ConnectionState.Closed
				|| e.CurrentState == ConnectionState.Broken)
				SetUpConnection(Connection.ConnectionString);
			else if (e.CurrentState != ConnectionState.Open)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5136, "Invalid notification state: {0}", e.CurrentState);
			}
		}

		public void Notify(NotifyInfo info) { Subject.OnNext(info); }

		private void Connection_Notification(object sender, NpgsqlNotificationEventArgs e)
		{
			try
			{
				if (e.Condition == "events" || e.Condition == "aggregate_roots")
				{
					TraceSource.TraceEvent(TraceEventType.Verbose, 5137, "Postgres notification: {0} with {1}", e.Condition, e.AdditionalInformation);
					var firstSeparator = e.AdditionalInformation.IndexOf(':');
					var name = e.AdditionalInformation.Substring(0, firstSeparator);
					var secondSeparator = e.AdditionalInformation.Substring(firstSeparator + 1).IndexOf(':');
					var op = e.AdditionalInformation.Substring(firstSeparator + 1, secondSeparator).Trim();
					var array = e.AdditionalInformation.Substring(firstSeparator + secondSeparator + 2).Trim();
					if (array.Length > 0)
					{
						var uris = StringConverter.ParseCollection(Reader.Reuse(array), 0, false).ToArray();
						switch (op)
						{
							case "Update":
								Subject.OnNext(new NotifyInfo(name, NotifyInfo.OperationEnum.Update, NotifyInfo.SourceEnum.Database, uris));
								break;
							case "Change":
								Subject.OnNext(new NotifyInfo(name, NotifyInfo.OperationEnum.Change, NotifyInfo.SourceEnum.Database, uris));
								break;
							case "Delete":
								Subject.OnNext(new NotifyInfo(name, NotifyInfo.OperationEnum.Delete, NotifyInfo.SourceEnum.Database, uris));
								break;
							default:
								Subject.OnNext(new NotifyInfo(name, NotifyInfo.OperationEnum.Insert, NotifyInfo.SourceEnum.Database, uris));
								break;
						}
					}
				}
				else if (e.Condition == "migration")
				{
					TraceSource.TraceEvent(TraceEventType.Information, 5155, "Postgres migration detected: {0} ", e.AdditionalInformation);
					SystemState.Notify(new SystemEvent("migration", e.AdditionalInformation));
				}
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5138, "{0}{1} {2}", e.Condition, e.AdditionalInformation, ex);
			}
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
			//TODO: notifications can lag and when they do, scope can be disposed
			var type = typeof(T);
			return
				Notifications.Where(it =>
				{
					HashSet<Type> set;
					if (!Targets.TryGetValue(it.Name, out set))
					{
						set = new HashSet<Type>();
						var domainType = DomainModel.Value.Find(it.Name);
						if (domainType != null)
						{
							set.Add(domainType);
							foreach (var i in domainType.GetInterfaces())
								set.Add(i);
						}
						Targets.TryAdd(it.Name, set);
					}
					return set.Contains(type);
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
				TraceSource.TraceEvent(TraceEventType.Error, 5139, "{0}", ex);
			}
			try { Connection.Dispose(); }
			catch (Exception ex)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5140, "{0}", ex);
			}
			Connection = null;
		}
	}
}
