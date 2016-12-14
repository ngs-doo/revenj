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
	public class DictionaryMethods : IExpressionMatcher
	{
		private delegate void MethodCallDelegate(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression);

		private static readonly Dictionary<MethodInfo, MethodCallDelegate> SupportedMethods;
		static DictionaryMethods()
		{
			SupportedMethods = new Dictionary<MethodInfo, MethodCallDelegate>();
			SupportedMethods.Add(typeof(Dictionary<string, string>).GetMethod("ContainsKey", new Type[] { typeof(string) }), ContainsKey);
			SupportedMethods.Add(typeof(Dictionary<string, string>).GetMethod("get_Item", new Type[] { typeof(string) }), GetValue);
			SupportedMethods.Add(typeof(Dictionary<string, object>).GetMethod("ContainsKey", new Type[] { typeof(string) }), ContainsKey);
			SupportedMethods.Add(typeof(Dictionary<string, object>).GetMethod("get_Item", new Type[] { typeof(string) }), GetValue);
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context, IPostgresConverterFactory converter)
		{
			var mce = expression as MethodCallExpression;
			if (mce == null)
				return false;

			MethodCallDelegate mcd;
			if (SupportedMethods.TryGetValue(mce.Method, out mcd))
			{
				if (mce.Arguments[0] is ConstantExpression == false)
					return false;
				mcd(mce, queryBuilder, visitExpression);
				return true;
			}
			return false;
		}

		private static void ContainsKey(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append('(');
			visitExpression(methodCall.Object);
			queryBuilder.Append(" ? ");
			var ce = methodCall.Arguments[0] as ConstantExpression;
			if (ce != null)
			{
				visitExpression(ce);
				queryBuilder.Append(')');
			}
			else
			{
				queryBuilder.Append('(');
				visitExpression(methodCall.Arguments[0]);
				queryBuilder.Append("))");
			}
		}

		private static void GetValue(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(" -> ");
			var ce = methodCall.Arguments[0] as ConstantExpression;
			if (ce != null)
			{
				visitExpression(ce);
				queryBuilder.Append(')');
			}
			else
			{
				queryBuilder.Append('(');
				visitExpression(methodCall.Arguments[0]);
				queryBuilder.Append("))");
			}
		}
	}
}
