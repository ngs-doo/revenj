using System;
using System.Collections.Generic;
using System.Data;
using System.Globalization;
using System.IO;
using System.Linq;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Report service will gather data from its specification.
	/// </summary>
	/// <typeparam name="TData">result type</typeparam>
	public interface IReport<TData>
	{
		/// <summary>
		/// Create result object.
		/// </summary>
		/// <param name="locator">service locator</param>
		/// <returns>populated result</returns>
		TData Populate(IServiceProvider locator);
	}
	/// <summary>
	/// Document report is a service for populating documents.
	/// </summary>
	/// <typeparam name="TData">population argument</typeparam>
	public interface IDocumentReport<TData>
	{
		/// <summary>
		/// Create document from provided data
		/// </summary>
		/// <param name="data">used for document population</param>
		/// <returns>populated document</returns>
		Stream Create(TData data);
	}

	/// <summary>
	/// Service for running queries against OLAP cube.
	/// Pick and choose interesting dimension and fact and run aggregation on them.
	/// </summary>
	public interface IOlapCubeQuery<TSource> where TSource : IDataSource
	{
		/// <summary>
		/// Available dimensions
		/// </summary>
		IEnumerable<string> Dimensions { get; }
		/// <summary>
		/// Available facts
		/// </summary>
		IEnumerable<string> Facts { get; }
		/// <summary>
		/// Run analysis on data. 
		/// Data will be grouped by specified dimensions.
		/// Aggregation will be run on specified facts.
		/// Data will be returned in specified order.
		/// Specification is used to filter only subset of data.
		/// </summary>
		/// <param name="dimensions">dimension subset</param>
		/// <param name="facts">fact subset</param>
		/// <param name="order">custom order</param>
		/// <param name="filter">predicate filter</param>
		/// <param name="limit">maximum number of rows</param>
		/// <param name="offset">how many initial rows to skip</param>
		/// <returns>result from created query</returns>
		DataTable Analyze(
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IEnumerable<KeyValuePair<string, bool>> order,
			ISpecification<TSource> filter,
			int? limit,
			int? offset);
	}
	/// <summary>
	/// Utility for easier use of OLAP cube
	/// </summary>
	public static class OlapCubeQueryHelper
	{
		/// <summary>
		/// Run analysis an all data.
		/// Data will be grouped by specified dimensions.
		/// Aggregation will be run on specified facts.
		/// Data will be returned in specified order.
		/// </summary>
		/// <param name="query">cube query</param>
		/// <param name="dimensions">dimension subset</param>
		/// <param name="facts">fact subset</param>
		/// <param name="order">custom order</param>
		/// <param name="limit">maximum number of row</param>
		/// <param name="offset">how many initial rows to skip</param>
		/// <returns>result from created query</returns>
		public static DataTable Analyze<T>(
			this IOlapCubeQuery<T> query,
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IEnumerable<KeyValuePair<string, bool>> order,
			int? limit,
			int? offset) where T : IDataSource
		{
			return query.Analyze(dimensions, facts, order, null, limit, offset);
		}
		/// <summary>
		/// Create OLAP cube builder with fluent API.
		/// </summary>
		/// <param name="query">OLAP cube</param>
		/// <returns>builder</returns>
		public static OlapCubeQueryBuilder<T> Builder<T>(this IOlapCubeQuery<T> query)
			where T : IDataSource
		{
			return new OlapCubeQueryBuilder<T>(query);
		}
	}
	/// <summary>
	/// Fluent interface for building OLAP query.
	/// </summary>
	public class OlapCubeQueryBuilder<T> where T : IDataSource
	{
		private readonly IOlapCubeQuery<T> Query;
		private readonly List<string> Dimensions = new List<string>();
		private readonly List<string> Facts = new List<string>();
		private int? ResultLimit;
		private int? ResultOffset;
		private readonly Dictionary<string, bool> Order = new Dictionary<string, bool>();

		/// <summary>
		/// Original OLAP cube query.
		/// </summary>
		/// <param name="query">OLAP cube query</param>
		public OlapCubeQueryBuilder(IOlapCubeQuery<T> query)
		{
			this.Query = query;
		}
		/// <summary>
		/// Use dimension or fact.
		/// Group by dimension or aggregate by fact.
		/// </summary>
		/// <param name="dimensionOrFact">dimension or fact</param>
		/// <returns>itself</returns>
		public OlapCubeQueryBuilder<T> Use(string dimensionOrFact)
		{
			if (Query.Dimensions.Contains(dimensionOrFact))
				Dimensions.Add(dimensionOrFact);
			else if (Query.Facts.Contains(dimensionOrFact))
				Facts.Add(dimensionOrFact);
			else
				throw new ArgumentException(
					string.Format(CultureInfo.InvariantCulture,
						"Unknown dimension or fact: {0}. Use Dimensions or Facts property for available dimensions and facts",
						dimensionOrFact));
			return this;
		}
		/// <summary>
		/// Use ascending order for specific dimension or fact.
		/// </summary>
		/// <param name="result">sort column</param>
		/// <returns>itself</returns>
		public OlapCubeQueryBuilder<T> Ascending(string result) { return OrderBy(result, true); }
		/// <summary>
		/// Use descending order for specific dimension or fact.
		/// </summary>
		/// <param name="result">sort column</param>
		/// <returns>itself</returns>
		public OlapCubeQueryBuilder<T> Descending(string result) { return OrderBy(result, false); }

		private OlapCubeQueryBuilder<T> OrderBy(string result, bool ascending)
		{
			if (!Query.Dimensions.Contains(result) && !Query.Facts.Contains(result))
				throw new ArgumentException(
					string.Format(CultureInfo.InvariantCulture,
						"Unknown result: {0}. Result can be only field from used dimensions and facts.",
						result));
			Order[result] = ascending;
			return this;
		}
		/// <summary>
		/// Limit maximum results returned from analysis.
		/// </summary>
		/// <param name="limit">maximum results</param>
		/// <returns>itself</returns>
		public OlapCubeQueryBuilder<T> Take(int limit) { return Limit(limit); }
		/// <summary>
		/// Limit maximum results returned from analysis.
		/// </summary>
		/// <param name="limit">maximum results</param>
		/// <returns>itself</returns>
		public OlapCubeQueryBuilder<T> Limit(int limit)
		{
			if (limit > 0)
				this.ResultLimit = limit;
			else throw new ArgumentException("Invalid limit value. Limit must be positive");
			return this;
		}
		/// <summary>
		/// Skip initial results returned from analysis.
		/// </summary>
		/// <param name="offset">skipped results</param>
		/// <returns>itself</returns>
		public OlapCubeQueryBuilder<T> Skip(int offset) { return Offset(offset); }
		/// <summary>
		/// Skip initial results returned from analysis.
		/// </summary>
		/// <param name="offset">skipped results</param>
		/// <returns>itself</returns>
		public OlapCubeQueryBuilder<T> Offset(int offset)
		{
			if (offset >= 0)
				this.ResultOffset = offset;
			else throw new ArgumentException("Invalid offset value. Offset must be positive");
			return this;
		}
		/// <summary>
		/// Load query data by running analysis on all data.
		/// </summary>
		/// <returns>aggregated information</returns>
		public DataTable Analyze() { return Query.Analyze(Dimensions, Facts, Order, ResultLimit, ResultOffset); }
		/// <summary>
		/// Load query data by running analysis on subset of data.
		/// Specification predicate is used to filter data.
		/// </summary>
		/// <param name="specification">predicate filter</param>
		/// <returns>aggregated information</returns>
		public DataTable Analyze(ISpecification<T> specification)
		{
			return Query.Analyze(Dimensions, Facts, Order, specification, ResultLimit, ResultOffset);
		}
	}
}
