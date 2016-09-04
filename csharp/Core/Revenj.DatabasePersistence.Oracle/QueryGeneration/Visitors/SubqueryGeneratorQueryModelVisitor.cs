using System;
using System.Linq.Expressions;
using Remotion.Linq;
using Remotion.Linq.Clauses;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors
{
	public class SubqueryGeneratorQueryModelVisitor : QueryModelVisitorBase
	{
		public static SubqueryParts ParseSubquery(QueryModel queryModel, QueryParts parentQuery, string contextName, QueryContext context)
		{
			return ParseSubquery(queryModel, parentQuery, false, contextName, context);
		}

		public static SubqueryParts ParseSubquery(QueryModel queryModel, QueryParts parentQuery, bool canQueryInMemory, string contextName, QueryContext context)
		{
			var visitor = new SubqueryGeneratorQueryModelVisitor(parentQuery, canQueryInMemory, queryModel.SelectClause.Selector, contextName, context);
			visitor.VisitQueryModel(queryModel);
			return visitor.QueryParts;
		}

		private readonly SubqueryParts QueryParts;

		private SubqueryGeneratorQueryModelVisitor(QueryParts parentQuery, bool canQueryInMemory, Expression selector, string contextName, QueryContext context)
		{
			QueryParts = new SubqueryParts(parentQuery, canQueryInMemory, selector, contextName, context);
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