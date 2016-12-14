using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using Revenj.DatabasePersistence.Postgres.Converters;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Postgres.Plugins.ExpressionSupport
{
	[Export(typeof(IExpressionMatcher))]
	public class DateTimeMethods : IExpressionMatcher
	{
		private delegate void MethodCallDelegate(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression);

		private static Dictionary<MethodInfo, MethodCallDelegate> ConstantSupportedMethods;
		private static Dictionary<MethodInfo, MethodCallDelegate> DynamicSupportedMethods;
		static DateTimeMethods()
		{
			ConstantSupportedMethods = new Dictionary<MethodInfo, MethodCallDelegate>();
			ConstantSupportedMethods.Add(typeof(DateTime).GetMethod("Add", new[] { typeof(TimeSpan) }), AddTimeSpan);
			ConstantSupportedMethods.Add(typeof(DateTime).GetMethod("Subtract", new[] { typeof(TimeSpan) }), SubtractTimeSpan);
			DynamicSupportedMethods = new Dictionary<MethodInfo, MethodCallDelegate>();
			DynamicSupportedMethods.Add(typeof(DateTime).GetMethod("AddDays", new[] { typeof(double) }), AddDays);
			DynamicSupportedMethods.Add(typeof(DateTime).GetMethod("AddHours", new[] { typeof(double) }), AddHours);
			DynamicSupportedMethods.Add(typeof(DateTime).GetMethod("AddMilliseconds", new[] { typeof(double) }), AddMilliseconds);
			DynamicSupportedMethods.Add(typeof(DateTime).GetMethod("AddMinutes", new[] { typeof(double) }), AddMinutes);
			DynamicSupportedMethods.Add(typeof(DateTime).GetMethod("AddMonths", new[] { typeof(int) }), AddMonths);
			DynamicSupportedMethods.Add(typeof(DateTime).GetMethod("AddSeconds", new[] { typeof(double) }), AddSeconds);
			DynamicSupportedMethods.Add(typeof(DateTime).GetMethod("AddYears", new[] { typeof(int) }), AddYears);
			DynamicSupportedMethods.Add(typeof(DateTime).GetMethod("Subtract", new[] { typeof(DateTime) }), SubtractDateTime);
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context, IPostgresConverterFactory converter)
		{
			var mce = expression as MethodCallExpression;
			if (mce == null)
				return false;

			MethodCallDelegate mcd;
			if (DynamicSupportedMethods.TryGetValue(mce.Method, out mcd))
			{
				mcd(mce, queryBuilder, visitExpression);
				return true;
			}
			if (ConstantSupportedMethods.TryGetValue(mce.Method, out mcd))
			{
				if (mce.Arguments[0] is ConstantExpression == false)
					return false;
				mcd(mce, queryBuilder, visitExpression);
				return true;
			}
			return false;
		}

		private static void Format(
			MethodCallExpression methodCall,
			StringBuilder queryBuilder,
			Action<Expression> visitExpression,
			string format)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(" + (");
			var ce = methodCall.Arguments[0] as ConstantExpression;
			if (ce != null)
				queryBuilder.Append('\'').Append(ce.Value).Append('\'');
			else
				visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(" || '").Append(format).Append("')::interval)");

		}

		private static void AddYears(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			Format(methodCall, queryBuilder, visitExpression, "y");
		}

		private static void AddMonths(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			Format(methodCall, queryBuilder, visitExpression, "mon");
		}

		private static void AddDays(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			Format(methodCall, queryBuilder, visitExpression, "d");
		}

		private static void AddHours(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			Format(methodCall, queryBuilder, visitExpression, "h");
		}

		private static void AddMinutes(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			Format(methodCall, queryBuilder, visitExpression, "m");
		}

		private static void AddSeconds(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			Format(methodCall, queryBuilder, visitExpression, "s");
		}

		private static void AddMilliseconds(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			Format(methodCall, queryBuilder, visitExpression, "ms");
		}

		private static void Format(
			MethodCallExpression methodCall,
			StringBuilder queryBuilder,
			Action<Expression> visitExpression,
			bool add)
		{
			queryBuilder.Append("(");
			visitExpression(methodCall.Object);
			var cs = (ConstantExpression)methodCall.Arguments[0];
			var ts = (TimeSpan)cs.Value;
			var value = new StringBuilder();
			if (ts.Days > 0)
				value.AppendFormat("{0} d ", ts.Days);
			if (ts.Hours > 0)
				value.AppendFormat("{0} h ", ts.Hours);
			if (ts.Minutes > 0)
				value.AppendFormat("{0} m ", ts.Minutes);
			if (ts.Seconds > 0)
				value.AppendFormat("{0} s ", ts.Seconds);
			if (ts.Milliseconds > 0)
				value.AppendFormat("{0} ms ", ts.Milliseconds);
			if (value.Length > 0)
				queryBuilder.AppendFormat(" {0} '{1}'::interval)", add ? "+" : "-", value.ToString());
			else
				queryBuilder.AppendFormat(")");
		}

		private static void AddTimeSpan(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			Format(methodCall, queryBuilder, visitExpression, true);
		}

		private static void SubtractTimeSpan(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			Format(methodCall, queryBuilder, visitExpression, false);
		}

		private static void SubtractDateTime(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("(");
			var ce = methodCall.Object as ConstantExpression;
			if (ce != null) ToDbTimestamp(queryBuilder, ce);
			else visitExpression(methodCall.Object);
			queryBuilder.Append("::timestamptz - ");
			ce = methodCall.Arguments[0] as ConstantExpression;
			if (ce != null) ToDbTimestamp(queryBuilder, ce);
			else visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(")");
		}

		private static void ToDbTimestamp(StringBuilder queryBuilder, ConstantExpression ce)
		{
			var dt = (DateTime)ce.Value;
			queryBuilder.Append("'");
			queryBuilder.Append(TimestampConverter.ToDatabase(dt));
			queryBuilder.Append("'::timestamptz");
		}
	}
}
