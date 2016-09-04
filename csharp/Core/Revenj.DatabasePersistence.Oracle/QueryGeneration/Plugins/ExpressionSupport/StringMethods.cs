using System;
using System.Collections.Generic;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Oracle.Plugins.ExpressionSupport
{
	internal class StringMethods : IExpressionMatcher
	{
		private delegate void MethodCallDelegate(
			MethodCallExpression methodCall,
			StringBuilder queryBuilder,
			Action<Expression> visitExpression,
			QueryContext context);

		private static Dictionary<MethodInfo, MethodCallDelegate> SupportedMethods;
		static StringMethods()
		{
			SupportedMethods = new Dictionary<MethodInfo, MethodCallDelegate>();
			SupportedMethods.Add(typeof(string).GetMethod("Equals", new[] { typeof(string) }), MatchStringEquals);
			SupportedMethods.Add(typeof(string).GetMethod("Equals", new[] { typeof(string), typeof(StringComparison) }), MatchStringEquals);
			SupportedMethods.Add(typeof(string).GetMethod("Contains", new[] { typeof(string) }), MatchStringContains);
			SupportedMethods.Add(typeof(string).GetMethod("StartsWith", new[] { typeof(string) }), MatchStringStartsWith);
			SupportedMethods.Add(typeof(string).GetMethod("StartsWith", new[] { typeof(string), typeof(StringComparison) }), MatchStringStartsWith);
			SupportedMethods.Add(typeof(string).GetMethod("EndsWith", new[] { typeof(string) }), MatchStringEndsWith);
			SupportedMethods.Add(typeof(string).GetMethod("EndsWith", new[] { typeof(string), typeof(StringComparison) }), MatchStringEndsWith);
			SupportedMethods.Add(typeof(string).GetMethod("ToUpper", new Type[0]), MatchStringToUpper);
			SupportedMethods.Add(typeof(string).GetMethod("ToUpperInvariant", new Type[0]), MatchStringToUpper);
			SupportedMethods.Add(typeof(string).GetMethod("ToLower", new Type[0]), MatchStringToLower);
			SupportedMethods.Add(typeof(string).GetMethod("ToLowerInvariant", new Type[0]), MatchStringToLower);
			SupportedMethods.Add(typeof(int).GetMethod("ToString", new Type[0]), ValueToString);
			SupportedMethods.Add(typeof(long).GetMethod("ToString", new Type[0]), ValueToString);
			SupportedMethods.Add(typeof(decimal).GetMethod("ToString", new Type[0]), ValueToString);
			SupportedMethods.Add(typeof(string).GetMethod("ToString", new Type[0]), ValueToString);
			SupportedMethods.Add(typeof(byte).GetMethod("ToString", new Type[0]), ValueToString);
			SupportedMethods.Add(typeof(Guid).GetMethod("ToString", new Type[0]), ValueToString);
			SupportedMethods.Add(typeof(double).GetMethod("ToString", new Type[0]), ValueToString);
			SupportedMethods.Add(typeof(float).GetMethod("ToString", new Type[0]), ValueToString);
			/*SupportedMethods.Add(typeof(StringExtensions).GetMethod("With", new Type[] { typeof(string), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(StringExtensions).GetMethod("With", new Type[] { typeof(string), typeof(object), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(StringExtensions).GetMethod("With", new Type[] { typeof(string), typeof(object), typeof(object), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(StringExtensions).GetMethod("With", new Type[] { typeof(string), typeof(object[]) }), WithFormatArray);
			SupportedMethods.Add(typeof(string).GetMethod("Format", new Type[] { typeof(string), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(string).GetMethod("Format", new Type[] { typeof(string), typeof(object), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(string).GetMethod("Format", new Type[] { typeof(string), typeof(object), typeof(object), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(string).GetMethod("Format", new Type[] { typeof(string), typeof(object[]) }), WithFormatArray);
			*/
			SupportedMethods.Add(typeof(string).GetMethod("Replace", new Type[] { typeof(string), typeof(string) }), ReplaceString);
			SupportedMethods.Add(typeof(string).GetMethod("Replace", new Type[] { typeof(char), typeof(char) }), ReplaceString);
			SupportedMethods.Add(typeof(string).GetMethod("IsNullOrEmpty", new[] { typeof(string) }), IsNullOrEmpty);
			SupportedMethods.Add(typeof(string).GetMethod("IsNullOrWhiteSpace", new[] { typeof(string) }), IsNullOrWhiteSpace);
			SupportedMethods.Add(typeof(string).GetMethod("Substring", new[] { typeof(int) }), SubstringFrom);
			SupportedMethods.Add(typeof(string).GetMethod("Substring", new[] { typeof(int), typeof(int) }), SubstringFromTo);
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			var mce = expression as MethodCallExpression;
			if (mce == null)
				return false;

			MethodCallDelegate mcd;
			if (SupportedMethods.TryGetValue(mce.Method, out mcd))
			{
				mcd(mce, queryBuilder, visitExpression, context);
				return true;
			}
			return false;
		}

		private static void Compare(bool before, bool after, MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			bool ignoreCase = false;
			ConstantExpression ce;
			if (methodCall.Arguments.Count == 2)
			{
				ce = methodCall.Arguments[1] as ConstantExpression;
				switch ((StringComparison)ce.Value)
				{
					case StringComparison.CurrentCulture:
					case StringComparison.InvariantCulture:
					case StringComparison.Ordinal:
						break;
					default:
						ignoreCase = true;
						break;
				}
			}
			if (context.InSelect)
				queryBuilder.Append(" CASE WHEN");
			else
				queryBuilder.Append("(");
			ce = methodCall.Object as ConstantExpression;
			if (ce != null)
			{
				if (ignoreCase)
					visitExpression(ConstantExpression.Constant((ce.Value as string).ToUpper(), ce.Type));
				else
					visitExpression(ce);
			}
			else
			{
				if (ignoreCase)
				{
					queryBuilder.Append(" UPPER(");
					visitExpression(methodCall.Object);
					queryBuilder.Append(")");
				}
				else
					visitExpression(methodCall.Object);
			}
			bool asLike = before || after;
			if (asLike)
				queryBuilder.Append(" LIKE ");
			else
				queryBuilder.Append(" = ");
			if (before)
				queryBuilder.Append("'%' || ");
			ce = methodCall.Arguments[0] as ConstantExpression;
			if (ce != null)
			{
				if (asLike)
				{
					var value = ce.Value as string;
					if (ignoreCase)
						value = value.ToUpper();
					value = value.Replace(@"\", @"\\").Replace(@"_", @"\_").Replace(@"%", @"\%");
					visitExpression(ConstantExpression.Constant(value, ce.Type));
				}
				else if (ignoreCase)
					visitExpression(ConstantExpression.Constant((ce.Value as string).ToUpper(), ce.Type));
				else
					visitExpression(ce);
			}
			else
			{
				if (asLike)
				{
					if (ignoreCase)
						queryBuilder.Append(" UPPER(");
					queryBuilder.Append("REPLACE(REPLACE(REPLACE(");
					visitExpression(methodCall.Object);
					queryBuilder.Append(@", '\','\\'), '_','\_'), '%','\%')");
					if (ignoreCase)
						queryBuilder.Append(")");
				}
				else if (ignoreCase)
				{
					queryBuilder.Append(" UPPER(");
					visitExpression(methodCall.Object);
					queryBuilder.Append(")");
				}
				else
					visitExpression(methodCall.Object);
			}
			if (after)
				queryBuilder.Append(" || '%'");
			if (asLike)
				queryBuilder.Append(@" ESCAPE '\' ");
			if (context.InSelect)
				queryBuilder.Append(" THEN 'Y' ELSE 'N' END");
			else
				queryBuilder.Append(")");
		}

		private static bool CheckForNull(Expression exp, StringBuilder queryBuilder, QueryContext context)
		{
			var ce = exp as ConstantExpression;
			if (ce == null || ce.Value != null)
				return false;
			if (context.InSelect)
				queryBuilder.Append("'N'");
			else
				queryBuilder.Append(" 0=1");
			return true;
		}

		private static void MatchStringEquals(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			if (!CheckForNull(methodCall.Arguments[0], queryBuilder, context))
				Compare(false, false, methodCall, queryBuilder, visitExpression, context);
		}

		private static void MatchStringContains(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			if (!CheckForNull(methodCall.Arguments[0], queryBuilder, context))
				Compare(true, true, methodCall, queryBuilder, visitExpression, context);
		}

		private static void MatchStringStartsWith(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			if (!CheckForNull(methodCall.Arguments[0], queryBuilder, context))
				Compare(false, true, methodCall, queryBuilder, visitExpression, context);
		}

		private static void MatchStringEndsWith(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			if (!CheckForNull(methodCall.Arguments[0], queryBuilder, context))
				Compare(true, false, methodCall, queryBuilder, visitExpression, context);
		}

		private static void MatchStringToUpper(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("UPPER(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(")");
		}

		private static void MatchStringToLower(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("LOWER(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(")");
		}

		//actually this is not correct because ToString uses regional settings, but let's ignore it for now
		private static void ValueToString(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("CAST(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(" AS VARCHAR(4000))");
		}
		/*
		private static void WithFormat(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, bool inSelect, bool inWhere)
		{
			queryBuilder.Append("format(");
			var index = queryBuilder.Length;
			visitExpression(methodCall.Arguments[0]);
			var count = methodCall.Arguments.Count - 1;
			var substr = queryBuilder.ToString(index, queryBuilder.Length - index);
			for (int i = 0; i < count; i++)
				substr = substr.Replace("{" + i + "}", "%" + (i + 1) + "$s");
			queryBuilder.Length = index;
			queryBuilder.Append(substr);
			for (int i = 0; i < count; i++)
			{
				queryBuilder.Append(",");
				visitExpression(methodCall.Arguments[i + 1]);
			}
			queryBuilder.Append(")");
		}

		private static void WithFormatArray(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, bool inSelect, bool inWhere)
		{
			queryBuilder.Append("format(");
			var index = queryBuilder.Length;
			visitExpression(methodCall.Arguments[0]);
			var args = (NewArrayExpression)methodCall.Arguments[1];
			var count = args.Expressions.Count;
			var substr = queryBuilder.ToString(index, queryBuilder.Length - index);
			for (int i = 0; i < count; i++)
				substr = substr.Replace("{" + i + "}", "%" + (i + 1) + "$s");
			queryBuilder.Length = index;
			queryBuilder.Append(substr);
			for (int i = 0; i < count; i++)
			{
				queryBuilder.Append(",");
				visitExpression(args.Expressions[i]);
			}
			queryBuilder.Append(")");
		}
		*/
		private static void ReplaceString(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("REPLACE(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(",");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(",");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(")");
		}

		private static void IsNullOrEmpty(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			//TODO check if length is greater than 0
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(" IS NULL ");
		}

		private static void IsNullOrWhiteSpace(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			//TODO different from C#
			queryBuilder.Append("TRIM(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(") IS NULL ");
		}

		private static void SubstringFrom(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("SUBSTR(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(",");
			var ce = methodCall.Arguments[0] as ConstantExpression;
			if (ce != null && ce.Type == typeof(int))
				queryBuilder.Append(1 + (int)ce.Value);
			else
			{
				queryBuilder.Append("1 + ");
				visitExpression(methodCall.Arguments[0]);
			}
			queryBuilder.Append(")");
		}

		private static void SubstringFromTo(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context)
		{
			queryBuilder.Append("SUBSTR(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(",");
			var ce = methodCall.Arguments[0] as ConstantExpression;
			if (ce != null && ce.Type == typeof(int))
				queryBuilder.Append(1 + (int)ce.Value);
			else
			{
				queryBuilder.Append("1 + ");
				visitExpression(methodCall.Arguments[0]);
			}
			queryBuilder.Append(",");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(")");
		}
	}
}
