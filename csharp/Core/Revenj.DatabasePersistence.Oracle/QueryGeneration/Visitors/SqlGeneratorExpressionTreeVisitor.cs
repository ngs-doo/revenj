using System;
using System.Collections.ObjectModel;
using System.Linq.Expressions;
using System.Reflection;
using System.Text;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ExpressionTreeVisitors;
using Remotion.Linq.Parsing;
using Revenj.Common;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors
{
	public class SqlGeneratorExpressionTreeVisitor : ThrowingExpressionTreeVisitor
	{
		public static string GetSqlExpression(Expression linqExpression, QueryParts queryParts)
		{
			//TODO pass queryParts context!?
			return GetSqlExpression(linqExpression, queryParts, string.Empty, queryParts.Context);
		}

		public static string GetSqlExpression(Expression linqExpression, QueryParts queryParts, string contextName, QueryContext context)
		{
			var visitor = new SqlGeneratorExpressionTreeVisitor(queryParts, contextName, context);

			visitor.VisitExpression(linqExpression);

			return visitor.SqlExpression.ToString();
		}

		private readonly StringBuilder SqlExpression = new StringBuilder();
		private readonly QueryParts QueryParts;
		private readonly string ContextName;
		private readonly QueryContext Context;

		private int Level;
		private int BinaryLevel;

		private SqlGeneratorExpressionTreeVisitor(QueryParts queryParts, string contextName, QueryContext context)
		{
			this.QueryParts = queryParts;
			this.ContextName = contextName;
			this.Context = context;
		}

		public override Expression VisitExpression(Expression expression)
		{
			try
			{
				Level++;
				return base.VisitExpression(expression);
			}
			finally
			{
				Level--;
			}
		}

		protected override Expression VisitQuerySourceReferenceExpression(QuerySourceReferenceExpression expression)
		{
			SqlExpression.AppendFormat("\"{0}\"", expression.ReferencedQuerySource.ItemName);
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

		protected override Expression VisitBinaryExpression(BinaryExpression expression)
		{
			SqlExpression.Append(" ");

			foreach (var candidate in QueryParts.ExpressionMatchers)
				if (candidate.TryMatch(expression, SqlExpression, exp => VisitExpression(exp), Context))
					return expression;
			foreach (var candidate in QueryParts.StaticExpressionMatchers)
				if (candidate.TryMatch(expression, SqlExpression, exp => VisitExpression(exp), Context))
					return expression;

			switch (expression.NodeType)
			{
				case ExpressionType.Coalesce:
					SqlExpression.Append(" COALESCE(");
					VisitExpression(expression.Left);
					SqlExpression.Append(", ");
					VisitExpression(expression.Right);
					SqlExpression.Append(")");
					return expression;
				case ExpressionType.Modulo:
					SqlExpression.Append(" MOD(");
					VisitExpression(expression.Left);
					SqlExpression.Append(", ");
					VisitExpression(expression.Right);
					SqlExpression.Append(")");
					return expression;
			}

			SqlExpression.Append("(");

			var nullLeft = IsNullExpression(expression.Left);
			var nullRight = IsNullExpression(expression.Right);
			if ((expression.NodeType == ExpressionType.NotEqual || expression.NodeType == ExpressionType.Equal)
				&& (nullLeft || nullRight))
			{
				if (expression.NodeType == ExpressionType.NotEqual)
					SqlExpression.Append("(NOT ");
				if (nullRight)
					VisitExpression(expression.Left);
				else
					VisitExpression(expression.Right);
				SqlExpression.Append(" IS NULL)");
				if (expression.NodeType == ExpressionType.NotEqual)
					SqlExpression.Append(")");
				return expression;
			}

			if (expression.NodeType == ExpressionType.Equal || expression.NodeType == ExpressionType.NotEqual)
				BinaryLevel = Level;

			VisitExpression(expression.Left);

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
					if (expression.Type == typeof(string))
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

				default:
					base.VisitBinaryExpression(expression);
					break;
			}

			VisitExpression(expression.Right);

			if (expression.NodeType == ExpressionType.NotEqual)
			{
				SqlExpression.Append(" OR (");
				if (expression.Left is ConstantExpression || expression.Left is PartialEvaluationExceptionExpression)
					VisitExpression(expression.Right);
				else
					VisitExpression(expression.Left);
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
			foreach (var candidate in QueryParts.StaticMemberMatchers)
				if (candidate.TryMatch(expression, SqlExpression, exp => VisitExpression(exp), Context))
					return expression;

			if (BinaryLevel == 0 && Level == 1 || BinaryLevel + 1 < Level)
			{
				var pi = expression.Member as PropertyInfo;
				if (pi != null && pi.PropertyType == typeof(bool))
					SqlExpression.Append("'Y' = ");
			}

			VisitExpression(expression.Expression);
			SqlExpression.AppendFormat(".\"{0}\"", expression.Member.Name);

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
			SqlExpression.AppendFormat(" AS {0}", memberAssigment.Member.Name);

			return Expression.Bind(memberAssigment.Member, expression);
		}

		protected override Expression VisitConstantExpression(ConstantExpression expression)
		{
			var value = expression.Value;
			var type = value != null ? value.GetType() : expression.Type;
			SqlExpression.Append(QueryParts.AddParameter(type, value, Context.CanUseParams));

			if ((type == typeof(bool) || type == typeof(bool?)) && (BinaryLevel == 0 && Level == 1 || BinaryLevel + 1 < Level))
				SqlExpression.Append(" = 'Y'");

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
				var name = "\"" + memb.Name + "\"";
				if (SqlExpression.Length < name.Length
					|| SqlExpression.ToString(SqlExpression.Length - name.Length, name.Length) != name)
					SqlExpression.Append(" AS ").Append(name);
			}

			return Expression.New(expression.Constructor, expression.Arguments);
		}

		//TODO check with where condition
		protected override Expression VisitMethodCallExpression(MethodCallExpression expression)
		{
			foreach (var candidate in QueryParts.ExpressionMatchers)
				if (candidate.TryMatch(expression, SqlExpression, exp => VisitExpression(exp), Context))
					return expression;
			foreach (var candidate in QueryParts.StaticExpressionMatchers)
				if (candidate.TryMatch(expression, SqlExpression, exp => VisitExpression(exp), Context))
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
					if (!em.TryMatch(expression, SqlExpression, exp => VisitExpression(exp), Context))
						throw new FrameworkException("DatabaseFunction could not match provided expression.");
					return expression;
				}
				catch (Exception ex)
				{
					throw new FrameworkException("Error executing DatabaseFunction", ex);
				}
			}

			throw new NotSupportedException(@"Unsupported method call: " + FormattingExpressionTreeVisitor.Format(expression) + @".
Method calls which don't have conversion to sql are available only in select part of query.");
		}

		protected override Expression VisitSubQueryExpression(SubQueryExpression expression)
		{
			//TODO select, where?
			var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(expression.QueryModel, QueryParts, false, ContextName, Context);
			SqlExpression.AppendFormat("({0})", subquery.BuildSqlString(true));
			return expression;
		}

		protected override Expression VisitParameterExpression(ParameterExpression expression)
		{
			SqlExpression.Append(ContextName).Append('"').Append(expression.Name).Append('"');
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