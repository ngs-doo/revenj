using System;
using System.Linq.Expressions;
using NGS.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;

namespace NGS.DatabasePersistence.Postgres.QueryGeneration.Visitors
{
	public interface IProjectionMatcher
	{
		bool TryMatch(Expression expression, MainQueryParts queryParts, Action<Expression> visitExpression);
	}
}
