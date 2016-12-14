using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Postgres.Plugins.ExpressionSupport
{
	[Export(typeof(IExpressionMatcher))]
	public class TreeMethods : IExpressionMatcher
	{
		private delegate void MethodCallDelegate(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression);

		private static Dictionary<MethodInfo, MethodCallDelegate> SupportedMethods;
		static TreeMethods()
		{
			SupportedMethods = new Dictionary<MethodInfo, MethodCallDelegate>();
			SupportedMethods.Add(typeof(TreePath).GetMethod("IsAncestor", new[] { typeof(TreePath) }), IsAncestor);
			SupportedMethods.Add(typeof(TreePath).GetMethod("IsDescendant", new[] { typeof(TreePath) }), IsDescendant);
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context, IPostgresConverterFactory converter)
		{
			var mce = expression as MethodCallExpression;
			if (mce == null)
				return false;

			MethodCallDelegate mcd;
			if (SupportedMethods.TryGetValue(mce.Method, out mcd))
			{
				mcd(mce, queryBuilder, visitExpression);
				return true;
			}
			return false;
		}

		private static void IsAncestor(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Object);
			queryBuilder.Append("::ltree @> ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::ltree)");
		}

		private static void IsDescendant(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Object);
			queryBuilder.Append("::ltree <@ ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::ltree)");
		}
	}
}
