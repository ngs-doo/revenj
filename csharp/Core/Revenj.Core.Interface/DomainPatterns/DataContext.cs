using System;
using System.Collections.Generic;
using System.Linq;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Access to data. Proxy to various features, such as 
	/// repositories, reports, notifications, OLAP, validation...
	/// Cache will be used if available.
	/// Data is available using current scope.
	/// If transaction is used, changes will be visible to other scopes only after commit.
	/// </summary>
	public interface IDataContext
	{
		/// <summary>
		/// Find identifiable data from provided URI
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="uri">identity</param>
		/// <returns>found value</returns>
		T Find<T>(string uri) where T : IIdentifiable;
		/// <summary>
		/// Find identifiable data from provided URIs
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="uris">identification</param>
		/// <returns>found values</returns>
		T[] Find<T>(IEnumerable<string> uris) where T : IIdentifiable;
		/// <summary>
		/// LINQ queries to data
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <returns>LINQ projection</returns>
		IQueryable<T> Query<T>() where T : IDataSource;
		/// <summary>
		/// Search data using provided specification.
		/// Specification is optional.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="filter">filter predicate</param>
		/// <param name="limit">limit maximum number of results</param>
		/// <param name="offset">skip initial results</param>
		/// <returns>found items</returns>
		T[] Search<T>(ISpecification<T> filter, int? limit, int? offset) where T : IDataSource;
		/// <summary>
		/// Count data using provided specification.
		/// Specification is optional.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="filter">filter predicate</param>
		/// <returns>total found items</returns>
		long Count<T>(ISpecification<T> filter) where T : IDataSource;
		/// <summary>
		/// Check if data exists using provided specification.
		/// Specification is optional.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="filter">filter predicate</param>
		/// <returns>items exists</returns>
		bool Exists<T>(ISpecification<T> filter) where T : IDataSource;
		/// <summary>
		/// Create new aggregate roots. 
		/// Data will be sent immediately to the backing store.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="aggregates">new aggregates</param>
		void Create<T>(IEnumerable<T> aggregates) where T : IAggregateRoot;
		/// <summary>
		/// Update existing aggregate roots. Provide pair of old and new.
		/// Old value is optional. Change track value will be used on null values.
		/// Lookup to DB will be performed if neither old value or change track value is available.
		/// Data will be sent immediately to the backing store.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="pairs">aggregate pairs</param>
		void Update<T>(IEnumerable<KeyValuePair<T, T>> pairs) where T : IAggregateRoot;
		/// <summary>
		/// Delete existing aggregate roots.
		/// Data will be sent immediately to the backing store.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="aggregates">remove provided aggregate roots</param>
		void Delete<T>(IEnumerable<T> aggregates) where T : IAggregateRoot;
		/// <summary>
		/// Raise domain events (within current transaction)
		/// If currently inside transaction and transaction is rolled back, event will not be saved
		/// </summary>
		/// <typeparam name="T">event type</typeparam>
		/// <param name="events">domain events</param>
		void Submit<T>(IEnumerable<T> events) where T : IDomainEvent;
		/// <summary>
		/// Queue domain event for out-of-transaction submission to the store
		/// If error happens during submission (loss of power, DB connection problems, event will be lost)
		/// If current transaction is rolled back, event will still be persisted
		/// </summary>
		/// <typeparam name="T">event type</typeparam>
		/// <param name="events">domain events</param>
		void Queue<T>(IEnumerable<T> events) where T : IDomainEvent;
		/// <summary>
		/// Populate report
		/// </summary>
		/// <typeparam name="T">report type</typeparam>
		/// <param name="report">arguments for report</param>
		/// <returns>populated report</returns>
		T Populate<T>(IReport<T> report);
		/// <summary>
		/// Change tracking.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <returns>notifications</returns>
		IObservable<NotifyInfo> Track<T>() where T : IIdentifiable;
		/// <summary>
		/// History for aggregate root changes.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="uris">identifiers</param>
		/// <returns>found history</returns>
		IHistory<T>[] History<T>(IEnumerable<string> uris) where T : IObjectHistory;
		/// <summary>
		/// OLAP cube builder. Data analysis using dimensions and facts
		/// </summary>
		/// <typeparam name="TCube">cube type</typeparam>
		/// <typeparam name="TSource">cube source type</typeparam>
		/// <returns>cube builder</returns>
		OlapCubeQueryBuilder<TSource> CubeBuilder<TCube, TSource>()
			where TCube : IOlapCubeQuery<TSource>
			where TSource : IDataSource;
		/// <summary>
		/// Data which fails specified validation.
		/// Filtered using provided specification.
		/// </summary>
		/// <typeparam name="TValidation">validation type</typeparam>
		/// <typeparam name="TResult">validation target</typeparam>
		/// <param name="specification">search only subset of data</param>
		/// <returns>found invalid items</returns>
		TResult[] InvalidItems<TValidation, TResult>(ISpecification<TResult> specification)
			where TValidation : IValidation<TResult>
			where TResult : IIdentifiable;
	}
	/// <summary>
	/// Data context helper methods
	/// </summary>
	public static class DataContextHelper
	{
		/// <summary>
		/// Search data using provided specification.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="filter">filter predicate</param>
		/// <returns>found items</returns>
		public static T[] Search<T>(this IDataContext context, ISpecification<T> filter) where T : IDataSource
		{
			return context.Search(filter, null, null);
		}
		/// <summary>
		/// Search all data.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="context">data context</param>
		/// <returns>found items</returns>
		public static T[] Search<T>(this IDataContext context) where T : IDataSource
		{
			return context.Search((ISpecification<T>)null, null, null);
		}
		/// <summary>
		/// Count all data.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="context">data context</param>
		/// <returns>total found items</returns>
		public static long Count<T>(this IDataContext context) where T : IDataSource
		{
			return context.Count((ISpecification<T>)null);
		}
		/// <summary>
		/// Data exists.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="context">data context</param>
		/// <returns>items exists</returns>
		public static bool Exists<T>(this IDataContext context) where T : IDataSource
		{
			return context.Exists((ISpecification<T>)null);
		}
		/// <summary>
		/// Create new aggregate root.
		/// Data will be sent immediately to the backing store.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="root">root instance</param>
		public static void Create<T>(this IDataContext context, T root) where T : IAggregateRoot
		{
			context.Create(new[] { root });
		}
		/// <summary>
		/// Bulk update existing aggregate roots.
		/// Data will be sent immediately to the backing store.
		/// Change tracking value will be used if available.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="aggregates">aggregate root instances</param>
		public static void Update<T>(this IDataContext context, IEnumerable<T> aggregates) where T : IAggregateRoot
		{
			var pairs = new KeyValuePair<T, T>[aggregates.Count()];
			int i = 0;
			foreach (var agg in aggregates)
			{
				var ct = agg as IChangeTracking<T>;
				pairs[i++] = new KeyValuePair<T, T>(ct != null ? ct.GetOriginalValue() : default(T), agg);
			}
			context.Update(pairs);
		}
		/// <summary>
		/// Bulk update existing aggregate roots.
		/// Data will be sent immediately to the backing store.
		/// Change tracking value will be used if available.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="aggregates">aggregate root instances</param>
		public static void Update<T>(this IDataContext context, T[] aggregates) where T : IAggregateRoot
		{
			var pairs = new KeyValuePair<T, T>[aggregates.Length];
			for (int i = 0; i < aggregates.Length; i++)
			{
				var agg = aggregates[i];
				var ct = agg as IChangeTracking<T>;
				pairs[i] = new KeyValuePair<T, T>(ct != null ? ct.GetOriginalValue() : default(T), agg);
			}
			context.Update(pairs);
		}
		/// <summary>
		/// Bulk update existing aggregate roots.
		/// Data will be sent immediately to the backing store.
		/// Change tracking value will be used if available.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="aggregates">aggregate root instances</param>
		public static void Update<T>(this IDataContext context, List<T> aggregates) where T : IAggregateRoot
		{
			var pairs = new KeyValuePair<T, T>[aggregates.Count];
			for (int i = 0; i < aggregates.Count; i++)
			{
				var agg = aggregates[i];
				var ct = agg as IChangeTracking<T>;
				pairs[i] = new KeyValuePair<T, T>(ct != null ? ct.GetOriginalValue() : default(T), agg);
			}
			context.Update(pairs);
		}
		/// <summary>
		/// Update existing aggregate root.
		/// Data will be sent immediately to the backing store.
		/// Change tracking value will be used if available.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="root">root instance</param>
		public static void Update<T>(this IDataContext context, T root) where T : IAggregateRoot
		{
			var ct = root as IChangeTracking<T>;
			context.Update(new[] { new KeyValuePair<T, T>(ct != null ? ct.GetOriginalValue() : default(T), root) });
		}
		/// <summary>
		/// Update existing aggregate root.
		/// Data will be sent immediately to the backing store.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="oldRoot">old instance</param>
		/// <param name="newRoot">new instance</param>
		public static void Update<T>(this IDataContext context, T oldRoot, T newRoot) where T : IAggregateRoot
		{
			context.Update(new[] { new KeyValuePair<T, T>(oldRoot, newRoot) });
		}
		/// <summary>
		/// Delete existing aggregate root.
		/// Data will be sent immediately to the backing store.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="root">root instance</param>
		public static void Delete<T>(this IDataContext context, T root) where T : IAggregateRoot
		{
			context.Delete(new[] { root });
		}
		/// <summary>
		/// Find history for provided aggregate root.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="uri">root URI</param>
		/// <returns>found history</returns>
		public static IHistory<T> History<T>(this IDataContext context, string uri) where T : IObjectHistory
		{
			var res = context.History<T>(new[] { uri });
			if (res.Length == 1)
				return res[0];
			return null;
		}
		/// <summary>
		/// Data which fails specified validation.
		/// </summary>
		/// <typeparam name="TValidation">validation type</typeparam>
		/// <typeparam name="TResult">validation target</typeparam>
		/// <returns>found invalid items</returns>
		public static TResult[] InvalidItems<TValidation, TResult>(this IDataContext context)
			where TValidation : IValidation<TResult>
			where TResult : IIdentifiable
		{
			return context.InvalidItems<TValidation, TResult>(null);
		}
		/// <summary>
		/// Submit single Domain Event
		/// If currently inside transaction and transaction is rolled back, event will not be saved
		/// </summary>
		/// <typeparam name="TEvent">domain event type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="domainEvent">domain event</param>
		public static void Submit<TEvent>(this IDataContext context, TEvent domainEvent)
			where TEvent : IDomainEvent
		{
			context.Submit(new[] { domainEvent });
		}
		/// <summary>
		/// Queue domain event for out-of-transaction submission to the store
		/// If error happens during submission (loss of power, DB connection problems, event will be lost)
		/// If current transaction is rolled back, event will still be persisted
		/// </summary>
		/// <typeparam name="TEvent">domain event type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="domainEvent">domain event</param>
		public static void Queue<TEvent>(this IDataContext context, TEvent domainEvent)
			where TEvent : IDomainEvent
		{
			context.Queue(new[] { domainEvent });
		}
	}
}
