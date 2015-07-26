using System;
using System.Linq.Expressions;
using System.Text;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors
{
	public interface IMemberMatcher
	{
		bool TryMatch(MemberExpression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context);
	}
}
