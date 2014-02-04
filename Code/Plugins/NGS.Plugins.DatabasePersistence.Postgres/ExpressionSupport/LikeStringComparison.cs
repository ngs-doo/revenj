using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using NGS.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace NGS.Plugins.DatabasePersistence.Postgres.ExpressionSupport
{
	[Export(typeof(IExpressionMatcher))]
	public class LikeStringComparison : IExpressionMatcher
	{
		private static List<MethodInfo> CompareMethods =
			new[]
			{
				typeof(string).GetMethod("Compare", new[] { typeof(string), typeof(string) }),
				typeof(string).GetMethod("Compare", new[] { typeof(string), typeof(string), typeof(bool) }),
				typeof(string).GetMethod("Compare", new[] { typeof(string), typeof(string), typeof(StringComparison) })
			}.ToList();

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression)
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

				return CompareString(expression.NodeType == ExpressionType.Equal, ceMethod, queryBuilder, visitExpression);
			}
			return false;
		}

		private static bool CompareString(bool equal, MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var count = methodCall.Arguments.Count;
			if (count != 2 && count != 3)
				return false;

			if (count == 2)
			{
				queryBuilder.Append("(");
				visitExpression(methodCall.Arguments[0]);
				if (!equal)
					queryBuilder.Append(" NOT");
				queryBuilder.Append(" LIKE ");
				visitExpression(methodCall.Arguments[1]);
			}
			else if (count == 3)
			{
				var cmpValue = methodCall.Arguments[2] as ConstantExpression;
				if (methodCall.Arguments[2].Type == typeof(bool) && cmpValue != null)
				{
					queryBuilder.Append("(");
					visitExpression(methodCall.Arguments[0]);
					if (!equal)
						queryBuilder.Append(" NOT");
					if ((bool)cmpValue.Value)
						queryBuilder.Append(" ILIKE ");
					else
						queryBuilder.Append(" LIKE ");
					visitExpression(methodCall.Arguments[1]);
				}
				else if (methodCall.Arguments[2].Type == typeof(StringComparison))
				{
					queryBuilder.Append("(");
					visitExpression(methodCall.Arguments[0]);
					if (!equal)
						queryBuilder.Append(" NOT");
					switch ((StringComparison)cmpValue.Value)
					{
						case StringComparison.CurrentCulture:
						case StringComparison.InvariantCulture:
						case StringComparison.Ordinal:
							queryBuilder.Append(" LIKE ");
							break;
						default:
							queryBuilder.Append(" ILIKE ");
							break;
					}
					visitExpression(methodCall.Arguments[1]);
				}
				else return false;
			}

			queryBuilder.Append(")");
			return true;
		}
	}
}
