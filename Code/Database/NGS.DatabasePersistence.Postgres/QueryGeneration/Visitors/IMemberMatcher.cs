using System;
using System.Linq.Expressions;
using System.Text;

namespace NGS.DatabasePersistence.Postgres.QueryGeneration.Visitors
{
	public interface IMemberMatcher
	{
		bool TryMatch(MemberExpression expression, StringBuilder queryBuilder, Action<Expression> visitExpression);
	}
}
