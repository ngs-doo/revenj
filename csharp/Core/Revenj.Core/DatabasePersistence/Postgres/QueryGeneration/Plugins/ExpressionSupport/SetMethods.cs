using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq.Expressions;
using System.Text;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Postgres.Plugins.ExpressionSupport
{
	[Export(typeof(IExpressionMatcher))]
	public class SetMethods : IExpressionMatcher
	{
		private delegate void MethodCallDelegate(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression);

		private static Dictionary<string, MethodCallDelegate> SupportedMethods;
		static SetMethods()
		{
			SupportedMethods = new Dictionary<string, MethodCallDelegate>();
			SupportedMethods.Add("IsSubsetOf", IsSubset);
			SupportedMethods.Add("IsProperSubsetOf", IsSubset);
			SupportedMethods.Add("IsSupersetOf", IsSuperset);
			SupportedMethods.Add("IsProperSupersetOf", IsSuperset);
			SupportedMethods.Add("Overlaps", Overlaps);
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context, IPostgresConverterFactory converter)
		{
			var mce = expression as MethodCallExpression;
			if (mce == null)
				return false;

			MethodCallDelegate mcd;
			var dt = mce.Method.DeclaringType;
			if (dt.IsGenericType && dt.GetGenericTypeDefinition() == typeof(HashSet<>) && SupportedMethods.TryGetValue(mce.Method.Name, out mcd))
			{
				mcd(mce, queryBuilder, visitExpression);
				return true;
			}
			return false;
		}

		private static void IsSubset(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var type = methodCall.Object.Type.GetGenericArguments()[0];
			var name = Revenj.DatabasePersistence.Postgres.NpgsqlTypes.TypeConverter.GetTypeName(type);
			queryBuilder.Append("(");
			visitExpression(methodCall.Object);
			queryBuilder.Append("::").Append(name).Append("[]");
			queryBuilder.Append(" <@ ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::").Append(name).Append("[]");
			queryBuilder.Append(")");
		}

		private static void IsSuperset(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var type = methodCall.Object.Type.GetGenericArguments()[0];
			var name = Revenj.DatabasePersistence.Postgres.NpgsqlTypes.TypeConverter.GetTypeName(type);
			queryBuilder.Append("(");
			visitExpression(methodCall.Object);
			queryBuilder.Append("::").Append(name).Append("[]");
			queryBuilder.Append(" @> ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::").Append(name).Append("[]");
			queryBuilder.Append(")");
		}

		private static void Overlaps(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var type = methodCall.Object.Type.GetGenericArguments()[0];
			var name = Revenj.DatabasePersistence.Postgres.NpgsqlTypes.TypeConverter.GetTypeName(type);
			queryBuilder.Append("(");
			visitExpression(methodCall.Object);
			queryBuilder.Append("::").Append(name).Append("[]");
			queryBuilder.Append(" && ");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::").Append(name).Append("[]");
			queryBuilder.Append(")");
		}
	}
}
