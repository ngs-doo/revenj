using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel.Composition;
using System.Linq.Expressions;
using System.Text;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Postgres.Plugins.ExpressionSupport
{
	[Export(typeof(IMemberMatcher))]
	public class EnumerableMembers : IMemberMatcher
	{
		private delegate void MemberCallDelegate(MemberExpression memberAccess, StringBuilder queryBuilder, Action<Expression> visitExpression);

		private static Dictionary<string, Dictionary<Type, MemberCallDelegate>> SupportedMembers;
		static EnumerableMembers()
		{
			SupportedMembers = new Dictionary<string, Dictionary<Type, MemberCallDelegate>>();
			var countDict = new Dictionary<Type, MemberCallDelegate>();
			countDict[typeof(List<>)] = GetLength;
			countDict[typeof(HashSet<>)] = GetLength;
			countDict[typeof(ReadOnlyCollection<>)] = GetLength;
			countDict[typeof(LinkedList<>)] = GetLength;
			countDict[typeof(Queue<>)] = GetLength;
			countDict[typeof(Stack<>)] = GetLength;
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
					mcd(expression, queryBuilder, visitExpression);
					return true;
				}
			}
			return false;
		}

		private static void GetLength(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("coalesce(array_upper(");
			visitExpression(memberCall.Expression);
			queryBuilder.Append(", 1), 0)");
		}
	}
}
