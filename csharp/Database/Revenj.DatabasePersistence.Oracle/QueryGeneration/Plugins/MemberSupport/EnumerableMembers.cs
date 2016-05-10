using System;
using System.Collections.Generic;
using System.Linq.Expressions;
using System.Text;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Oracle.Plugins.MemberSupport
{
	internal class EnumerableMembers : IMemberMatcher
	{
		private delegate void MemberCallDelegate(
			MemberExpression memberAccess,
			StringBuilder queryBuilder,
			Action<Expression> visitExpression,
			QueryContext context);

		private static Dictionary<string, Dictionary<Type, MemberCallDelegate>> SupportedMembers;
		static EnumerableMembers()
		{
			SupportedMembers = new Dictionary<string, Dictionary<Type, MemberCallDelegate>>();
			var countDict = new Dictionary<Type, MemberCallDelegate>();
			countDict[typeof(List<>)] = GetLength;
			countDict[typeof(HashSet<>)] = GetLength;
			SupportedMembers["Count"] = countDict;
		}

		public bool TryMatch(MemberExpression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			Dictionary<Type, MemberCallDelegate> dict;
			var member = expression.Member;
			if (member.DeclaringType.IsGenericType && SupportedMembers.TryGetValue(member.Name, out dict))
			{
				MemberCallDelegate mcd;
				if (dict.TryGetValue(member.DeclaringType.GetGenericTypeDefinition(), out mcd))
				{
					mcd(expression, queryBuilder, visitExpression, context);
					return true;
				}
			}
			return false;
		}

		private static void GetLength(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("(SELECT COUNT(*) FROM TABLE(");
			visitExpression(memberCall.Expression);
			queryBuilder.Append("))");
		}
	}
}
