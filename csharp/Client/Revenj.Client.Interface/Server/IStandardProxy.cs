using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Revenj.DomainPatterns;

namespace Revenj
{
	public interface IStandardProxy
	{
		Task<string[]> Persist<TAggregate>(
			IEnumerable<TAggregate> insert,
			IEnumerable<KeyValuePair<TAggregate, TAggregate>> update,
			IEnumerable<TAggregate> delete)
			where TAggregate : IAggregateRoot;
		Task<TResult[]> OlapCube<TCube, TSpecification, TResult>(
			TSpecification specification,
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IDictionary<string, bool> order);
		Task<TResult[]> OlapCube<TCube, TResult>(
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IDictionary<string, bool> order);
		Task<TResult> Execute<TArgument, TResult>(string command, TArgument argument);
	}

	public static class StandardProxyHelper
	{
		public static Task<string[]> Insert<TAggregate>(
			this IStandardProxy proxy,
			IEnumerable<TAggregate> aggregates)
			where TAggregate : class, IAggregateRoot
		{
			if (proxy == null)
				throw new ArgumentNullException("proxy can't be null");
			if (aggregates == null)
				throw new ArgumentNullException("aggregates can't be null");
			return proxy.Persist(aggregates, null, null);
		}

		public static Task Update<TAggregate>(
			this IStandardProxy proxy,
			IEnumerable<TAggregate> aggregates)
			where TAggregate : class, IAggregateRoot
		{
			if (proxy == null)
				throw new ArgumentNullException("proxy can't be null");
			if (aggregates == null)
				throw new ArgumentNullException("aggregates can't be null");
			return
				proxy.Persist(
					null,
					aggregates.Select(a => new KeyValuePair<TAggregate, TAggregate>(null, a)),
					null);
		}

		public static Task Delete<TAggregate>(
			this IStandardProxy proxy,
			IEnumerable<TAggregate> aggregates)
			where TAggregate : class, IAggregateRoot
		{
			if (proxy == null)
				throw new ArgumentNullException("proxy can't be null");
			if (aggregates == null)
				throw new ArgumentNullException("aggregates can't be null");
			return proxy.Persist(null, null, aggregates);
		}
	}
}
