using System;
using System.Threading.Tasks;
using Revenj.DomainPatterns;

namespace Revenj
{
	public interface ICrudProxy
	{
		Task<TAggregate> Create<TAggregate>(TAggregate aggregate)
			where TAggregate : class, IAggregateRoot;
		Task<T> Read<T>(string uri)
			where T : class, IIdentifiable;
		Task<TAggregate> Update<TAggregate>(TAggregate aggregate)
			where TAggregate : class, IAggregateRoot;
		Task<TAggregate> Delete<TAggregate>(string uri)
			where TAggregate : class, IAggregateRoot;
	}

	public static class CrudProxyHelper
	{
		public static Task<TAggregate> Delete<TAggregate>(this ICrudProxy proxy, TAggregate aggregate)
			where TAggregate : class, IAggregateRoot
		{
			if (proxy == null)
				throw new ArgumentNullException("proxy can't be null");
			if (aggregate == null)
				throw new ArgumentNullException("aggregate can't be null");
			return proxy.Delete<TAggregate>(aggregate.URI);
		}
	}
}
