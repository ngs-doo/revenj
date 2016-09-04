using System;
using System.Linq.Expressions;
using Remotion.Linq;
using Remotion.Linq.Clauses;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors
{
	public class SubqueryGeneratorQueryModelVisitor : QueryModelVisitorBase
	{
		public static SubqueryParts ParseSubquery(QueryModel queryModel, QueryParts parentQuery)
		{
			return ParseSubquery(queryModel, parentQuery, false, parentQuery.Context);
		}

		public static SubqueryParts ParseSubquery(QueryModel queryModel, QueryParts parentQuery, bool canQueryInMemory)
		{
			return ParseSubquery(queryModel, parentQuery, canQueryInMemory, parentQuery.Context);
		}

		public static SubqueryParts ParseSubquery(QueryModel queryModel, QueryParts parentQuery, bool canQueryInMemory, QueryContext context)
		{
			var visitor = new SubqueryGeneratorQueryModelVisitor(parentQuery, canQueryInMemory, queryModel.SelectClause.Selector, context);
			visitor.VisitQueryModel(queryModel);
			return visitor.QueryParts;
		}

		private readonly SubqueryParts QueryParts;

		private SubqueryGeneratorQueryModelVisitor(QueryParts parentQuery, bool canQueryInMemory, Expression selector, QueryContext context)
		{
			QueryParts = new SubqueryParts(parentQuery, canQueryInMemory, selector, context);
		}

		public override void VisitQueryModel(QueryModel queryModel)
		{
			queryModel.MainFromClause.Accept(this, queryModel);
			VisitBodyClauses(queryModel.BodyClauses, queryModel);
			VisitResultOperators(queryModel.ResultOperators, queryModel);
			queryModel.SelectClause.Accept(this, queryModel);
		}

		public override void VisitResultOperator(ResultOperatorBase resultOperator, QueryModel queryModel, int index)
		{
			QueryParts.AddResultOperator(resultOperator);

			base.VisitResultOperator(resultOperator, queryModel, index);
		}

		public override void VisitMainFromClause(MainFromClause fromClause, QueryModel queryModel)
		{
			QueryParts.SetFrom(fromClause);

			base.VisitMainFromClause(fromClause, queryModel);
		}

		public override void VisitSelectClause(SelectClause selectClause, QueryModel queryModel)
		{
			SubquerySelectExpressionTreeVisitor.ProcessExpression(selectClause.Selector, QueryParts);

			base.VisitSelectClause(selectClause, queryModel);
		}

		public override void VisitWhereClause(WhereClause whereClause, QueryModel queryModel, int index)
		{
			QueryParts.AddCondition(whereClause.Predicate);

			base.VisitWhereClause(whereClause, queryModel, index);
		}

		public override void VisitOrderByClause(OrderByClause orderByClause, QueryModel queryModel, int index)
		{
			QueryParts.AddOrderBy(orderByClause);

			base.VisitOrderByClause(orderByClause, queryModel, index);
		}

		public override void VisitJoinClause(JoinClause joinClause, QueryModel queryModel, int index)
		{
			QueryParts.AddJoin(joinClause);

			base.VisitJoinClause(joinClause, queryModel, index);
		}

		public override void VisitAdditionalFromClause(AdditionalFromClause fromClause, QueryModel queryModel, int index)
		{
			QueryParts.AddJoin(fromClause);

			base.VisitAdditionalFromClause(fromClause, queryModel, index);
		}

		public override void VisitGroupJoinClause(GroupJoinClause groupJoinClause, QueryModel queryModel, int index)
		{
			throw new NotSupportedException("Not implemented yet.");
		}
	}
}