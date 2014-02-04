using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Linq.Expressions;
using NGS.DatabasePersistence.Postgres.QueryGeneration.Visitors;
using System.ComponentModel.Composition;

namespace NGS.Plugins.DatabasePersistence.Postgres.ExpressionSupport
{
	[Export(typeof(IExpressionMatcher))]
	public class ExpressionShortCircuiting : IExpressionMatcher
	{
		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var be = expression as BinaryExpression;
			return be != null
				&& (CheckShortCircuitAnd(be, queryBuilder, visitExpression)
					|| CheckShortCircuitOr(be, queryBuilder, visitExpression));
		}

		private static bool CheckShortCircuitAnd(BinaryExpression expression, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			if (expression.NodeType == ExpressionType.And || expression.NodeType == ExpressionType.AndAlso)
			{
				var ceLeft = expression.Left as ConstantExpression;
				var ceRight = expression.Right as ConstantExpression;

				if (ceLeft != null && ceLeft.Type == typeof(bool))
				{
					if ((bool)ceLeft.Value)
						visitExpression(expression.Right);
					else
						queryBuilder.Append("false ");
					return true;
				}
				else if (ceRight != null && ceRight.Type == typeof(bool))
				{
					if ((bool)ceRight.Value)
						visitExpression(expression.Left);
					else
						queryBuilder.Append("false ");
					return true;
				}
			}
			return false;
		}

		private static bool CheckShortCircuitOr(BinaryExpression expression, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			if (expression.NodeType == ExpressionType.Or || expression.NodeType == ExpressionType.OrElse)
			{
				var ceLeft = expression.Left as ConstantExpression;
				var ceRight = expression.Right as ConstantExpression;

				if (ceLeft != null && ceLeft.Type == typeof(bool))
				{
					if ((bool)ceLeft.Value)
						queryBuilder.Append("true ");
					else
						visitExpression(expression.Right);
					return true;
				}
				else if (ceRight != null && ceRight.Type == typeof(bool))
				{
					if ((bool)ceRight.Value)
						queryBuilder.Append("true ");
					else
						visitExpression(expression.Left);
					return true;
				}
			}
			return false;
		}
	}
}
