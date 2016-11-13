using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Revenj.DomainPatterns
{
	public interface ISearchableRepository<T>
		where T : class, ISearchable
	{
		Task<T[]> Search(ISpecification<T> specification, int? limit, int? offset, IDictionary<string, bool> order);
		Task<long> Count(ISpecification<T> specification);
	}

	public interface IRepository<T> : ISearchableRepository<T>
		where T : class, IIdentifiable
	{
		Task<T[]> Find(IEnumerable<string> uris);
	}

	public interface IPersistableRepository<T> : IRepository<T>
		where T : class, IAggregateRoot
	{
		Task<string[]> Persist(IEnumerable<T> insert, IEnumerable<KeyValuePair<T, T>> update, IEnumerable<T> delete);
	}

	public static partial class RepositoryHelper
	{
		public static Task<string[]> Persist<T>(
			this IPersistableRepository<T> repository,
			IEnumerable<T> insert,
			IEnumerable<T> update,
			IEnumerable<T> delete)
			where T : class, IAggregateRoot
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return
				repository.Persist(
					insert,
					update != null ? update.Select(it => new KeyValuePair<T, T>(default(T), it)) : null,
					delete);
		}

		public static Task<T[]> FindAll<T>(this ISearchableRepository<T> repository)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Search(null, null, null, null);
		}

		public static Task<long> CountAll<T>(this ISearchableRepository<T> repository)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Count(null);
		}

		public static Task<T> Find<T>(this IRepository<T> repository, string uri)
			where T : class, IIdentifiable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Find(new[] { uri }).ContinueWith(t => t.Result.FirstOrDefault());
		}

		public static Task<T[]> Search<T>(this ISearchableRepository<T> repository, ISpecification<T> specification)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Search(specification, null, null, null);
		}

		public static Task<string[]> Insert<T>(this IPersistableRepository<T> repository, IEnumerable<T> data)
			where T : class, IAggregateRoot
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Persist(data, null, null);
		}

		public static Task Update<T>(this IPersistableRepository<T> repository, IEnumerable<T> data)
			where T : class, IAggregateRoot
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Persist(null, data, null);
		}

		public static Task Delete<T>(this IPersistableRepository<T> repository, IEnumerable<T> data)
			where T : class, IAggregateRoot
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Persist(null, null, data);
		}

		public static Task<string> Insert<T>(this IPersistableRepository<T> repository, T data)
			where T : class, IAggregateRoot
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Insert(new[] { data }).ContinueWith(t => t.Result.First());
		}

		public static Task Update<T>(this IPersistableRepository<T> repository, T data)
			where T : class, IAggregateRoot
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Update(new[] { data });
		}

		public static Task Update<T>(this IPersistableRepository<T> repository, T oldValue, T newValue)
			where T : class, IAggregateRoot
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Persist(null, new[] { new KeyValuePair<T, T>(oldValue, newValue) }, null);
		}

		public static Task Delete<T>(this IPersistableRepository<T> repository, T data)
			where T : class, IAggregateRoot
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Delete(new[] { data });
		}

		public static Task Delete<T>(this IPersistableRepository<T> repository, string uri)
			where T : class, IAggregateRoot
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			var oldValue = repository.Find(uri);
			return oldValue.ContinueWith(t =>
			{
				if (t.Result == null)
					throw new ArgumentException(string.Format("Can't find {0} with URI: {1}", typeof(T).FullName, uri));

				repository.Delete(new[] { t.Result }).Wait();
			});
		}
	}
}
