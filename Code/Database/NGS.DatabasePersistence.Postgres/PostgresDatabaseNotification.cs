using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics.Contracts;
using System.IO;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using System.Threading;
using NGS.DatabasePersistence.Postgres.Converters;
using NGS.DomainPatterns;
using NGS.Logging;
using Npgsql;

namespace NGS.DatabasePersistence.Postgres
{
	public class PostgresDatabaseNotification : IDataChangeNotification, IDisposable
	{
		private NpgsqlConnection Connection;
		private readonly Subject<NotifyInfo> Subject = new Subject<NotifyInfo>();
		private bool IsDisposed;
		private readonly Lazy<IDomainModel> DomainModel;
		private readonly ConcurrentDictionary<string, HashSet<Type>> Targets = new ConcurrentDictionary<string, HashSet<Type>>(1, 17);
		private int RetryCount;
		private readonly ConcurrentDictionary<Type, IRepository<IIdentifiable>> Repositories =
			new ConcurrentDictionary<Type, IRepository<IIdentifiable>>(1, 17);
		private readonly IServiceLocator Locator;
		private readonly ILogger Logger;

		public PostgresDatabaseNotification(
			ConnectionInfo connectionInfo,
			Lazy<IDomainModel> domainModel,
			IServiceLocator locator,
			ILogFactory logFactory)
		{
			Contract.Requires(connectionInfo != null);
			Contract.Requires(domainModel != null);
			Contract.Requires(locator != null);

			this.DomainModel = domainModel;
			this.Locator = locator;
			Logger = logFactory.Create("Postgres notification");
			Notifications = Subject.AsObservable();
			SetUpConnection(connectionInfo.ConnectionString + ";SyncNotification=true");
		}

		private void SetUpConnection(string connectionString)
		{
			RetryCount++;
			if (RetryCount > 60)
			{
				Logger.Fatal("Retry count exceeded setting up connection string: " + connectionString);
				RetryCount = 30;
			}
			try
			{
				if (Connection != null)
				{
					Connection.StateChange -= Connection_StateChange;
					Connection.Notification -= Connection_Notification;
					try { Connection.Dispose(); }
					catch (Exception ex)
					{
						Logger.Error(ex.ToString());
					}
				}
				Connection = new NpgsqlConnection(connectionString);
				Connection.StateChange += Connection_StateChange;
				Connection.Notification += Connection_Notification;
				Connection.Open();
				var com = Connection.CreateCommand();
				com.CommandText = "listen events; listen aggregate_roots;";
				com.ExecuteNonQuery();
				RetryCount = 0;
			}
			catch (Exception ex)
			{
				Logger.Error(ex.ToString());
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
				Logger.Error("Invalid notification state: " + e.CurrentState.ToString());
			}
		}

		private void Connection_Notification(object sender, NpgsqlNotificationEventArgs e)
		{
			try
			{
				if (e.Condition == "events" || e.Condition == "aggregate_roots")
				{
					var firstSeparator = e.AdditionalInformation.IndexOf(':');
					var name = e.AdditionalInformation.Substring(0, firstSeparator);
					var secondSeparator = e.AdditionalInformation.Substring(firstSeparator + 1).IndexOf(':');
					var op = e.AdditionalInformation.Substring(firstSeparator + 1, secondSeparator).Trim();
					var array = e.AdditionalInformation.Substring(firstSeparator + secondSeparator + 2).Trim();
					if (array.Length > 0)
					{

						var uris = StringConverter.ParseCollection(new StringReader(array), 0, false).ToArray();
						switch (op)
						{
							case "Update":
								Subject.OnNext(new NotifyInfo(name, NotifyInfo.OperationEnum.Update, uris));
								break;
							case "Change":
								Subject.OnNext(new NotifyInfo(name, NotifyInfo.OperationEnum.Change, uris));
								break;
							case "Delete":
								Subject.OnNext(new NotifyInfo(name, NotifyInfo.OperationEnum.Delete, uris));
								break;
							default:
								Subject.OnNext(new NotifyInfo(name, NotifyInfo.OperationEnum.Insert, uris));
								break;
						}
					}
				}
			}
			catch (Exception ex)
			{
				Logger.Error(e.Condition + e.AdditionalInformation + ex.ToString());
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
