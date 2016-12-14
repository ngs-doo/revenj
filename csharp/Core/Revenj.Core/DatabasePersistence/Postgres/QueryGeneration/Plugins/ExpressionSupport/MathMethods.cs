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
	public class MathMethods : IExpressionMatcher
	{
		private delegate void MethodCallDelegate(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression);

		private static Dictionary<MethodInfo, MethodCallDelegate> SupportedMethods = InitDictionary();
		private static Dictionary<MethodInfo, MethodCallDelegate> InitDictionary()
		{
			var dict = new Dictionary<MethodInfo, MethodCallDelegate>();
			dict.Add(typeof(Math).GetMethod("Abs", new[] { typeof(decimal) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Abs", new[] { typeof(double) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Abs", new[] { typeof(int) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Abs", new[] { typeof(long) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Ceiling", new[] { typeof(decimal) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Ceiling", new[] { typeof(double) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Exp", new[] { typeof(double) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Floor", new[] { typeof(decimal) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Floor", new[] { typeof(double) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Max", new[] { typeof(decimal), typeof(decimal) }), MatchMax);
			dict.Add(typeof(Math).GetMethod("Max", new[] { typeof(double), typeof(double) }), MatchMax);
			dict.Add(typeof(Math).GetMethod("Max", new[] { typeof(int), typeof(int) }), MatchMax);
			dict.Add(typeof(Math).GetMethod("Max", new[] { typeof(long), typeof(long) }), MatchMax);
			dict.Add(typeof(Math).GetMethod("Min", new[] { typeof(decimal), typeof(decimal) }), MatchMin);
			dict.Add(typeof(Math).GetMethod("Min", new[] { typeof(double), typeof(double) }), MatchMin);
			dict.Add(typeof(Math).GetMethod("Min", new[] { typeof(int), typeof(int) }), MatchMin);
			dict.Add(typeof(Math).GetMethod("Min", new[] { typeof(long), typeof(long) }), MatchMin);
			dict.Add(typeof(Math).GetMethod("Pow", new[] { typeof(double), typeof(double) }), MatchSimpleDouble);
			dict.Add(typeof(Math).GetMethod("Round", new[] { typeof(decimal) }), MatchDecimalRound);
			dict.Add(typeof(Math).GetMethod("Round", new[] { typeof(decimal), typeof(int) }), MatchDecimalRound);
			dict.Add(typeof(Math).GetMethod("Round", new[] { typeof(double) }), MatchDoubleRound);
			dict.Add(typeof(Math).GetMethod("Round", new[] { typeof(double), typeof(int) }), MatchDoubleRound);
			dict.Add(typeof(Math).GetMethod("Sign", new[] { typeof(decimal) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Sign", new[] { typeof(double) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Sign", new[] { typeof(int) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Sign", new[] { typeof(long) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Sqrt", new[] { typeof(double) }), MatchSimpleMethod);
			dict.Add(typeof(Math).GetMethod("Truncate", new[] { typeof(decimal) }), MatchDecimalRound);
			dict.Add(typeof(Math).GetMethod("Truncate", new[] { typeof(double) }), MatchDoubleRound);
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

		private static void MatchSimpleMethod(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append(" ");
			queryBuilder.Append(methodCall.Method.Name);
			queryBuilder.Append("(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(")");
		}

		private static void MatchDecimalRound(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append(" ROUND(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(", ");
			if (methodCall.Arguments.Count > 1)
				visitExpression(methodCall.Arguments[1]);
			else
				queryBuilder.Append("0");
			queryBuilder.Append(")::numeric");
		}

		private static void MatchDoubleRound(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
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

		private static void MatchMax(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append(" CASE WHEN ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(" > ");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(" THEN ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(" ELSE ");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(" END ");
		}

		private static void MatchMin(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append(" CASE WHEN ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(" < ");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(" THEN ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(" ELSE ");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(" END ");
		}

		private static void MatchSimpleDouble(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append(" ");
			queryBuilder.Append(methodCall.Method.Name);
			queryBuilder.Append("(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(", ");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(")");
		}
	}
}
