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
	public class SumProjection : IProjectionMatcher
	{
		public bool TryMatch(Expression expression, MainQueryParts queryParts, Action<Expression> visitExpression, QueryContext context)
		{
			var sq = expression as SubQueryExpression;
			return sq != null
				&& sq.QueryModel.ResultOperators.Count == 1
				&& sq.QueryModel.ResultOperators[0] is SumResultOperator
				&& CheckShortCircuitSum(sq, queryParts, visitExpression);
		}

		private static bool CheckShortCircuitSum(SubQueryExpression expression, MainQueryParts queryParts, Action<Expression> visitExpression)
		{
			var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(expression.QueryModel, queryParts, true);
			if (subquery.ShouldQueryInMemory)
			{
				throw new NotImplementedException("Unsupported subquery. Please provide more info about query.");
				//return false;
			}

			var sql = subquery.BuildSqlString(false);
			var cnt = queryParts.CurrentSelectIndex;

			var selector = expression.QueryModel.SelectClause.Selector as MemberExpression;
			if (selector == null)
				return false;

			var type = expression.QueryModel.ResultTypeOverride;
			if (type.IsNullable())
				type = type.GetGenericArguments()[0];

			queryParts.AddSelectPart(
				expression.QueryModel.MainFromClause,
				@"(SELECT SUM((""{1}"").""{3}"") FROM ({2}) ""{1}"") AS ""{0}""".With(
					"_subquery_" + cnt,
					expression.QueryModel.MainFromClause.ItemName,
					sql,
					queryParts.ConverterFactory.GetName(selector.Member)),
				"_sum_" + cnt,
				expression.QueryModel.ResultTypeOverride,
				(_, __, dr) => dr.IsDBNull(cnt) ? 0 : Convert.ChangeType(dr.GetValue(cnt), type));
			return true;
		}
	}
}
