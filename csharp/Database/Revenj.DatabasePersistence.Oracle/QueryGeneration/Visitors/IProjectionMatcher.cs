using System;
using System.Linq.Expressions;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors
{
	public interface IProjectionMatcher
	{
		bool TryMatch(Expression expression, MainQueryParts queryParts, Action<Expression> visitExpression);
	}
}
