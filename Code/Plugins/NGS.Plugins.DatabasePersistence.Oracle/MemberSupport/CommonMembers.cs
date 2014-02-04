using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using NGS.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;
using NGS.DatabasePersistence.Oracle.QueryGeneration.Visitors;

namespace NGS.Plugins.DatabasePersistence.Oracle.ExpressionSupport
{
	[Export(typeof(IMemberMatcher))]
	public class CommonMembers : IMemberMatcher
	{
		private delegate void MemberCallDelegate(MemberExpression memberAccess, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context);

		private static Dictionary<MemberInfo, MemberCallDelegate> SupportedMembers;
		static CommonMembers()
		{
			SupportedMembers = new Dictionary<MemberInfo, MemberCallDelegate>();
			SupportedMembers.Add(typeof(string).GetProperty("Length"), GetStringLength);
		}

		public bool TryMatch(MemberExpression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			MemberCallDelegate mcd;
			if (SupportedMembers.TryGetValue(expression.Member, out mcd))
			{
				mcd(expression, queryBuilder, visitExpression, context);
				return true;
			}
			return false;
		}

		private static void GetStringLength(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("length(");
			visitExpression(memberCall.Expression);
			queryBuilder.Append(")");
		}
	}
}
