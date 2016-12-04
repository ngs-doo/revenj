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
	[Export(typeof(IMemberMatcher))]
	public class DictionaryMembers : IMemberMatcher
	{
		private delegate void MemberCallDelegate(MemberExpression memberAccess, StringBuilder queryBuilder, Action<Expression> visitExpression);

		private static readonly Dictionary<MemberInfo, MemberCallDelegate> SupportedMembers;
		static DictionaryMembers()
		{
			SupportedMembers = new Dictionary<MemberInfo, MemberCallDelegate>();
			SupportedMembers.Add(typeof(Dictionary<string, string>).GetProperty("Keys"), GetKeys);
			SupportedMembers.Add(typeof(Dictionary<string, string>).GetProperty("Values"), GetValues);
			SupportedMembers.Add(typeof(Dictionary<string, string>.KeyCollection).GetProperty("Count"), GetCount);
			SupportedMembers.Add(typeof(Dictionary<string, string>.ValueCollection).GetProperty("Count"), GetCount);
			SupportedMembers.Add(typeof(Dictionary<string, object>).GetProperty("Keys"), GetKeys);
			SupportedMembers.Add(typeof(Dictionary<string, object>).GetProperty("Values"), GetValues);
			SupportedMembers.Add(typeof(Dictionary<string, object>.KeyCollection).GetProperty("Count"), GetCount);
			SupportedMembers.Add(typeof(Dictionary<string, object>.ValueCollection).GetProperty("Count"), GetCount);
		}

		public bool TryMatch(MemberExpression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			MemberCallDelegate mcd;
			if (SupportedMembers.TryGetValue(expression.Member, out mcd))
			{
				mcd(expression, queryBuilder, visitExpression);
				return true;
			}
			return false;
		}

		private static void GetKeys(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("akeys(");
			visitExpression(memberCall.Expression);
			queryBuilder.AppendFormat(")");
		}

		private static void GetValues(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("avals(");
			visitExpression(memberCall.Expression);
			queryBuilder.AppendFormat(")");
		}

		private static void GetCount(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("array_upper(");
			visitExpression(memberCall.Expression);
			queryBuilder.AppendFormat(", 1)");
		}
	}
}
