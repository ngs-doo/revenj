using System;
using System.Collections.ObjectModel;
using System.Linq.Expressions;
using System.Text;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ExpressionTreeVisitors;
using Remotion.Linq.Parsing;
using Revenj.Common;
using Revenj.DatabasePersistence.Postgres.NpgsqlTypes;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DomainPatterns;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors
{
	public class SqlGeneratorExpressionTreeVisitor : ThrowingExpressionTreeVisitor
	{
		public static string GetSqlExpression(Expression linqExpression, QueryParts queryParts)
		{
			//TODO pass queryParts context!?
			return GetSqlExpression(linqExpression, queryParts, QueryContext.Standard);
		}

		public static string GetSqlExpression(Expression linqExpression, QueryParts queryParts, QueryContext context)
		{
			var visitor = new SqlGeneratorExpressionTreeVisitor(queryParts, context);
			visitor.VisitExpression(linqExpression);

			return visitor.SqlExpression.ToString();
		}

		private readonly StringBuilder SqlExpression = new StringBuilder();
		private readonly QueryParts QueryParts;
		private readonly QueryContext Context;

		private SqlGeneratorExpressionTreeVisitor(QueryParts queryParts, QueryContext context)
		{
			this.QueryParts = queryParts;
			this.Context = context;
		}

		protected override Expression VisitQuerySourceReferenceExpression(QuerySourceReferenceExpression expression)
		{
			SqlExpression.AppendFormat("(\"{0}\")", expression.ReferencedQuerySource.ItemName);
			return expression;
		}

		protected override Expression VisitUnaryExpression(UnaryExpression expression)
		{
			if (expression.NodeType == ExpressionType.Not)
				SqlExpression.Append(" NOT (");

			VisitExpression(expression.Operand);

			if (expression.NodeType == ExpressionType.Not)
				SqlExpression.Append(")");

			return expression;
		}

		private static bool IsNullExpression(Expression expression)
		{
			var ce = expression as ConstantExpression;
			if (ce != null && ce.Value == null)
				return true;
			var un = expression as UnaryExpression;
			if (un != null)
				return IsNullExpression(un.Operand);
			var pe = expression as PartialEvaluationExceptionExpression;
			if (pe != null)
				return IsNullExpression(pe.EvaluatedExpression);
			return false;
		}

		private static ConstantExpression ConvertToEnum(UnaryExpression ua, ConstantExpression ce)
		{
			var type = ua.Operand.Type;
			if (ce == null || !type.IsEnum)
				return null;
			return Expression.Constant(Enum.ToObject(type, (int)ce.Value), type);
		}

		protected override Expression VisitBinaryExpression(BinaryExpression expression)
		{
			SqlExpression.Append(" ");

			foreach (var candidate in QueryParts.ExpressionMatchers)
				if (candidate.TryMatch(expression, SqlExpression, exp => VisitExpression(exp), Context, QueryParts.ConverterFactory))
					return expression;

			var left = expression.Left;
			var right = expression.Right;

			if (left.Type == typeof(int) && right.Type == typeof(int)
				&& (left.NodeType == ExpressionType.Convert && right.NodeType == ExpressionType.Constant
					|| left.NodeType == ExpressionType.Constant && right.NodeType == ExpressionType.Convert))
			{
				var ual = left as UnaryExpression;
				var uar = right as UnaryExpression;
				if (ual != null)
				{
					var ce = ConvertToEnum(ual, right as ConstantExpression);
					if (ce != null) right = ce;
				}
				else if (uar != null)
				{
					var ce = ConvertToEnum(uar, left as ConstantExpression);
					if (ce != null) left = ce;
				}
			}

			switch (expression.NodeType)
			{
				case ExpressionType.Coalesce:
					SqlExpression.Append(" (COALESCE(");
					VisitExpression(left);
					SqlExpression.Append(", ");
					VisitExpression(right);
					SqlExpression.Append("))");
					return expression;
			}

			SqlExpression.Append("(");

			var nullLeft = IsNullExpression(left);
			var nullRight = IsNullExpression(right);
			if ((expression.NodeType == ExpressionType.NotEqual || expression.NodeType == ExpressionType.Equal)
				&& (nullLeft || nullRight))
			{
				if (expression.NodeType == ExpressionType.NotEqual)
					SqlExpression.Append("(NOT ");
				if (nullRight)
					VisitExpression(left);
				else
					VisitExpression(right);
				SqlExpression.Append(" IS NULL)");
				if (expression.NodeType == ExpressionType.NotEqual)
					SqlExpression.Append(")");
				return expression;
			}

			VisitExpression(left);

			// In production code, handle this via lookup tables.
			switch (expression.NodeType)
			{
				case ExpressionType.Equal:
					SqlExpression.Append(" = ");
					break;

				case ExpressionType.NotEqual:
					SqlExpression.Append(" <> ");
					break;

				case ExpressionType.AndAlso:
				case ExpressionType.And:
					SqlExpression.Append(" AND ");
					break;

				case ExpressionType.OrElse:
				case ExpressionType.Or:
					SqlExpression.Append(" OR ");
					break;

				case ExpressionType.Add:
					//TODO: hack. move outside
					if (expression.Type == typeof(string) || expression.Type == typeof(TreePath) || expression.Type == typeof(TreePath?))
						SqlExpression.Append(" || ");
					else
						SqlExpression.Append(" + ");
					break;

				case ExpressionType.Subtract:
					SqlExpression.Append(" - ");
					break;

				case ExpressionType.Multiply:
					SqlExpression.Append(" * ");
					break;

				case ExpressionType.Divide:
					SqlExpression.Append(" / ");
					break;

				case ExpressionType.GreaterThan:
					SqlExpression.Append(" > ");
					break;

				case ExpressionType.GreaterThanOrEqual:
					SqlExpression.Append(" >= ");
					break;

				case ExpressionType.LessThan:
					SqlExpression.Append(" < ");
					break;

				case ExpressionType.LessThanOrEqual:
					SqlExpression.Append(" <= ");
					break;

				case ExpressionType.Modulo:
					SqlExpression.Append(" % ");
					break;

				default:
					base.VisitBinaryExpression(expression);
					break;
			}

			VisitExpression(right);

			if (expression.NodeType == ExpressionType.NotEqual)
			{
				SqlExpression.Append(" OR (");
				if (left is ConstantExpression || left is PartialEvaluationExceptionExpression)
					VisitExpression(right);
				else
					VisitExpression(left);
				SqlExpression.Append(" IS NULL)");
			}

			SqlExpression.Append(")");

			return expression;
		}

		protected override Expression VisitConditionalExpression(ConditionalExpression expression)
		{
			SqlExpression.Append("CASE WHEN ");
			VisitExpression(expression.Test);
			SqlExpression.Append(" THEN ");
			VisitExpression(expression.IfTrue);
			SqlExpression.Append(" ELSE ");
			VisitExpression(expression.IfFalse);
			SqlExpression.Append(" END");

			return expression;
		}

		protected override Expression VisitMemberExpression(MemberExpression expression)
		{
			foreach (var candidate in QueryParts.MemberMatchers)
				if (candidate.TryMatch(expression, SqlExpression, exp => VisitExpression(exp), Context))
					return expression;

			if (expression.Expression == null)
			{
				var member = expression.Member;
				SqlExpression.AppendFormat("\"{0}\".\"{1}.{2}\"()",
					member.DeclaringType.Namespace,
					member.DeclaringType.Name,
					member.Name);
			}
			else
			{
				//TODO query parts context!?
				if (!string.IsNullOrEmpty(Context.Name))
					SqlExpression.Append('(');
				VisitExpression(expression.Expression);
				if (!string.IsNullOrEmpty(Context.Name))
					SqlExpression.Append(')');
				SqlExpression.AppendFormat(".\"{0}\"", QueryParts.ConverterFactory.GetName(expression.Member));
			}
			return expression;
		}

		protected override Expression VisitMemberInitExpression(MemberInitExpression expression)
		{
			var newExpression = expression.NewExpression;

			var newBindings = VisitMemberBindingList(expression.Bindings);

			if (newExpression != expression.NewExpression || newBindings != expression.Bindings)
				return Expression.MemberInit(newExpression, newBindings);

			return Expression.New(newExpression.Constructor, newExpression.Arguments);
		}

		protected override ReadOnlyCollection<MemberBinding> VisitMemberBindingList(ReadOnlyCollection<MemberBinding> expressions)
		{
			return base.VisitMemberBindingList(expressions);
		}

		protected override MemberBinding VisitMemberAssignment(MemberAssignment memberAssigment)
		{
			if (memberAssigment.Expression.NodeType == ExpressionType.Constant)
				return Expression.Bind(memberAssigment.Member, memberAssigment.Expression);

			var expression = VisitExpression(memberAssigment.Expression);
			SqlExpression.AppendFormat(" AS {0}", QueryParts.ConverterFactory.GetName(memberAssigment.Member));

			return Expression.Bind(memberAssigment.Member, expression);
		}

		protected override Expression VisitConstantExpression(ConstantExpression expression)
		{
			var value = expression.Value;
			if (value != null)
			{
				var type = value.GetType();
				var source = SqlSourceAttribute.FindSource(type);
				var converter = QueryParts.ConverterFactory.GetSerializationFactory(type);
				if (converter != null)
				{
					SqlExpression.Append('\'').Append(converter(value).Replace("'", "''")).Append('\'');
					if (value is IIdentifiable || value is IEntity)
						SqlExpression.Append("::").Append(source);
					else if (value is ICloneable)
						SqlExpression.Append("::\"").Append(type.Namespace).Append("\".\"").Append(type.Name).Append('"');
					return expression;
				}
				else if (type.IsEnum)
				{
					SqlExpression.Append('\'').Append(value).Append("'::\"").Append(type.Namespace).Append("\".\"").Append(type.Name).Append('"');
					return expression;
				}
			}

			SqlExpression.Append(TypeConverter.Convert(expression.Type, expression.Value));

			return expression;
		}

		protected override Expression VisitNewExpression(NewExpression expression)
		{
			int len = expression.Arguments.Count;
			for (int i = 0; i < len; i++)
			{
				if (i > 0)
					SqlExpression.Append(", ");
				var arg = expression.Arguments[i];
				var memb = expression.Members[i];
				VisitExpression(arg);
				var name = "\"" + QueryParts.ConverterFactory.GetName(memb) + "\"";
				if (SqlExpression.Length < name.Length
					|| SqlExpression.ToString(SqlExpression.Length - name.Length, name.Length) != name)
					SqlExpression.Append(" AS ").Append(name);
			}

			return Expression.New(expression.Constructor, expression.Arguments);
		}

		//TODO provjeriti ovo sa where dijelom
		protected override Expression VisitMethodCallExpression(MethodCallExpression expression)
		{
			foreach (var candidate in QueryParts.ExpressionMatchers)
				if (candidate.TryMatch(expression, SqlExpression, exp => VisitExpression(exp), Context, QueryParts.ConverterFactory))
					return expression;

			var attr = expression.Method.GetCustomAttributes(typeof(DatabaseFunctionAttribute), false) as DatabaseFunctionAttribute[];
			if (attr != null && attr.Length == 1)
			{
				var df = attr[0];
				try
				{
					var em = Activator.CreateInstance(df.Call, new object[] { df.Function }) as IExpressionMatcher;
					if (em == null)
						throw new FrameworkException("DatabaseFunction attribute target is not an " + typeof(IExpressionMatcher).FullName);
					if (!em.TryMatch(expression, SqlExpression, exp => VisitExpression(exp), Context, QueryParts.ConverterFactory))
						throw new FrameworkException("DatabaseFunction could not match provided expression.");
					return expression;
				}
				catch (Exception ex)
				{
					throw new FrameworkException("Error executing DatabaseFunction", ex);
				}
			}

			throw new FrameworkException(@"Unsupported method call: " + FormattingExpressionTreeVisitor.Format(expression) + @". 
Method calls which don't have conversion to sql are available only in select part of query.");
		}

		protected override Expression VisitSubQueryExpression(SubQueryExpression expression)
		{
			var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(expression.QueryModel, QueryParts, false, Context);
			SqlExpression.AppendFormat("({0})", subquery.BuildSqlString(true));
			return expression;
		}

		protected override Expression VisitParameterExpression(ParameterExpression expression)
		{
			SqlExpression.Append(Context.Name).Append('"').Append(expression.Name).Append('"');
			return expression;
		}

		// Called when a LINQ expression type is not handled above.
		protected override Exception CreateUnhandledItemException<T>(T unhandledItem, string visitMethod)
		{
			string itemText = FormatUnhandledItem(unhandledItem);
			var message = "The expression '{0}' (type: {1}) is not supported by this LINQ provider.".With(itemText, typeof(T));
			return new NotSupportedException(message);
		}

		private string FormatUnhandledItem<T>(T unhandledItem)
		{
			var itemAsExpression = unhandledItem as Expression;
			return itemAsExpression != null ? FormattingExpressionTreeVisitor.Format(itemAsExpression) : unhandledItem.ToString();
		}
	}
}