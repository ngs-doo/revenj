using System;
using System.ComponentModel.Composition;
using System.Linq.Expressions;
using System.Text;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.Plugins.DatabasePersistence.Postgres.ExpressionSupport
{
	[Export(typeof(IExpressionMatcher))]
	public class ArrayMethods : IExpressionMatcher
	{
		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var be = expression as BinaryExpression;
			return be != null
				&& ArrayIndex(be, queryBuilder, visitExpression);
		}

		private static bool ArrayIndex(BinaryExpression expression, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			if (expression.NodeType == ExpressionType.ArrayIndex)
			{
				visitExpression(expression.Left);
				//TODO: detect constants to avoid 1 + !?
				queryBuilder.Append("[1 + ");
				visitExpression(expression.Right);
				queryBuilder.Append("]");
				return true;
			}
			return false;
		}
	}
}
