using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics.Contracts;
using System.Linq;

namespace Revenj.DomainPatterns
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
		/// Find objects by identity
		/// </summary>
		/// <param name="uri">object identity</param>
		/// <returns>found object, null if not found</returns>
		TValue Find(string uri);
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
		/// If specification is provided it must be compatible with data type.
		/// This is only a projection, actual query will be done after materialization from IQueryable&lt;TValue&gt;
		/// </summary>
		/// <typeparam name="TCondition">specification type</typeparam>
		/// <param name="specification">filter predicate</param>
		/// <returns>data projection</returns>
		IQueryable<TValue> Query<TCondition>(ISpecification<TCondition> specification);
		/// <summary>
		/// Search data using provided specification.
		/// If specification is provided it must be compatible with data type.
		/// </summary>
		/// <typeparam name="TCondition">specification type</typeparam>
		/// <param name="specification">filter predicate</param>
		/// <param name="limit">limit maximum number of results</param>
		/// <param name="offset">skip initial results</param>
		/// <returns>found items</returns>
		TValue[] Search<TCondition>(ISpecification<TCondition> specification, int? limit, int? offset);
		/// <summary>
		/// Count data using provided specification.
		/// If specification is provided it must be compatible with data type.
		/// </summary>
		/// <typeparam name="TCondition">specification type</typeparam>
		/// <param name="specification">filter predicate</param>
		/// <returns>total found items</returns>
		long Count<TCondition>(ISpecification<TCondition> specification);
		/// <summary>
		/// Check for data using provided specification.
		/// If specification is provided it must be compatible with data type.
		/// </summary>
		/// <typeparam name="TCondition">specification type</typeparam>
		/// <param name="specification">filter predicate</param>
		/// <returns>data has been found</returns>
		bool Exists<TCondition>(ISpecification<TCondition> specification);
	}
	/// <summary>
	/// Aggregate root persistable repository.
	/// Besides querying capabilities, repository has set based API for persistence.
	/// </summary>
	/// <typeparam name="TRoot">aggregate root type</typeparam>
	public interface IPersistableRepository<TRoot> : IQueryableRepository<TRoot>, IRepository<TRoot>
		where TRoot : IAggregateRoot
	{
		/// <summary>
		/// Persist aggregate roots. Bulk persistence.
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
	/// Provides bulk access to DB operations (if supported by implementation)
	/// </summary>
	public interface IRepositoryBulkReader
	{
		/// <summary>
		/// Reset reader for new queries.
		/// </summary>
		/// <param name="usePrepared">use prepared statements</param>
		void Reset(bool usePrepared);
		/// <summary>
		/// Find object by identity
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="uri">string primary key representation</param>
		/// <returns>object which was found</returns>
		Lazy<T> Find<T>(string uri) where T : IIdentifiable;
		/// <summary>
		/// Find objects by identity
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="uris">primary keys as strings</param>
		/// <returns>found objects</returns>
		Lazy<T[]> Find<T>(IEnumerable<string> uris) where T : IIdentifiable;
		/// <summary>
		/// Search for objects using provided specification
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="filter">search filter</param>
		/// <param name="limit">max results</param>
		/// <param name="offset">skip initial results</param>
		/// <returns>found objects</returns>
		Lazy<T[]> Search<T>(ISpecification<T> filter, int? limit, int? offset) where T : IDataSource;
		/// <summary>
		/// Count domain object using provided specification
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="filter">search filter</param>
		/// <returns>total found objects</returns>
		Lazy<long> Count<T>(ISpecification<T> filter) where T : IDataSource;
		/// <summary>
		/// Check if objects exists using provided specification
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="filter">search filter</param>
		/// <returns>objects exists</returns>
		Lazy<bool> Exists<T>(ISpecification<T> filter) where T : IDataSource;
		/// <summary>
		/// Run OLAP analysis on objects using provided specification
		/// </summary>
		/// <typeparam name="TCube">cube type</typeparam>
		/// <typeparam name="TSource">cube data source type</typeparam>
		/// <param name="dimensionsAndFacts">group by dimensions, analyze by facts</param>
		/// <param name="order">provide result in specific order</param>
		/// <param name="filter">search filter</param>
		/// <param name="limit">max results</param>
		/// <param name="offset">skip initial results</param>
		/// <returns>found objects</returns>
		Lazy<DataTable> Analyze<TCube, TSource>(
			IEnumerable<string> dimensionsAndFacts,
			IEnumerable<KeyValuePair<string, bool>> order,
			ISpecification<TSource> filter,
			int? limit,
			int? offset)
			where TCube : IOlapCubeQuery<TSource>
			where TSource : IDataSource;
		/// <summary>
		/// Execute queries
		/// </summary>
		void Execute();
	}
	/// <summary>
	/// Utility for easier usage of repositories.
	/// </summary>
	public static class RepositoryHelper
	{
		/// <summary>
		/// Persist aggregate roots. Bulk persistence.
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
		public static IQueryable<TValue> Query<TValue>(this IQueryableRepository<TValue> repository)
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
		public static TValue[] Search<TValue>(this IQueryableRepository<TValue> repository)
			where TValue : IDataSource
		{
			Contract.Requires(repository != null);

			return repository.Search<TValue>(null, null, null);
		}
		/// <summary>
		/// Count all data of specified type.
		/// </summary>
		/// <typeparam name="TValue">data type</typeparam>
		/// <param name="repository">queryable repository</param>
		/// <returns>total found items</returns>
		public static long Count<TValue>(this IQueryableRepository<TValue> repository)
			where TValue : IDataSource
		{
			Contract.Requires(repository != null);

			return repository.Count<TValue>(null);
		}
		/// <summary>
		/// Check for any data of specified type.
		/// </summary>
		/// <typeparam name="TValue">data type</typeparam>
		/// <param name="repository">queryable repository</param>
		/// <returns>items has been found</returns>
		public static bool Exists<TValue>(this IQueryableRepository<TValue> repository)
			where TValue : IDataSource
		{
			Contract.Requires(repository != null);

			return repository.Exists<TValue>(null);
		}
		/// <summary>
		/// Insert new aggregate roots.
		/// Aggregates are modified in place.
		/// </summary>
		/// <typeparam name="TRoot">aggregate type</typeparam>
		/// <param name="repository">persistable repository</param>
		/// <param name="data">new aggregates</param>
		/// <returns>created identifiers</returns>
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
		/// <param name="data">collection of old and changed aggregates</param>
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

			var ct = data as IChangeTracking<TRoot>;
			repository.Persist(null, new[] { new KeyValuePair<TRoot, TRoot>(ct != null ? ct.GetOriginalValue() : default(TRoot), data) }, null);
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
		/// <summary>
		/// Run OLAP analysis on objects using provided specification
		/// </summary>
		/// <typeparam name="TCube">cube type</typeparam>
		/// <typeparam name="TSource">cube data source type</typeparam>
		/// <param name="bulk">bulk reader</param>
		/// <param name="dimensionsAndFacts">group by dimensions, analyze by facts</param>
		/// <returns>found objects</returns>
		public static Lazy<DataTable> Analyze<TCube, TSource>(this IRepositoryBulkReader bulk, IEnumerable<string> dimensionsAndFacts)
			where TCube : IOlapCubeQuery<TSource>
			where TSource : IDataSource
		{
			return bulk.Analyze<TCube, TSource>(dimensionsAndFacts, null, null, null, null);
		}
	}
}
