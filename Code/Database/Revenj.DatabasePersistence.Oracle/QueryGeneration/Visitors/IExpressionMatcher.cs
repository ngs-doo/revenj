using System;
using System.Linq.Expressions;
using System.Text;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors
{
	public interface IExpressionMatcher
	{
		bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context);
	}
}
