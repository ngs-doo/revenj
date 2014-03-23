using System.ComponentModel.Composition;
using System.Linq.Expressions;
using NGS.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ResultOperators;

namespace NGS.Plugins.DatabasePersistence.Postgres.QuerySimplifications
{
	[Export(typeof(IQuerySimplification))]
	public class ArraySizeSubquery : IQuerySimplification
	{
		public bool CanSimplify(QueryParts query)
		{
			var parts = query as SubqueryParts;
			var me = parts != null ? parts.MainFrom.FromExpression as MemberExpression : null;
			return parts != null
				&& me != null
				&& !parts.ShouldQueryInMemory
				&& parts.ResultOperators.Count == 1
				&& parts.Joins.Count == 0 && parts.AdditionalJoins.Count == 0 && parts.Conditions.Count == 0
				&& (me.Expression is QuerySourceReferenceExpression
					|| me.Expression is ParameterExpression)
				&& me.Type.IsSupportedCollectionType()
				&& (parts.ResultOperators[0] is CountResultOperator
					|| parts.ResultOperators[0] is LongCountResultOperator);
		}

		public string Simplify(QueryParts query)
		{
			var parts = query as SubqueryParts;

			var me = parts.MainFrom.FromExpression as MemberExpression;
			var qsre = me.Expression as QuerySourceReferenceExpression;
			var pe = me.Expression as ParameterExpression;
			if (me.Type == typeof(byte[]))
			{
				return @"COALESCE(octet_length(({2}""{0}"").""{1}""), 0)".With(
					qsre != null ? qsre.ReferencedQuerySource.ItemName : pe.Name,
					me.Member.Name,
					pe != null ? query.ContextName : string.Empty);
			}
			return @"COALESCE(array_upper(({2}""{0}"").""{1}"", 1), 0)".With(
				qsre != null ? qsre.ReferencedQuerySource.ItemName : pe.Name,
				me.Member.Name,
				pe != null ? query.ContextName : string.Empty);
		}
	}
}
