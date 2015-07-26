using System;
using System.Collections.Concurrent;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using Remotion.Linq;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ResultOperators;
using Remotion.Linq.Parsing;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors
{
	internal class ProjectorBuildingExpressionTreeVisitor<T> : ExpressionTreeVisitor
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
			//PERF: compile is slow
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
			ResultItemParameter = Expression.Parameter(typeof(ResultObjectMapping), "resultItem");
			this.ResultTypeOverride = resultTypeOverride;
		}

		private static readonly ConcurrentDictionary<Type, MethodInfo> ObjectMethodCache = new ConcurrentDictionary<Type, MethodInfo>(1, 117);
		private static readonly ConcurrentDictionary<Type, MethodInfo> EvaluateMethodCache = new ConcurrentDictionary<Type, MethodInfo>(1, 117);

		protected override Expression VisitQuerySourceReferenceExpression(QuerySourceReferenceExpression expression)
		{
			// Substitute generic parameter "T" of ResultObjectMapping.GetObject<T>() with type of query source item, then return a call to that method
			// with the query source referenced by the expression.
			var type = ResultTypeOverride ?? expression.Type;
			MethodInfo target;
			if (!ObjectMethodCache.TryGetValue(type, out target))
			{
				target = GetObjectMethod.MakeGenericMethod(ResultTypeOverride ?? expression.Type);
				ObjectMethodCache.TryAdd(type, target);
			}
			return Expression.Call(ResultItemParameter, target, Expression.Constant(expression.ReferencedQuerySource));
		}

		protected override Expression VisitSubQueryExpression(SubQueryExpression expression)
		{
			var type = expression.QueryModel.ResultTypeOverride;
			MethodInfo target;
			if (!EvaluateMethodCache.TryGetValue(type, out target))
			{
				target = EvaluateSubqueryMethod.MakeGenericMethod(type);
				EvaluateMethodCache.TryAdd(type, target);
			}
			return Expression.Call(ResultItemParameter, target, Expression.Constant(expression));
		}
	}
}
