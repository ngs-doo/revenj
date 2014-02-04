using System;
using System.Linq.Expressions;
using NGS.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;

namespace NGS.DatabasePersistence.Oracle.QueryGeneration.Visitors
{
	public interface IProjectionMatcher
	{
		bool TryMatch(Expression expression, MainQueryParts queryParts, Action<Expression> visitExpression);
	}
}
