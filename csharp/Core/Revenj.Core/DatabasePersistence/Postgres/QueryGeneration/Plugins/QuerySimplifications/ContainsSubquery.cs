using System;
using System.Collections;
using System.ComponentModel.Composition;
using System.Linq;
using System.Linq.Expressions;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ResultOperators;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Postgres.Plugins.QuerySimplifications
{
	[Export(typeof(IQuerySimplification))]
	public class ContainsSubquery : IQuerySimplification
	{
		public bool CanSimplify(QueryParts query)
		{
			var parts = query as SubqueryParts;
			var ce = parts != null ? parts.MainFrom.FromExpression as ConstantExpression : null;
			var me = parts != null ? parts.MainFrom.FromExpression as MemberExpression : null;
			var pe = parts != null ? parts.MainFrom.FromExpression as ParameterExpression : null;
			return parts != null
				&& (ce != null && ce.Type.IsSupportedCollectionType()
					|| me != null
					|| pe != null)
				&& !parts.ShouldQueryInMemory
				&& parts.ResultOperators.Count == 1
				&& parts.Joins.Count == 0 && parts.AdditionalJoins.Count == 0 && parts.Conditions.Count == 0
				&& parts.ResultOperators[0] is ContainsResultOperator;
		}

		public string Simplify(QueryParts query)
		{
			var parts = query as SubqueryParts;
			var containsResult = parts.ResultOperators[0] as ContainsResultOperator;

			var exp = parts.GetSqlExpression(containsResult.Item);
			var ce = parts.MainFrom.FromExpression as ConstantExpression;
			var pe = parts.MainFrom.FromExpression as ParameterExpression;
			var ma = parts.MainFrom.FromExpression as MemberExpression;
			return ce != null ? SimplifyConstantExpression(query, parts, exp, ce)
				: ma != null ? SimplifyExpression(query, parts, exp, ma)
				: pe != null ? SimplifyExpression(query, parts, exp, pe)
				: exp + " = ANY(" + parts.GetSqlExpression(parts.MainFrom.FromExpression) + ")";
		}

		private string SimplifyConstantExpression(QueryParts query, SubqueryParts parts, string exp, ConstantExpression ce)
		{
			if (ce.Type.IsArray || ce.Value is Array)
			{
				var array = ((Array)ce.Value).Cast<object>().ToArray();
				return FormatInQuery(exp, array, parts);
			}
			else if (ce.Value is IEnumerable)
			{
				var array = ((IEnumerable)ce.Value).Cast<object>().ToArray();
				return FormatInQuery(exp, array, parts);
			}
			return exp + " = " + query.FormatObject(ce.Value);
		}

		private string SimplifyExpression(QueryParts query, SubqueryParts parts, string exp, Expression e)
		{
			var source = query.GetSqlExpression(e);
			var qsre = parts.Selector as QuerySourceReferenceExpression;
			if (qsre != null && qsre.ReferencedQuerySource.Equals(query.MainFrom))
				return exp + " = ANY(" + source + ")";
			return exp + " IN (SELECT " + query.GetSqlExpression(parts.Selector) + " FROM unnest(" + source + ") \"" + query.MainFrom.ItemName + "\")";
		}

		private string FormatInQuery(string exp, object[] array, SubqueryParts query)
		{
			if (array.Length == 0)
				return "false";
			if (array.Length == 1 && array[0] == null)
				return exp + " IS NULL ";

			var qsre = query.Selector as QuerySourceReferenceExpression;
			if (qsre != null && qsre.ReferencedQuerySource.Equals(query.MainFrom)
				&& Revenj.DatabasePersistence.Postgres.NpgsqlTypes.TypeConverter.CanConvert(query.MainFrom.ItemType))
			{
				//TODO: values
				return array.Length == 1
					? exp + " = " + Revenj.DatabasePersistence.Postgres.NpgsqlTypes.TypeConverter.Convert(query.MainFrom.ItemType, array[0])
					: exp + " IN ("
						+ string.Join(
							",",
							array.Select(it => Revenj.DatabasePersistence.Postgres.NpgsqlTypes.TypeConverter.Convert(query.MainFrom.ItemType, it)))
							+ ")";
			}

			return exp + " IN (SELECT " + query.GetSqlExpression(query.Selector)
					+ " FROM unnest(" + query.FormatArray(array) + ") \"" + query.MainFrom.ItemName + "\")";
		}
	}
}
