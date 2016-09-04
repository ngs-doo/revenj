using System;
using System.ComponentModel.Composition;
using System.Linq.Expressions;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ResultOperators;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Postgres.Plugins.ProjectionSimplifications
{
	[Export(typeof(IProjectionMatcher))]
	public class CountProjection : IProjectionMatcher
	{
		public bool TryMatch(Expression expression, MainQueryParts queryParts, Action<Expression> visitExpression, QueryContext context)
		{
			var sq = expression as SubQueryExpression;
			return sq != null
				&& sq.QueryModel.ResultOperators.Count == 1
				&& (sq.QueryModel.ResultOperators[0] is CountResultOperator
				|| sq.QueryModel.ResultOperators[0] is LongCountResultOperator)
				&& CheckShortCircuitCount(sq, queryParts, visitExpression);
		}

		private static bool CheckShortCircuitCount(SubQueryExpression expression, MainQueryParts queryParts, Action<Expression> visitExpression)
		{
			var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(expression.QueryModel, queryParts, true);
			if (subquery.ShouldQueryInMemory)
			{
				throw new NotImplementedException("Unsupported subquery. Please provide more info about query.");
				//return false;
			}

			var cnt = queryParts.CurrentSelectIndex;

			var mq = subquery.MainFrom.FromExpression as QuerySourceReferenceExpression;
			if (mq != null && subquery.Joins.Count == 0 && subquery.AdditionalJoins.Count == 0 && subquery.Conditions.Count == 0)
			{
				if (mq.ReferencedQuerySource.ItemType.IsGrouping())
				{
					queryParts.AddSelectPart(
						expression.QueryModel.MainFromClause,
						"ARRAY_UPPER(\"{0}\".\"Values\", 1) AS \"_count_helper_{1}\"".With(mq.ReferencedQuerySource.ItemName, cnt),
						"_count_helper_" + cnt,
						expression.QueryModel.ResultTypeOverride,
						(_, __, dr) => dr.IsDBNull(cnt) ? 0 : Convert.ChangeType(dr.GetValue(cnt), expression.QueryModel.ResultTypeOverride));
					return true;
				}
			}

			var sql = subquery.BuildSqlString(false);

			queryParts.AddSelectPart(
				expression.QueryModel.MainFromClause,
				@"(SELECT COUNT(""{1}"") FROM ({2}) ""{1}"") AS ""{0}"" ".With(
					"_subquery_" + cnt,
					expression.QueryModel.MainFromClause.ItemName,
					sql),
				"_count_" + cnt,
				expression.QueryModel.ResultTypeOverride,
				(_, __, dr) => dr.IsDBNull(cnt) ? 0 : Convert.ChangeType(dr.GetValue(cnt), expression.QueryModel.ResultTypeOverride));
			return true;
		}
	}
}
