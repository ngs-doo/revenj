using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using Remotion.Linq.Clauses.Expressions;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Postgres.Plugins.ExpressionSupport
{
	[Export(typeof(IExpressionMatcher))]
	public class LinqMethods : IExpressionMatcher
	{
		private delegate void MethodCallDelegate(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, IPostgresConverterFactory converter);

		private static Dictionary<MethodInfo, MethodCallDelegate> SupportedMethods;
		static LinqMethods()
		{
			SupportedMethods = new Dictionary<MethodInfo, MethodCallDelegate>();
			SupportedMethods.Add(typeof(Enumerable).GetMethod("ToArray"), ArrayAgg);
			SupportedMethods.Add(typeof(Enumerable).GetMethod("ToList"), ArrayAgg);
			SupportedMethods.Add(typeof(CollectionExtensions).GetMethod("ToSet"), ArrayAgg);
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context, IPostgresConverterFactory converter)
		{
			var mce = expression as MethodCallExpression;
			if (mce == null || !mce.Method.IsGenericMethod)
				return false;

			MethodCallDelegate mcd;
			if (SupportedMethods.TryGetValue(mce.Method.GetGenericMethodDefinition(), out mcd))
			{
				mcd(mce, queryBuilder, visitExpression, converter);
				return true;
			}
			return false;
		}

		private static void ArrayAgg(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, IPostgresConverterFactory converter)
		{
			queryBuilder.Append("(SELECT array_agg(");
			var sqe = methodCall.Arguments[0] as SubQueryExpression;
			var alias = "\"-sq-\"";
			if (sqe != null)
			{
				var me = sqe.QueryModel.SelectClause.Selector as MemberExpression;
				if (me != null)
					queryBuilder.Append("\"").Append(converter.GetName(me.Member)).Append("\"");
				else
				{
					visitExpression(sqe.QueryModel.SelectClause.Selector);
					alias = "\"" + sqe.QueryModel.MainFromClause.ItemName + "\"";
				}
			}
			else queryBuilder.Append(alias);
			queryBuilder.Append(") FROM ");
			if (methodCall.Arguments[0] is MemberExpression)
				queryBuilder.Append("unnest(");
			else queryBuilder.Append('(');
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(") ");
			queryBuilder.Append(alias);
			queryBuilder.Append(")");
		}
	}
}
