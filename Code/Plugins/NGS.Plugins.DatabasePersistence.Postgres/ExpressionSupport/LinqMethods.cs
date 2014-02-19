using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using NGS.DatabasePersistence.Postgres.QueryGeneration.Visitors;
using Remotion.Linq.Clauses.Expressions;

namespace NGS.Plugins.DatabasePersistence.Postgres.ExpressionSupport
{
	[Export(typeof(IExpressionMatcher))]
	public class LinqMethods : IExpressionMatcher
	{
		private delegate void MethodCallDelegate(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression);

		private static Dictionary<MethodInfo, MethodCallDelegate> SupportedMethods;
		static LinqMethods()
		{
			SupportedMethods = new Dictionary<MethodInfo, MethodCallDelegate>();
			SupportedMethods.Add(typeof(Enumerable).GetMethod("ToArray"), ArrayAgg);
			SupportedMethods.Add(typeof(Enumerable).GetMethod("ToList"), ArrayAgg);
			SupportedMethods.Add(typeof(CollectionExtensions).GetMethod("ToSet"), ArrayAgg);
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var mce = expression as MethodCallExpression;
			if (mce == null || !mce.Method.IsGenericMethod)
				return false;

			MethodCallDelegate mcd;
			if (SupportedMethods.TryGetValue(mce.Method.GetGenericMethodDefinition(), out mcd))
			{
				mcd(mce, queryBuilder, visitExpression);
				return true;
			}
			return false;
		}

		private static void ArrayAgg(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(SELECT array_agg(");
			var sqe = methodCall.Arguments[0] as SubQueryExpression;
			if (sqe != null)
			{
				var me = sqe.QueryModel.SelectClause.Selector as MemberExpression;
				if (me != null)
					queryBuilder.Append("\"").Append(me.Member.Name).Append("\"");
				else
					visitExpression(sqe.QueryModel.SelectClause.Selector);
			}
			else queryBuilder.Append("\"-sq-\"");
			queryBuilder.Append(") FROM ");
			if (methodCall.Arguments[0] is MemberExpression)
				queryBuilder.Append("unnest(");
			else queryBuilder.Append('(');
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(") \"-sq-\")");
		}
	}
}
