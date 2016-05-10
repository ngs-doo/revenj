using System;
using System.Collections.Generic;
using System.Linq.Expressions;
using System.Reflection;
using System.Security.Principal;
using System.Text;
using System.Threading;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Oracle.Plugins.MemberSupport
{
	internal class CommonMembers : IMemberMatcher
	{
		private delegate void MemberCallDelegate(MemberExpression memberAccess, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context);

		private static Dictionary<MemberInfo, MemberCallDelegate> SupportedMembers;
		static CommonMembers()
		{
			SupportedMembers = new Dictionary<MemberInfo, MemberCallDelegate>();
			SupportedMembers.Add(typeof(string).GetProperty("Length"), GetStringLength);
			SupportedMembers.Add(typeof(IIdentity).GetProperty("Name"), GetPrincipal);
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

		private static readonly MemberInfo CurrentPrincipal = typeof(Thread).GetMember("CurrentPrincipal", BindingFlags.Public | BindingFlags.Static)[0];

		private static void GetPrincipal(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			var me = memberCall.Expression as MemberExpression;
			if (me != null && me.NodeType == ExpressionType.MemberAccess)
			{
				var parent = me.Expression as MemberExpression;
				if (parent != null && parent.NodeType == ExpressionType.MemberAccess && parent.Member == CurrentPrincipal)
				{
					queryBuilder.Append("USER");
					return;
				}
			}
			throw new NotSupportedException("Invalid member call:" + memberCall);
		}
	}
}
