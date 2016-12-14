using System;
using System.Linq;
using System.Linq.Expressions;
using System.Text;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Postgres.Plugins.ExpressionSupport
{
	public class DatabaseSpecificationFunctions : IExpressionMatcher
	{
		private readonly string Function;

		public DatabaseSpecificationFunctions(string function)
		{
			this.Function = function;
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context, IPostgresConverterFactory converter)
		{
			var mce = expression as MethodCallExpression;
			if (mce == null || mce.Method.ReturnType != typeof(bool))
				return false;

			queryBuilder.Append(Function).Append('(');
			visitExpression(mce.Arguments[0]);
			foreach (var arg in mce.Arguments.Skip(1))
			{
				queryBuilder.Append(',');
				visitExpression(arg);
			}
			queryBuilder.Append(')');
			return true;
		}
	}
}
