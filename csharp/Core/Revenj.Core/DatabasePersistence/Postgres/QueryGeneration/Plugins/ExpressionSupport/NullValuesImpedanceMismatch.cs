using System;
using System.ComponentModel.Composition;
using System.Linq.Expressions;
using System.Text;
using Remotion.Linq.Clauses.Expressions;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Postgres.Plugins.ExpressionSupport
{
	[Export(typeof(IExpressionMatcher))]
	public class NullValuesImpedanceMismatch : IExpressionMatcher
	{
		private static bool IsNullExpression(Expression expression)
		{
			var ce = expression as ConstantExpression;
			if (ce != null && ce.Value == null)
				return true;
			var un = expression as UnaryExpression;
			if (un != null)
				return IsNullExpression(un.Operand);
			var pe = expression as PartialEvaluationExceptionExpression;
			if (pe != null)
				return IsNullExpression(pe.EvaluatedExpression);
			return false;
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context, IPostgresConverterFactory converter)
		{
			var be = expression as BinaryExpression;
			if (be == null)
				return false;

			if (expression.NodeType == ExpressionType.Equal || expression.NodeType == ExpressionType.NotEqual)
			{
				var nullLeft = IsNullExpression(be.Left);
				var nullRight = IsNullExpression(be.Right);

				if (!nullLeft && nullRight)
				{
					if (expression.NodeType == ExpressionType.NotEqual)
						queryBuilder.Append(" (NOT ");
					visitExpression(be.Left);
					queryBuilder.Append(" IS NULL ");
					if (expression.NodeType == ExpressionType.NotEqual)
						queryBuilder.Append(")");
					return true;
				}
				else if (nullLeft && !nullRight)
				{
					if (expression.NodeType == ExpressionType.NotEqual)
						queryBuilder.Append(" (NOT ");
					visitExpression(be.Right);
					queryBuilder.Append(" IS NULL ");
					if (expression.NodeType == ExpressionType.NotEqual)
						queryBuilder.Append(")");
					return true;
				}
				else if (nullLeft && nullRight)
				{
					if (expression.NodeType == ExpressionType.Equal)
						queryBuilder.Append(" true ");
					else
						queryBuilder.Append(" false ");
					return true;
				}
			}
			return false;
		}
	}
}
