using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Postgres.Plugins.ExpressionSupport
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

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context, IPostgresConverterFactory converter)
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

		private static readonly char[] EscapeChars = new[] { '\\', '%', '_' };

		private static void EscapeForLike(Expression exp, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var ce = exp as ConstantExpression;
			if (ce != null)
			{
				var value = ce.Value as string;
				if (value.IndexOfAny(EscapeChars) >= 0)
					visitExpression(ConstantExpression.Constant(value.Replace(@"\", @"\\").Replace("_", "\\_").Replace("%", "\\%"), typeof(string)));
				else
					visitExpression(ce);
			}
			else
			{
				queryBuilder.Append(" REPLACE(REPLACE(REPLACE(");
				visitExpression(exp);
				queryBuilder.Append(@", '\','\\'), '_','\_'), '%','\%') ");
			}
		}

		private static bool GuardForNull(bool equal, Expression exp, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			visitExpression(exp);
			if (equal)
				queryBuilder.Append(" IS NULL");
			else
				queryBuilder.Append(" IS NOT NULL");
			return true;
		}

		private static bool CompareString(bool equal, MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var count = methodCall.Arguments.Count;
			if (count != 2 && count != 3)
				return false;

			var ce = methodCall.Arguments[0] as ConstantExpression;
			if (ce != null && ce.Value == null)
				return GuardForNull(equal, methodCall.Arguments[1], queryBuilder, visitExpression);
			ce = methodCall.Arguments[1] as ConstantExpression;
			if (ce != null && ce.Value == null)
				return GuardForNull(equal, methodCall.Arguments[0], queryBuilder, visitExpression);

			if (count == 2)
			{
				queryBuilder.Append("(");
				visitExpression(methodCall.Arguments[0]);
				if (!equal)
					queryBuilder.Append(" NOT");
				queryBuilder.Append(" LIKE ");
				EscapeForLike(methodCall.Arguments[1], queryBuilder, visitExpression);
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
					EscapeForLike(methodCall.Arguments[1], queryBuilder, visitExpression);
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
					EscapeForLike(methodCall.Arguments[1], queryBuilder, visitExpression);
				}
				else return false;
			}

			queryBuilder.Append(")");
			return true;
		}
	}
}
