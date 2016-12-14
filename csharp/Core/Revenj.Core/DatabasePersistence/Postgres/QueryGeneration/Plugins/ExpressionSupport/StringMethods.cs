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
	public class StringMethods : IExpressionMatcher
	{
		private delegate void MethodCallDelegate(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression);

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
			SupportedMethods.Add(typeof(StringExtensions).GetMethod("With", new Type[] { typeof(string), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(StringExtensions).GetMethod("With", new Type[] { typeof(string), typeof(object), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(StringExtensions).GetMethod("With", new Type[] { typeof(string), typeof(object), typeof(object), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(StringExtensions).GetMethod("With", new Type[] { typeof(string), typeof(object[]) }), WithFormatArray);
			SupportedMethods.Add(typeof(string).GetMethod("Format", new Type[] { typeof(string), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(string).GetMethod("Format", new Type[] { typeof(string), typeof(object), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(string).GetMethod("Format", new Type[] { typeof(string), typeof(object), typeof(object), typeof(object) }), WithFormat);
			SupportedMethods.Add(typeof(string).GetMethod("Format", new Type[] { typeof(string), typeof(object[]) }), WithFormatArray);
			SupportedMethods.Add(typeof(string).GetMethod("Replace", new Type[] { typeof(string), typeof(string) }), ReplaceString);
			SupportedMethods.Add(typeof(string).GetMethod("Replace", new Type[] { typeof(char), typeof(char) }), ReplaceString);
			SupportedMethods.Add(typeof(string).GetMethod("IsNullOrEmpty", new[] { typeof(string) }), IsNullOrEmpty);
			SupportedMethods.Add(typeof(string).GetMethod("IsNullOrWhiteSpace", new[] { typeof(string) }), IsNullOrWhiteSpace);
			SupportedMethods.Add(typeof(int).GetMethod("Parse", new[] { typeof(string) }), StringToInt);
			SupportedMethods.Add(typeof(long).GetMethod("Parse", new[] { typeof(string) }), StringToLong);
			SupportedMethods.Add(typeof(decimal).GetMethod("Parse", new[] { typeof(string) }), StringToDecimal);
			SupportedMethods.Add(typeof(double).GetMethod("Parse", new[] { typeof(string) }), StringToDouble);
			SupportedMethods.Add(typeof(float).GetMethod("Parse", new[] { typeof(string) }), StringToFloat);
			SupportedMethods.Add(typeof(Guid).GetMethod("Parse", new[] { typeof(string) }), StringToGuid);
			SupportedMethods.Add(typeof(string).GetMethod("Substring", new[] { typeof(int) }), SubstringFrom);
			SupportedMethods.Add(typeof(string).GetMethod("Substring", new[] { typeof(int), typeof(int) }), SubstringFromTo);
		}

		public bool TryMatch(Expression expression, StringBuilder queryBuilder, Action<Expression> visitExpression, QueryContext context, IPostgresConverterFactory converter)
		{
			var mce = expression as MethodCallExpression;
			if (mce == null)
				return false;

			MethodCallDelegate mcd;
			if (SupportedMethods.TryGetValue(mce.Method, out mcd))
			{
				mcd(mce, queryBuilder, visitExpression);
				return true;
			}
			return false;
		}

		private static readonly char[] EscapeChars = new[] { '\\', '%', '_' };

		private static void EscapeForLike(Expression exp, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			var ce = exp as ConstantExpression;
			if (ce != null)
			{
				var value = ce.Value as string;
				if (value.IndexOfAny(EscapeChars) >= 0)
					visitExpression(ConstantExpression.Constant(value.Replace(@"\", @"\\").Replace("_", "\\_").Replace("%", "\\%"), typeof(string)));
				else
					visitExpression(ce);
			}
			else
			{
				queryBuilder.Append(" REPLACE(REPLACE(REPLACE(");
				visitExpression(exp);
				queryBuilder.Append(@", '\','\\'), '_','\_'), '%','\%') ");
			}
		}

		private static bool CheckIfNull(Expression exp)
		{
			var ce = exp as ConstantExpression;
			return ce != null && ce.Value == null;
		}

		private static void ChooseComparison(MethodCallExpression methodCall, StringBuilder queryBuilder)
		{
			if (methodCall.Arguments.Count == 2)
			{
				var ce = methodCall.Arguments[1] as ConstantExpression;
				switch ((StringComparison)ce.Value)
				{
					case StringComparison.CurrentCulture:
					case StringComparison.InvariantCulture:
					case StringComparison.Ordinal:
						queryBuilder.Append(" LIKE ");
						break;
					default:
						queryBuilder.Append(" ILIKE ");
						break;
				}
			}
			else queryBuilder.Append(" LIKE ");
		}

		private static void MatchStringEquals(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			if (CheckIfNull(methodCall.Arguments[0]))
				queryBuilder.Append(" FALSE ");
			else
			{
				queryBuilder.Append("(");
				visitExpression(methodCall.Object);
				ChooseComparison(methodCall, queryBuilder);
				EscapeForLike(methodCall.Arguments[0], queryBuilder, visitExpression);
				queryBuilder.Append(")");
			}
		}

		private static void MatchStringContains(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			if (CheckIfNull(methodCall.Arguments[0]))
				queryBuilder.Append(" FALSE ");
			else
			{
				queryBuilder.Append("(");
				visitExpression(methodCall.Object);
				queryBuilder.Append(" LIKE '%' || ");
				EscapeForLike(methodCall.Arguments[0], queryBuilder, visitExpression);
				queryBuilder.Append(" || '%')");
			}
		}

		private static void MatchStringStartsWith(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			if (CheckIfNull(methodCall.Arguments[0]))
				queryBuilder.Append(" FALSE ");
			else
			{
				queryBuilder.Append("(");
				visitExpression(methodCall.Object);
				ChooseComparison(methodCall, queryBuilder);
				EscapeForLike(methodCall.Arguments[0], queryBuilder, visitExpression);
				queryBuilder.Append(" || '%')");
			}
		}

		private static void MatchStringEndsWith(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			if (CheckIfNull(methodCall.Arguments[0]))
				queryBuilder.Append(" FALSE ");
			else
			{
				queryBuilder.Append("(");
				visitExpression(methodCall.Object);
				ChooseComparison(methodCall, queryBuilder);
				queryBuilder.Append(" '%' || ");
				EscapeForLike(methodCall.Arguments[0], queryBuilder, visitExpression);
				queryBuilder.Append(" )");
			}
		}

		private static void MatchStringToUpper(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("upper(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(")");
		}

		private static void MatchStringToLower(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("lower(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(")");
		}

		//actually this is not correct because ToString uses regional settings, but let's ignore it for now
		private static void ValueToString(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			visitExpression(methodCall.Object);
			queryBuilder.Append("::text");
		}

		private static void WithFormat(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
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

		private static void WithFormatArray(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
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

		private static void ReplaceString(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("replace(");
			visitExpression(methodCall.Object);
			queryBuilder.Append(",");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(",");
			visitExpression(methodCall.Arguments[1]);
			queryBuilder.Append(")");
		}

		private static void IsNullOrEmpty(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("coalesce(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(",'') = ''");
		}

		private static void IsNullOrWhiteSpace(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("trim(both ' ' from coalesce(");
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append(",'')) = ''");
		}

		private static void StringToInt(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::int");
		}

		private static void StringToLong(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::bigint");
		}

		private static void StringToDecimal(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::numeric");
		}

		private static void StringToDouble(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::float");
		}

		private static void StringToFloat(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::real");
		}

		private static void StringToGuid(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			visitExpression(methodCall.Arguments[0]);
			queryBuilder.Append("::uuid");
		}

		private static void SubstringFrom(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("substr(");
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

		private static void SubstringFromTo(MethodCallExpression methodCall, StringBuilder queryBuilder, Action<Expression> visitExpression)
		{
			queryBuilder.Append("substr(");
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
