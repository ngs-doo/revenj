using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Oracle.Plugins.MemberSupport
{
	[Export(typeof(IMemberMatcher))]
	public class DateTimeMembers : IMemberMatcher
	{
		private delegate void MemberCallDelegate(MemberExpression memberAccess, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context);

		private static Dictionary<MemberInfo, MemberCallDelegate> SupportedMembers;
		static DateTimeMembers()
		{
			SupportedMembers = new Dictionary<MemberInfo, MemberCallDelegate>();
			/*SupportedMembers.Add(typeof(DateTime).GetProperty("Date"), GetDate);
			SupportedMembers.Add(typeof(DateTime).GetProperty("Day"), GetDay);
			SupportedMembers.Add(typeof(DateTime).GetProperty("DayOfWeek"), GetDayOfWeek);
			SupportedMembers.Add(typeof(DateTime).GetProperty("DayOfYear"), GetDayOfYear);
			SupportedMembers.Add(typeof(DateTime).GetProperty("Hour"), GetHour);
			SupportedMembers.Add(typeof(DateTime).GetProperty("Millisecond"), GetMillisecond);
			SupportedMembers.Add(typeof(DateTime).GetProperty("Minute"), GetMinute);
			SupportedMembers.Add(typeof(DateTime).GetProperty("Month"), GetMonth);
			SupportedMembers.Add(typeof(DateTime).GetProperty("Second"), GetSecond);
			SupportedMembers.Add(typeof(DateTime).GetProperty("TimeOfDay"), GetTimeOfDay);
			SupportedMembers.Add(typeof(DateTime).GetProperty("Year"), GetYear);
			SupportedMembers.Add(typeof(TimeSpan).GetProperty("Days"), GetTimeSpanDays);
			SupportedMembers.Add(typeof(TimeSpan).GetProperty("TotalDays"), GetTimeSpanTotalDays);
			SupportedMembers.Add(typeof(TimeSpan).GetProperty("TotalHours"), GetTimeSpanTotalHours);*/
			SupportedMembers.Add(typeof(DateTime).GetProperty("Now", BindingFlags.Static | BindingFlags.Public), CurrentTimestamp);
			SupportedMembers.Add(typeof(DateTime).GetProperty("UtcNow", BindingFlags.Static | BindingFlags.Public), CurrentTimestamp);
			SupportedMembers.Add(typeof(DateTime).GetProperty("Today", BindingFlags.Static | BindingFlags.Public), CurrentDate);
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
		/*
		private static void GetDate(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			visitExpression(memberCall.Expression);
			queryBuilder.AppendFormat("::date");
		}

		private static void GetTimeOfDay(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			visitExpression(memberCall.Expression);
			queryBuilder.AppendFormat("::time");
		}

		private static void Format(
			MemberExpression memberCall,
			StringBuilder queryBuilder,
			Action<Expression> visitExpression,
			string part)
		{
			queryBuilder.AppendFormat("extract({0} from ", part);
			visitExpression(memberCall.Expression);
			queryBuilder.AppendFormat(")::int");
		}

		private static void GetDayOfWeek(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			Format(memberCall, queryBuilder, visitExpression, "dow");
		}

		private static void GetDayOfYear(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			Format(memberCall, queryBuilder, visitExpression, "doy");
		}

		private static void GetYear(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			Format(memberCall, queryBuilder, visitExpression, "y");
		}

		private static void GetMonth(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			Format(memberCall, queryBuilder, visitExpression, "mon");
		}

		private static void GetDay(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			Format(memberCall, queryBuilder, visitExpression, "d");
		}

		private static void GetHour(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			Format(memberCall, queryBuilder, visitExpression, "h");
		}

		private static void GetMinute(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			Format(memberCall, queryBuilder, visitExpression, "m");
		}

		private static void GetSecond(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			Format(memberCall, queryBuilder, visitExpression, "s");
		}

		private static void GetMillisecond(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			Format(memberCall, queryBuilder, visitExpression, "ms");
			queryBuilder.Append("%1000");
		}

		private static void GetTimeSpanDays(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			GetTimeSpanTotalDays(memberCall, queryBuilder, visitExpression);
			queryBuilder.AppendFormat("::int");
		}

		private static void GetTimeSpanTotalDays(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("extract(day from ");
			visitExpression(memberCall.Expression);
			queryBuilder.AppendFormat(")");
		}

		private static void GetTimeSpanTotalHours(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("extract(day from ");
			visitExpression(memberCall.Expression);
			queryBuilder.AppendFormat(")::int * 24 + extract(hour from ");
			visitExpression(memberCall.Expression);
			queryBuilder.AppendFormat(")");
		}
		*/
		private static void CurrentTimestamp(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("CURRENT_TIMESTAMP");
		}

		private static void CurrentDate(MemberExpression memberCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("CURRENT_DATE");
		}
	}
}
