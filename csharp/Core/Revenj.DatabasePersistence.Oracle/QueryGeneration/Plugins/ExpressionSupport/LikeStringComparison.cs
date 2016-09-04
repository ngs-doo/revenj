using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Oracle.Plugins.ExpressionSupport
{
	internal class LikeStringComparison : IExpressionMatcher
	{
		private static List<MethodInfo> CompareMethods =
			new[]
			{
				typeof(string).GetMethod("Compare", new[] { typeof(string), typeof(string) }),
				typeof(string).GetMethod("Compare", new[] { typeof(string), typeof(string), typeof(bool) }),
				typeof(string).GetMethod("Compare", new[] { typeof(string), typeof(string), typeof(StringComparison) })
			}.ToList();

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			var be = expression as BinaryExpression;
			if (be == null)
				return false;

			if (expression.NodeType == ExpressionType.Equal || expression.NodeType == ExpressionType.NotEqual)
			{
				var ceZero = be.Right as ConstantExpression ?? be.Left as ConstantExpression;
				var ceMethod = be.Left as MethodCallExpression ?? be.Right as MethodCallExpression;
				if (ceZero == null || ceMethod == null || !ceZero.Value.Equals(0) || !CompareMethods.Contains(ceMethod.Method))
					return false;

				return CompareString(expression.NodeType == ExpressionType.Equal, ceMethod, queryBuilder, visitExpression, context);
			}
			return false;
		}

		private static void EscapeForLike(bool equal, bool ignoreCase, MethodCallExpression mce, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var first = mce.Arguments[0];
			var second = mce.Arguments[1];
			var ce = first as ConstantExpression;
			if (ce != null)
			{
				if (ignoreCase)
					visitExpression(ConstantExpression.Constant((ce.Value as string).ToUpper(), ce.Type));
				else
					visitExpression(ce);
			}
			else
			{
				if (ignoreCase)
				{
					queryBuilder.Append(" UPPER(");
					visitExpression(first);
					queryBuilder.Append(")");
				}
				else visitExpression(first);
			}
			if (equal)
				queryBuilder.Append(" = ");
			else
				queryBuilder.Append(" <> ");
			ce = second as ConstantExpression;
			if (ce != null)
			{
				if (ignoreCase)
					visitExpression(ConstantExpression.Constant((ce.Value as string).ToUpper(), ce.Type));
				else
					visitExpression(ce);
			}
			else
			{
				if (ignoreCase)
				{
					queryBuilder.Append(" UPPER(");
					visitExpression(second);
					queryBuilder.Append(")");
				}
				else visitExpression(second);
			}
		}

		private static bool GuardForNull(bool equal, Expression exp, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			if (context.InSelect)
			{
				queryBuilder.Append(" CASE WHEN ");
				visitExpression(exp);
				if (equal)
					queryBuilder.Append(" IS NULL THEN 'Y' ELSE 'N' END");
				else
					queryBuilder.Append(" IS NULL THEN 'N' ELSE 'Y' END");
			}
			else
			{
				visitExpression(exp);
				if (equal)
					queryBuilder.Append(" IS NULL");
				else
					queryBuilder.Append(" IS NOT NULL");
			}
			return true;
		}

		private static bool CompareString(bool equal, MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			var count = methodCall.Arguments.Count;
			if (count != 2 && count != 3)
				return false;
			//TODO: handle Oracle incorrect handling of empty string
			var ce = methodCall.Arguments[0] as ConstantExpression;
			if (ce != null && ce.Value == null)
				return GuardForNull(equal, methodCall.Arguments[1], queryBuilder, visitExpression, context);
			ce = methodCall.Arguments[1] as ConstantExpression;
			if (ce != null && ce.Value == null)
				return GuardForNull(equal, methodCall.Arguments[0], queryBuilder, visitExpression, context);

			if (count == 2)
			{
				queryBuilder.Append("(");
				if (context.InSelect)
					queryBuilder.Append(" CASE WHEN ");
				visitExpression(methodCall.Arguments[0]);
				if (equal)
					queryBuilder.Append(" = ");
				else
					queryBuilder.Append(" <> ");
				visitExpression(methodCall.Arguments[1]);
			}
			else
			{
				var cmpValue = methodCall.Arguments[2] as ConstantExpression;
				if (methodCall.Arguments[2].Type == typeof(bool) && cmpValue != null)
				{
					queryBuilder.Append("(");
					if (context.InSelect)
						queryBuilder.Append(" CASE WHEN ");
					EscapeForLike(equal, (bool)cmpValue.Value, methodCall, queryBuilder, visitExpression);
				}
				else if (methodCall.Arguments[2].Type == typeof(StringComparison))
				{
					var cmpVal = (StringComparison)cmpValue.Value;
					var ignoreCase = cmpVal != StringComparison.CurrentCulture
						&& cmpVal != StringComparison.InvariantCulture
						&& cmpVal != StringComparison.Ordinal;

					queryBuilder.Append("(");
					if (context.InSelect)
						queryBuilder.Append(" CASE WHEN ");
					EscapeForLike(equal, ignoreCase, methodCall, queryBuilder, visitExpression);
				}
				else return false;
			}

			if (context.InSelect)
				queryBuilder.Append(" THEN 'Y' ELSE 'N' END");
			queryBuilder.Append(")");
			return true;
		}
	}
}