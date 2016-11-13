using System;
using System.Collections.Generic;
using System.Linq;

namespace Revenj.DomainPatterns
{
	public class History<TRoot> : IHistory<TRoot>
		where TRoot : class, IAggregateRoot
	{
		public Snapshot<TRoot>[] Snapshots { get; private set; }

		public static History<TRoot> Create(IEnumerable<Snapshot<TRoot>> snapshots)
		{
			return
				new History<TRoot>
				{
					Snapshots = (from s in snapshots orderby s.At select s).ToArray()
				};
		}

		IEnumerable<ISnapshot<TRoot>> IHistory<TRoot>.Snapshots { get { return Snapshots; } }
		string IIdentifiable.URI { get { return Snapshots[0].Value.URI; } }
	}

	public class Snapshot<TRoot> : ISnapshot<TRoot>
		where TRoot : class, IAggregateRoot
	{
		public DateTime At { get; private set; }
		public TRoot Value { get; private set; }
		public string Action { get; private set; }

		public static Snapshot<TRoot> Create(DateTime at, TRoot value, string action)
		{
			return
				new Snapshot<TRoot>
				{
					At = at,
					Value = value,
					Action = action
				};
		}

		public string URI { get { return Value.URI + "/" + At.Ticks; } }
	}
}
