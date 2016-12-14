using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq.Expressions;
using System.Text;
using Remotion.Linq.Clauses;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ExpressionTreeVisitors;
using Remotion.Linq.Parsing;
using Revenj.Common;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors
{
	public class SubquerySelectExpressionTreeVisitor : ThrowingExpressionTreeVisitor
	{
		public static void ProcessExpression(Expression linqExpression, SubqueryParts query)
		{
			var visitor = new SubquerySelectExpressionTreeVisitor(query);
			visitor.VisitExpression(linqExpression);
		}

		private readonly SubqueryParts Query;

		private SubquerySelectExpressionTreeVisitor(SubqueryParts query)
		{
			this.Query = query;
		}

		private readonly HashSet<IQuerySource> ProcessedSources = new HashSet<IQuerySource>();

		protected override Expression VisitQuerySourceReferenceExpression(QuerySourceReferenceExpression expression)
		{
			var qs = expression.ReferencedQuerySource;

			if (Query.CanQueryInMemory)
			{
				if (ProcessedSources.Contains(qs))
					return expression;
				ProcessedSources.Add(qs);
				if (Query.ParentQuery.MainFrom.Equals(qs))
					return expression;
			}
			Query.AddSelectPart(qs, "\"{0}\"".With(qs.ItemName), qs.ItemName, qs.ItemType, null);

			return expression;
		}

		protected override Expression VisitNewExpression(NewExpression expression)
		{
			if (expression.CanReduce)
				return expression.Reduce();

			int len = expression.Arguments.Count;
			for (int i = 0; i < len; i++)
			{
				var arg = expression.Arguments[i];
				var memb = expression.Members[i];
				VisitExpression(arg);
				var last = Query.Selects[Query.Selects.Count - 1];
				var membName = Query.ConverterFactory.GetName(memb);
				if (last.Name != membName && !last.Sql.Contains(" AS "))
				{
					last.Name = membName;
					if (!Query.CanQueryInMemory)
						last.Sql += " AS \"{0}\"".With(membName);
				}
			}

			return Expression.New(expression.Constructor, expression.Arguments);
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

			var cnt = Query.Selects.Count;
			var expression = VisitExpression(memberAssigment.Expression);
			if (cnt != Query.Selects.Count)
				Query.Selects[cnt].Sql = "\"{0}\" AS \"{1}\"".With(Query.Selects[cnt].Name, Query.ConverterFactory.GetName(memberAssigment.Member));

			return Expression.Bind(memberAssigment.Member, expression);
		}

		protected override Expression VisitBinaryExpression(BinaryExpression expression)
		{
			VisitExpression(expression.Left);
			VisitExpression(expression.Right);

			return expression;
		}

		protected override Expression VisitConstantExpression(ConstantExpression expression)
		{
			if (!Query.CanQueryInMemory)
			{
				//TODO complex types?
				//TODO enum types
				Query.AddSelectPart(
					null,
					NpgsqlTypes.TypeConverter.Convert(expression.Type, expression.Value),
					null,
					expression.Type,
					null);
				Query.Selects[Query.Selects.Count - 1].Expression = expression;
			}
			return expression;
		}

		protected override Expression VisitUnaryExpression(UnaryExpression expression)
		{
			VisitExpression(expression.Operand);

			return expression;
		}

		protected override Expression VisitMemberExpression(MemberExpression expression)
		{
			VisitExpression(expression.Expression);

			if (!Query.CanQueryInMemory)
			{
				//TODO hack for subexpressions
				var last = Query.Selects[Query.Selects.Count - 1];
				var sb = new StringBuilder();
				foreach (var mm in Query.MemberMatchers)
					if (mm.TryMatch(expression, sb, exp => sb.Append(last.Sql), Query.Context))
						break;
				var name = Query.ConverterFactory.GetName(expression.Member);
				if (sb.Length > 0)
				{
					sb.Append(" AS \"").Append(name).Append("\"");
					last.Sql = sb.ToString();
				}
				else
				{
					last.Sql = "({0}).\"{1}\"".With(last.Sql, name);
					last.Name = name;
				}
				last.ItemType = expression.Type;
				last.Expression = expression;
			}
			return expression;
		}

		protected override Expression VisitSubQueryExpression(SubQueryExpression expression)
		{
			var sqParts = new SubqueryParts(Query, expression.QueryModel.SelectClause.Selector, Query.Context);
			var sq = SubqueryGeneratorQueryModelVisitor.ParseSubquery(expression.QueryModel, sqParts);
			if (!Query.CanQueryInMemory)
			{
				Query.AddSelectPart(
					null,
					"(" + sq.BuildSqlString(true) + ")",
					null,
					expression.QueryModel.ResultTypeOverride ?? expression.QueryModel.SelectClause.Selector.Type,
					null);
			}
			return expression;
		}

		protected override Expression VisitMethodCallExpression(MethodCallExpression expression)
		{
			//unsupported method can be run in memory
			if (Query.CanQueryInMemory)
			{
				Query.ShouldQueryInMemory = true;

				VisitExpression(expression.Object);

				foreach (var arg in expression.Arguments)
					VisitExpression(arg);

				return expression;
			}

			throw new FrameworkException("Unsupported method call: " + expression.Method.Name
				+ " in query " + FormattingExpressionTreeVisitor.Format(expression) + ".");
		}

		protected override Expression VisitConditionalExpression(ConditionalExpression expression)
		{
			if (!Query.CanQueryInMemory)
			{
				var caseWhen = Query.GetSqlExpression(expression);
				Query.AddSelectPart(null, caseWhen, null, expression.Type, null);
			}
			return expression;
		}

		// Called when a LINQ expression type is not handled above.
		protected override Exception CreateUnhandledItemException<T>(T unhandledItem, string visitMethod)
		{
			string itemText = FormatUnhandledItem(unhandledItem);
			var message = string.Format("The expression '{0}' (type: {1}) is not supported by this LINQ provider.", itemText, typeof(T));
			return new NotSupportedException(message);
		}

		private string FormatUnhandledItem<T>(T unhandledItem)
		{
			var itemAsExpression = unhandledItem as Expression;
			return itemAsExpression != null ? FormattingExpressionTreeVisitor.Format(itemAsExpression) : unhandledItem.ToString();
		}
	}
}