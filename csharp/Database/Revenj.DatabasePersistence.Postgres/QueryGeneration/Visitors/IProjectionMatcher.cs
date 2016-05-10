using System;
using System.Linq.Expressions;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors
{
	public interface IProjectionMatcher
	{
		bool TryMatch(Expression expression, MainQueryParts queryParts, Action<Expression> visitExpression, QueryContext context);
	}
}
