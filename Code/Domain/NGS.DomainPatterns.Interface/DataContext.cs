using System;
using System.Collections.Generic;
using System.Linq;

namespace NGS.DomainPatterns
{
	[Obsolete("not implemented yet")]
	public interface IDataContext : IDisposable
	{
		T[] Find<T>(string[] uris) where T : IIdentifiable;
		//TODO better api for order
		TResult[] Search<TResult, TFilter>(ISpecification<TFilter> specification, int? limit, int? offset, IEnumerable<KeyValuePair<string, bool>> order);
		long Count<TTarget, TFilter>(ISpecification<TFilter> specification);
		//TODO: collection
		void Submit<T>(T root) where T : IAggregateRoot;
		//TODO: collection
		void Delete<T>(T root) where T : IAggregateRoot;
		//TODO: collection
		void Raise<T>(T domainEvent) where T : IDomainEvent;
		T Populate<T>(IReport<T> report);
		void SaveChanges();
	}

	public static class DataContextHelper
	{
		public static T Find<T>(this IDataContext context, string uri) where T : IIdentifiable
		{
			return context.Find<T>(new[] { uri }).FirstOrDefault();
		}
		public static T[] Search<T>(this IDataContext context, ISpecification<T> specification, int? limit, int? offset, IEnumerable<KeyValuePair<string, bool>> order)
		{
			return context.Search<T, T>(specification, limit, offset, order);
		}
		public static T[] Search<T>(this IDataContext context, ISpecification<T> specification, int? limit, int? offset)
		{
			return context.Search<T, T>(specification, limit, offset, null);
		}
		public static T[] Search<T>(this IDataContext context, ISpecification<T> specification)
		{
			return context.Search<T, T>(specification, null, null, null);
		}
		public static T[] FindAll<T>(this IDataContext context)
		{
			return context.Search<T, T>(null, null, null, null);
		}
		public static long Count<T>(this IDataContext context, ISpecification<T> specification)
		{
			return context.Count<T, T>(specification);
		}
		public static long CountAll<T>(this IDataContext context)
		{
			return context.Count<T, T>(null);
		}
	}
}
