using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Reactive.Linq;
using System.Reactive.Subjects;

namespace Revenj.DomainPatterns
{
	public interface IEagerNotification : IDataChangeNotification
	{
		void Notify(NotifyInfo info);
	}

	internal class RegisterChangeNotifications<TSource> : IObservable<TSource>, IDisposable
	{
		private readonly IDisposable ChangeNotification;
		private readonly Func<IObserver<TSource>, IDisposable> Register;

		public RegisterChangeNotifications(IDataChangeNotification notifications)
		{
			var target = typeof(TSource);
			if (target.IsGenericType && target.GetGenericTypeDefinition() == typeof(Lazy<>))
			{
				target = target.GetGenericArguments()[0];
				if (target.IsArray)
				{
					target = target.GetElementType();
					dynamic cn = Activator.CreateInstance(typeof(ChangeNotifications<>).MakeGenericType(target), new object[] { notifications });
					Register = cn.SubscribeBulk;
					ChangeNotification = cn;
				}
				else
				{
					dynamic cn = Activator.CreateInstance(typeof(ChangeNotifications<>).MakeGenericType(target), new object[] { notifications });
					Register = cn.SubscribeLazy;
					ChangeNotification = cn;
				}
			}
			else
			{
				var cn = new ChangeNotifications<TSource>(notifications);
				Register = cn.SubscribeEager;
				ChangeNotification = cn;
			}
		}

		public IDisposable Subscribe(IObserver<TSource> observer)
		{
			return Register(observer);
		}

		public void Dispose()
		{
			ChangeNotification.Dispose();
		}
	}

	internal class ChangeNotifications<TSource> : IDisposable
	{
		private readonly IDisposable Subscription;
		private readonly Subject<KeyValuePair<string[], Lazy<TSource[]>>> Subject = new Subject<KeyValuePair<string[], Lazy<TSource[]>>>();

		public ChangeNotifications(IDataChangeNotification notifications)
		{
			Contract.Requires(notifications != null);

			Subscription = notifications.Track<TSource>().Subscribe(kv => Subject.OnNext(kv));
			var source = Subject.AsObservable();
			BulkChanges = source.Select(it => it.Value);
			LazyChanges =
				from it in source
				let lazy = it.Value
				from i in Enumerable.Range(0, it.Key.Length)
				select new Lazy<TSource>(() => lazy.Value[i]);
			EagerChanges =
				from it in source
				from v in it.Value.Value
				select v;
		}

		private readonly IObservable<TSource> EagerChanges;
		private readonly IObservable<Lazy<TSource>> LazyChanges;
		private readonly IObservable<Lazy<TSource[]>> BulkChanges;

		public Func<IObserver<TSource>, IDisposable> SubscribeEager { get { return EagerChanges.Subscribe; } }
		public Func<IObserver<Lazy<TSource>>, IDisposable> SubscribeLazy { get { return LazyChanges.Subscribe; } }
		public Func<IObserver<Lazy<TSource[]>>, IDisposable> SubscribeBulk { get { return BulkChanges.Subscribe; } }

		public void Dispose()
		{
			Subscription.Dispose();
		}
	}
}
