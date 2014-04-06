using System;
using System.Collections.Generic;
using System.Linq;

namespace NGS.DomainPatterns
{
	/// <summary>
	/// Access to data. Proxy to various features, such as 
	/// repositories, reports, notifications, olap, validation...
	/// Cache will be used if available.
	/// Data is available using current scope.
	/// If transaction is used, changes will be visible to other scopes only after commit.
	/// </summary>
	public interface IDataContext
	{
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
		/// <returns>linq projection</returns>
		IQueryable<T> Query<T>() where T : IDataSource;
		/// <summary>
		/// Create new aggregate roots. 
		/// Data will be sent immediatelly to the backing store.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="aggregates">new aggregates</param>
		void Create<T>(T[] aggregates) where T : IAggregateRoot;
		/// <summary>
		/// Update existing aggregate roots. Provide pair of old and new.
		/// Old value is optional. Change track value will be used on null values.
		/// Lookup to DB will be performed if neither old value or change track value is available.
		/// Data will be sent immediatelly to the backing store.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="pairs">aggregate pairs</param>
		void Update<T>(KeyValuePair<T, T>[] pairs) where T : IAggregateRoot;
		/// <summary>
		/// Delete existing aggregate roots.
		/// Data will be sent immediatelly to the backing store.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="aggregates">remove provided aggregate roots</param>
		void Delete<T>(T[] aggregates) where T : IAggregateRoot;
		/// <summary>
		/// Raise domain events
		/// </summary>
		/// <typeparam name="T">event type</typeparam>
		/// <param name="events">domain events</param>
		void Submit<T>(T[] events) where T : IDomainEvent;
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
		IHistory<T>[] History<T>(string[] uris) where T : IObjectHistory;
		/// <summary>
		/// OLAP cube builder. Data analysis using dimensions and facts
		/// </summary>
		/// <typeparam name="T">cube type</typeparam>
		/// <returns>cube builder</returns>
		OlapCubeQueryBuilder CubeBuilder<T>() where T : IOlapCubeQuery;
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
		/// Find identifiable object using provided uri
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="uri">search by uri</param>
		/// <returns>found object or null</returns>
		public static T Find<T>(this IDataContext context, string uri) where T : IIdentifiable
		{
			var res = context.Find<T>(new[] { uri });
			if (res.Length == 1)
				return res[0];
			return default(T);
		}
		/// <summary>
		/// Create new aggregate root.
		/// Data will be sent immediatelly to the backing store.
		/// </summary>
		/// <typeparam name="T">aggregate type</typeparam>
		/// <param name="context">data context</param>
		/// <param name="root">root instance</param>
		public static void Create<T>(this IDataContext context, T root) where T : IAggregateRoot
		{
			context.Create(new[] { root });
		}
		/// <summary>
		/// Update existing aggregate root.
		/// Data will be sent immediatelly to the backing store.
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
		/// Data will be sent immediatelly to the backing store.
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
		/// Data will be sent immediatelly to the backing store.
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
		/// <param name="uri">root uri</param>
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
	}
}
