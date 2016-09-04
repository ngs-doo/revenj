using System;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using Remotion.Linq;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ResultOperators;
using Remotion.Linq.Parsing;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors
{
	public class ProjectorBuildingExpressionTreeVisitor<T> : ExpressionTreeVisitor
	{
		// This is the generic ResultObjectMapping.GetObject<T>() method we'll use to obtain a queried object for an IQuerySource.
		protected static readonly MethodInfo GetObjectMethod = typeof(ResultObjectMapping).GetMethod("GetObject");
		protected static readonly MethodInfo EvaluateSubqueryMethod = typeof(ResultObjectMapping).GetMethod("EvaluateSubquery");

		// Call this method to get the projector. T is the type of the result (after the projection).
		public static Func<ResultObjectMapping, T> BuildProjector(QueryModel model)
		{
			// The visitor gives us the projector's body. It simply replaces all QuerySourceReferenceExpressions with calls to ResultObjectMapping.GetObject<T>().
			var selector = model.SelectClause.Selector;
			Type resultTypeOverride = null;
			var castOperator = model.ResultOperators.LastOrDefault(it => it is CastResultOperator) as CastResultOperator;
			if (castOperator != null && selector.Type != castOperator.CastItemType)
				resultTypeOverride = castOperator.CastItemType;
			var allOperator = model.ResultOperators.LastOrDefault(it => it is AllResultOperator) as AllResultOperator;
			if (allOperator != null)
				resultTypeOverride = allOperator.Predicate.Type;
			if (model.ResultOperators.Any(it => it is CountResultOperator))
				resultTypeOverride = typeof(int);
			if (model.ResultOperators.Any(it => it is LongCountResultOperator))
				resultTypeOverride = typeof(long);
			if (model.ResultOperators.Any(it => it is AnyResultOperator))
				resultTypeOverride = typeof(bool);

			var visitor = new ProjectorBuildingExpressionTreeVisitor<T>(selector, resultTypeOverride);
			var body = visitor.VisitExpression(selector);

			// Construct a LambdaExpression from parameter and body and compile it into a delegate.
			return Expression.Lambda<Func<ResultObjectMapping, T>>(body, visitor.ResultItemParameter).Compile();
		}

		public static Func<ResultObjectMapping, Func<T, T>> BuildAggregateProjector(QueryModel model, LambdaExpression aggregate)
		{
			var visitor = new ProjectorBuildingExpressionTreeVisitor<T>(aggregate, model.MainFromClause.ItemType);
			var body = visitor.VisitExpression(aggregate);
			return Expression.Lambda<Func<ResultObjectMapping, Func<T, T>>>(body, visitor.ResultItemParameter).Compile();
		}

		protected readonly ParameterExpression ResultItemParameter;
		protected readonly Type ResultTypeOverride;

		protected ProjectorBuildingExpressionTreeVisitor(Expression selectExpression, Type resultTypeOverride)
		{
			// This is the parameter of the delegat we're building. It's the ResultObjectMapping, which holds all the input data needed for the projection.
			ResultItemParameter = Expression.Parameter(typeof(ResultObjectMapping), "resultItem");
			this.ResultTypeOverride = resultTypeOverride;
		}

		protected override Expression VisitQuerySourceReferenceExpression(QuerySourceReferenceExpression expression)
		{
			// Substitute generic parameter "T" of ResultObjectMapping.GetObject<T>() with type of query source item, then return a call to that method
			// with the query source referenced by the expression.

			return Expression.Call(
				ResultItemParameter,
				GetObjectMethod.MakeGenericMethod(ResultTypeOverride ?? expression.Type),
				Expression.Constant(expression.ReferencedQuerySource));
		}

		protected override Expression VisitSubQueryExpression(SubQueryExpression expression)
		{
			return Expression.Call(
				ResultItemParameter,
				EvaluateSubqueryMethod.MakeGenericMethod(expression.QueryModel.ResultTypeOverride),
				Expression.Constant(expression));
		}
	}
}
