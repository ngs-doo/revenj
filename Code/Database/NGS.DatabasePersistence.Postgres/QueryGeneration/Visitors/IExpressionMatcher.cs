using System;
using System.Linq.Expressions;
using System.Text;

namespace NGS.DatabasePersistence.Postgres.QueryGeneration.Visitors
{
	public interface IExpressionMatcher
	{
		bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression);
	}
}
