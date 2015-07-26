using System;
using System.Diagnostics.Contracts;
using System.Linq.Expressions;
using Remotion.Linq;
using Remotion.Linq.Clauses;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;
using Revenj.Extensibility;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration
{
	internal class SqlGeneratorQueryModelVisitor : QueryModelVisitorBase
	{
		public static SqlCommandData GenerateSqlQuery(
			QueryModel queryModel,
			IServiceProvider locator,
			IPostgresConverterFactory factory,
			IExtensibilityProvider extensibilityProvider)
		{
			Contract.Requires(queryModel != null);
			Contract.Requires(locator != null);
			Contract.Requires(factory != null);
			Contract.Requires(extensibilityProvider != null);

			var visitor =
				new SqlGeneratorQueryModelVisitor(
					new MainQueryParts(
						locator,
						factory,
						extensibilityProvider.ResolvePlugins<IQuerySimplification>(),
						extensibilityProvider.ResolvePlugins<IExpressionMatcher>(),
						extensibilityProvider.ResolvePlugins<IMemberMatcher>(),
						extensibilityProvider.ResolvePlugins<IProjectionMatcher>()));
			visitor.VisitQueryModel(queryModel);
			return new SqlCommandData(visitor.QueryParts);
		}

		private readonly MainQueryParts QueryParts;

		public SqlGeneratorQueryModelVisitor(MainQueryParts queryParts)
		{
			Contract.Requires(queryParts != null);

			this.QueryParts = queryParts;
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
			SelectGeneratorExpressionTreeVisitor.ProcessExpression(selectClause.Selector, QueryParts, queryModel);

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
			QueryParts.AddJoin(groupJoinClause);

			base.VisitGroupJoinClause(groupJoinClause, queryModel, index);
		}

		private string GetSqlExpression(Expression expression)
		{
			return SqlGeneratorExpressionTreeVisitor.GetSqlExpression(expression, QueryParts);
		}
	}
}