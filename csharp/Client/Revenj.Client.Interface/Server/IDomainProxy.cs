using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Revenj.DomainPatterns;

namespace Revenj
{
	public interface IDomainProxy
	{
		Task<T[]> Find<T>(IEnumerable<string> uris)
			where T : class, IIdentifiable;
		Task<T[]> Search<T>(
			ISpecification<T> specification,
			int? limit,
			int? offset,
			IDictionary<string, bool> order)
			where T : class, ISearchable;
		Task<long> Count<T>(ISpecification<T> specification)
			where T : class, ISearchable;
		Task<string> Submit<T>(T domainEvent)
			where T : class, IDomainEvent;
		Task<TAggregate> Submit<TEvent, TAggregate>(TEvent domainEvent, string uri)
			where TEvent : class, IDomainEvent<TAggregate>
			where TAggregate : class, IAggregateRoot;
	}

	public static class DomainProxyHelper
	{
		public static Task<T[]> FindAll<T>(this IDomainProxy repository)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Search<T>(null, null, null, null);
		}
		public static Task<T[]> FindAll<T>(
			this IDomainProxy repository,
			int? limit,
			int? offset)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Search<T>(null, limit, offset, null);
		}
		public static Task<T[]> Search<T>(
			this IDomainProxy repository,
			ISpecification<T> specification)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Search<T>(specification, null, null, null);
		}
		public static Task<long> Count<T>(this IDomainProxy repository)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Count<T>(null);
		}
	}
}
