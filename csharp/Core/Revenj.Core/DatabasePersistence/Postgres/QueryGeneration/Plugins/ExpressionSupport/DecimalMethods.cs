using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Postgres.Plugins.ExpressionSupport
{
	[Export(typeof(IExpressionMatcher))]
	public class DecimalMethods : IExpressionMatcher
	{
		private delegate void MethodCallDelegate(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression);

		private static Dictionary<MethodInfo, MethodCallDelegate> SupportedMethods = InitDictionary();
		private static Dictionary<MethodInfo, MethodCallDelegate> InitDictionary()
		{
			var dict = new Dictionary<MethodInfo, MethodCallDelegate>();
			dict.Add(typeof(decimal).GetMethod("Add", new[] { typeof(decimal), typeof(decimal) }), MatchAdd);
			dict.Add(typeof(decimal).GetMethod("Ceiling", new[] { typeof(decimal) }), MatchSimpleMethod);
			dict.Add(typeof(decimal).GetMethod("Compare", new[] { typeof(decimal), typeof(decimal) }), MatchCompare);
			dict.Add(typeof(decimal).GetMethod("Divide", new[] { typeof(decimal), typeof(decimal) }), MatchDivide);
			dict.Add(typeof(decimal).GetMethod("Equals", new[] { typeof(decimal), typeof(decimal) }), MatchEquals);
			dict.Add(typeof(decimal).GetMethod("Floor", new[] { typeof(decimal) }), MatchSimpleMethod);
			dict.Add(typeof(decimal).GetMethod("Multiply", new[] { typeof(decimal), typeof(decimal) }), MatchMultiply);
			dict.Add(typeof(decimal).GetMethod("Negate", new[] { typeof(decimal) }), MatchNegate);
			dict.Add(typeof(decimal).GetMethod("Remainder", new[] { typeof(decimal), typeof(decimal) }), MatchRemainder);
			dict.Add(typeof(decimal).GetMethod("Round", new[] { typeof(decimal) }), MatchSimpleRound);
			dict.Add(typeof(decimal).GetMethod("Round", new[] { typeof(decimal), typeof(int) }), MatchSimpleRound);
			dict.Add(typeof(decimal).GetMethod("Subtract", new[] { typeof(decimal), typeof(decimal) }), MatchSubtract);
			dict.Add(typeof(decimal).GetMethod("Truncate", new[] { typeof(decimal) }), MatchSimpleRound);
			return dict;
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context, IPostgresConverterFactory converter)
		{
			var mce = expression as MethodCallExpression;
			if (mce == null)
				return false;

			MethodCallDelegate mcd;
			if (SupportedMethods.TryGetValue(mce.Method, out mcd))
			{
				mcd(mce, queryBuilder, visitExpression);
				return true;
			}
			return false;
		}

		private static void MatchAdd(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("+");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(")");
		}

		private static void MatchSimpleMethod(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append(" ");
			queryBuilder.Append(methodCall.Method.Name);
			queryBuilder.Append("(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(")");
		}

		private static void MatchCompare(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append(" CASE WHEN ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(" > ");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(" THEN 1 WHEN ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(" < ");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(" THEN -1 ELSE 0 END ");
		}

		private static void MatchDivide(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("/");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(")");
		}

		private static void MatchEquals(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("=");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(")");
		}

		private static void MatchMultiply(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("*");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(")");
		}

		private static void MatchNegate(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append(" -(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(")");
		}

		private static void MatchRemainder(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("%");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(")");
		}

		private static void MatchSubtract(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("-");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(")");
		}

		private static void MatchSimpleRound(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append(" ROUND(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(", ");
			if (methodCall.Arguments.Count > 1)
				visitExpression(methodCall.Arguments[1]);
			else
				queryBuilder.Append("0");
			queryBuilder.Append(")");
		}
	}
}
