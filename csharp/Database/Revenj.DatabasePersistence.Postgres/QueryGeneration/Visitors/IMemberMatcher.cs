using System;
using System.Linq.Expressions;
using System.Text;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors
{
	public interface IMemberMatcher
	{
		bool TryMatch(MemberExpression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context);
	}
}
