using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Specification is predicate which states a condition.
	/// It can be used to filter data which satisfy defined condition.
	/// </summary>
	/// <typeparam name="TValue">specification type</typeparam>
	public interface ISpecification<TValue>
	{
		/// <summary>
		/// Expression for testing specified condition.
		/// </summary>
		Expression<Func<TValue, bool>> IsSatisfied { get; }
	}
	/// <summary>
	/// Utility for applying specification on data.
	/// </summary>
	public static class SpecificationHelper
	{
		/// <summary>
		/// Apply specification on data projection.
		/// Specification must be compatible with data.
		/// </summary>
		/// <typeparam name="TSource">data type</typeparam>
		/// <typeparam name="TFilter">specification type</typeparam>
		/// <param name="source">data projection</param>
		/// <param name="filter">specification filter predicate</param>
		/// <returns>filtered data projection</returns>
		public static IQueryable<TSource> Filter<TSource, TFilter>(this IQueryable<TSource> source, ISpecification<TFilter> filter)
		{
			return typeof(TSource) == typeof(TFilter)
				? source.Where(((ISpecification<TSource>)filter).IsSatisfied)
				: source.Cast<TFilter>().Where(filter.IsSatisfied).Cast<TSource>();
		}
		/// <summary>
		/// Check if any item satisfies specification.
		/// </summary>
		/// <typeparam name="TSource">data and specification type</typeparam>
		/// <param name="specification">filter predicate</param>
		/// <param name="items">data collection</param>
		/// <returns>does any item satisfies specification</returns>
		public static bool IsSatisfiedBy<TSource>(this ISpecification<TSource> specification, IEnumerable<TSource> items)
		{
			return items.AsQueryable().Where(specification.IsSatisfied).Any();
		}
		/// <summary>
		/// Is specification satisfied by provided object.
		/// </summary>
		/// <typeparam name="TSource">object and specification type</typeparam>
		/// <param name="specification">condition</param>
		/// <param name="item">object to check</param>
		/// <returns>specification satisfied</returns>
		public static bool IsSatisfiedBy<TSource>(this ISpecification<TSource> specification, TSource item)
		{
			return specification.IsSatisfiedBy(new[] { item });
		}
	}
}
