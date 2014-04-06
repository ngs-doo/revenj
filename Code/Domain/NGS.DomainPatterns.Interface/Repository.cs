using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;

namespace NGS.DomainPatterns
{
	/// <summary>
	/// Data access abstraction. 
	/// Lookup data by its identifier.
	/// </summary>
	/// <typeparam name="TValue">data type</typeparam>
	public interface IRepository<out TValue>
		where TValue : IIdentifiable
	{
		/// <summary>
		/// Find objects by provided identifiers
		/// </summary>
		/// <param name="uris">object identifiers</param>
		/// <returns>found objects</returns>
		TValue[] Find(IEnumerable<string> uris);
	}
	//TODO: TValue should implement IQueryable-like interface (doesn't exists yet)
	/// <summary>
	/// Data access abstraction.
	/// Query data using LINQ.
	/// </summary>
	/// <typeparam name="TValue">data type</typeparam>
	public interface IQueryableRepository<out TValue>
		where TValue : IDataSource
	{
		/// <summary>
		/// Query data using provided expression (optional).
		/// If specification is provided it must be compatibile with data type.
		/// This is only a projection, actual query will be done after materialization from IQueryable&lt;TValue&gt;
		/// </summary>
		/// <typeparam name="TCondition">specification type</typeparam>
		/// <param name="specification">filter predicate</param>
		/// <returns>data projection</returns>
		IQueryable<TValue> Query<TCondition>(ISpecification<TCondition> specification);
		/// <summary>
		/// Search data using provided specification.
		/// If specification is provided it must be compatibile with data type.
		/// </summary>
		/// <typeparam name="TCondition">specification type</typeparam>
		/// <param name="specification">filter predicate</param>
		/// <param name="limit">limit maximum number of results</param>
		/// <param name="offset">skip initial results</param>
		/// <returns>found items</returns>
		TValue[] Search<TCondition>(ISpecification<TCondition> specification, int? limit, int? offset);
	}
	/// <summary>
	/// Aggregate root persistable repository.
	/// Besides querying capabilities, repository has set based API for persistance.
	/// </summary>
	/// <typeparam name="TRoot">aggregate root type</typeparam>
	public interface IPersistableRepository<TRoot> : IQueryableRepository<TRoot>, IRepository<TRoot>
		where TRoot : IAggregateRoot
	{
		/// <summary>
		/// Persist aggregate roots. Bulk persistance.
		/// Inserted aggregates will return new identifiers.
		/// Aggregate roots will be modified in place.
		/// For update aggregates, if old aggregate is not provided, it will be looked up using aggregate identifier.
		/// </summary>
		/// <param name="insert">new aggregates</param>
		/// <param name="update">collection of old and changed aggregates</param>
		/// <param name="delete">remove aggregates</param>
		/// <returns>created identifiers</returns>
		string[] Persist(IEnumerable<TRoot> insert, IEnumerable<KeyValuePair<TRoot, TRoot>> update, IEnumerable<TRoot> delete);
	}
	/// <summary>
	/// Utility for easier usage of repositories.
	/// </summary>
	public static class RepositoryHelper
	{
		/// <summary>
		/// Persist aggregate roots. Bulk persistance.
		/// Inserted aggregates will return new identifiers.
		/// Aggregate roots will be modified in place.
		/// For update aggregates, old aggregates will be loaded from change tracking or looked up using aggregate identifier.
		/// </summary>
		/// <typeparam name="TRoot">aggregate root type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="insert">new aggregates</param>
		/// <param name="update">collection of changed aggregates</param>
		/// <param name="delete">remove aggregates</param>
		/// <returns>created identifiers</returns>
		public static string[] Persist<TRoot>(
			this IPersistableRepository<TRoot> repository,
			IEnumerable<TRoot> insert,
			IEnumerable<TRoot> update,
			IEnumerable<TRoot> delete)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);

			if (insert != null && insert.Any()
				|| update != null && update.Any()
				|| delete != null && delete.Any())
				return
					repository.Persist(
						insert,
						update != null
							? (from u in update
							   let ct = u as IChangeTracking<TRoot>
							   select new KeyValuePair<TRoot, TRoot>(ct != null ? ct.GetOriginalValue() : default(TRoot), u))
								.ToList()
							: null,
						delete);
			return new string[0];
		}
		/// <summary>
		/// Query all data of specific type.
		/// This is only a projection, actual query will be done after materialization from IQueryable&lt;TValue&gt;
		/// </summary>
		/// <typeparam name="TValue">data type</typeparam>
		/// <param name="repository">queryable repository</param>
		/// <returns>projection to data</returns>
		public static IQueryable<TValue> FindAll<TValue>(this IQueryableRepository<TValue> repository)
			where TValue : IDataSource
		{
			Contract.Requires(repository != null);

			return repository.Query<TValue>(null);
		}
		/// <summary>
		/// Search all data of specified type.
		/// </summary>
		/// <typeparam name="TValue">data type</typeparam>
		/// <param name="repository">queryable repository</param>
		/// <returns>found items</returns>
		public static TValue[] SearchAll<TValue>(this IQueryableRepository<TValue> repository)
			where TValue : IDataSource
		{
			Contract.Requires(repository != null);

			return repository.Search<TValue>(null, null, null);
		}
		/// <summary>
		/// Find objects by provided identifier
		/// </summary>
		/// <typeparam name="TValue">object type</typeparam>
		/// <param name="repository">repository to data</param>
		/// <param name="uri">object identifier</param>
		/// <returns>found object</returns>
		public static TValue Find<TValue>(this IRepository<TValue> repository, string uri)
			where TValue : IIdentifiable
		{
			Contract.Requires(repository != null);
			Contract.Requires(uri != null);

			var found = repository.Find(new[] { uri });
			if (found.Length > 0)
				return found[0];
			return default(TValue);
		}
		/// <summary>
		/// Insert new aggregate roots.
		/// Aggregates are modified in place.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="data">new aggregates</param>
		/// <returns>created indentifiers</returns>
		public static string[] Insert<TRoot>(this IPersistableRepository<TRoot> repository, IEnumerable<TRoot> data)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);

			if (data != null && data.Any())
				return repository.Persist(data, null, null);
			return new string[0];
		}
		/// <summary>
		/// Save changed aggregate roots.
		/// Aggregates are modified in place.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="data">aggregates to save</param>
		public static void Update<TRoot>(this IPersistableRepository<TRoot> repository, IEnumerable<TRoot> data)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);

			if (data != null && data.Any())
				repository.Persist(null, data, null);
		}
		/// <summary>
		/// Save changed aggregate roots.
		/// Aggregates are modified in place.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="data">collection of old and chaned aggregates</param>
		public static void Update<TRoot>(this IPersistableRepository<TRoot> repository, IEnumerable<KeyValuePair<TRoot, TRoot>> data)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);

			if (data != null && data.Any())
				repository.Persist(null, data, null);
		}
		/// <summary>
		/// Delete aggregate roots.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="data">aggregates to delete</param>
		public static void Delete<TRoot>(this IPersistableRepository<TRoot> repository, IEnumerable<TRoot> data)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);

			if (data != null && data.Any())
				repository.Persist(null, null, data);
		}
		/// <summary>
		/// Insert aggregate root.
		/// Return new identifier.
		/// Aggregate is modified in place.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="data">new aggregate</param>
		/// <returns>assigned identifier</returns>
		public static string Insert<TRoot>(this IPersistableRepository<TRoot> repository, TRoot data)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);
			Contract.Requires(data != null);

			return repository.Insert(new[] { data })[0];
		}
		/// <summary>
		/// Update changed aggregate root.
		/// Aggregate is modified in place.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="data">changed aggregate</param>
		public static void Update<TRoot>(this IPersistableRepository<TRoot> repository, TRoot data)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);

			repository.Update(new[] { data });
		}
		/// <summary>
		/// Update changed aggregate root.
		/// Aggregate is modified in place.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="oldValue">old aggregate instance</param>
		/// <param name="newValue">new aggregate instance</param>
		public static void Update<TRoot>(this IPersistableRepository<TRoot> repository, TRoot oldValue, TRoot newValue)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);
			Contract.Requires(oldValue != null);
			Contract.Requires(newValue != null);

			repository.Persist(null, new[] { new KeyValuePair<TRoot, TRoot>(oldValue, newValue) }, null);
		}
		/// <summary>
		/// Delete aggregate root.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="data">aggregate to delete</param>
		public static void Delete<TRoot>(this IPersistableRepository<TRoot> repository, TRoot data)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);
			Contract.Requires(data != null);

			repository.Delete(new[] { data });
		}
		/// <summary>
		/// Delete aggregate root defined by provided identifier.
		/// Deleted aggregate is returned.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="uri">aggregate identifier</param>
		/// <returns>deleted aggregate root</returns>
		public static TRoot Delete<TRoot>(this IPersistableRepository<TRoot> repository, string uri)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);
			Contract.Requires(uri != null);

			var oldValue = repository.Find(uri);
			if (oldValue == null)
				throw new ArgumentException(string.Format("Can't find {0} with URI: {1}", typeof(TRoot).FullName, uri));

			repository.Delete(new[] { oldValue });
			return oldValue;
		}
		//TODO: remove!?
		/// <summary>
		/// Change aggregate root defined by identifier using provided action.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="uri">aggregate identifier</param>
		/// <param name="update">change method</param>
		/// <returns>found and changed aggregate</returns>
		public static TRoot Update<TRoot>(this IPersistableRepository<TRoot> repository, string uri, Action<TRoot> update)
			where TRoot : IAggregateRoot
		{
			Contract.Requires(repository != null);
			Contract.Requires(uri != null);
			Contract.Requires(update != null);

			var found = repository.Find(uri);
			if (found == null)
				throw new ArgumentException(string.Format("Can't find {0} with URI: {1}", typeof(TRoot).FullName, uri));

			var ct = found as IChangeTracking<TRoot>;
			var original = ct != null
				? ct.GetOriginalValue()
				: found is ICloneable ? (TRoot)((ICloneable)found).Clone() : default(TRoot);
			update(found);
			repository.Persist(null, new[] { new KeyValuePair<TRoot, TRoot>(original, found) }, null);
			return found;
		}
	}
}
