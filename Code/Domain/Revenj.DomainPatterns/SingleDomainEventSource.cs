using System;
using System.Diagnostics.Contracts;
using System.Reactive.Linq;
using System.Reactive.Subjects;

namespace Revenj.DomainPatterns
{
	internal class SingleDomainEventSource<TEvent> : IDomainEventSource<TEvent>, IDisposable
		where TEvent : IDomainEvent
	{
		private readonly IDisposable Subscription;
		private readonly Subject<TEvent> Subject = new Subject<TEvent>();

		public SingleDomainEventSource(IDataChangeNotification notifications)
		{
			Contract.Requires(notifications != null);

			Subscription =
				notifications.Track<TEvent>().Subscribe(kv =>
				{
					foreach (var ev in kv.Value.Value)
						Subject.OnNext(ev);
				});
			Events = Subject.AsObservable();
		}

		public IObservable<TEvent> Events { get; private set; }

		public void Dispose()
		{
			Subscription.Dispose();
		}
	}
}
