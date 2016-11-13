using System;
using System.Collections.Generic;

namespace Revenj.DomainPatterns
{
	public interface IHistory<T> : IIdentifiable
		where T : class, IAggregateRoot
	{
		IEnumerable<ISnapshot<T>> Snapshots { get; }
	}

	public interface ISnapshot<T> : IIdentifiable
		where T : class, IAggregateRoot
	{
		DateTime At { get; }
		string Action { get; }
		T Value { get; }
	}
}
