using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq.Expressions;
using System.Text;
using Remotion.Linq.Clauses;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ExpressionTreeVisitors;
using Remotion.Linq.Parsing;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors
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
			if (typeof(IOracleReader).IsAssignableFrom(qs.ItemType))
			{
				//TODO: expand query
				Query.AddSelectPart(qs, "\"{0}\"".With(qs.ItemName), qs.ItemName, qs.ItemType, null);
			}
			else if (qs.ItemType.AsValue())
			{
				Query.AddSelectPart(qs, "VALUE(\"{0}\") AS \"{0}\"".With(qs.ItemName), qs.ItemName, qs.ItemType, null);
			}
			else
			{
				Query.AddSelectPart(qs, "\"{0}\"".With(qs.ItemName), qs.ItemName, qs.ItemType, null);
			}

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
				if (last.Name != memb.Name && !last.Sql.Contains(" AS "))
				{
					last.Name = memb.Name;
					if (!Query.CanQueryInMemory)
						last.Sql += " AS \"{0}\"".With(memb.Name);
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
				Query.Selects[cnt].Sql = "\"{0}\" AS \"{1}\"".With(Query.Selects[cnt].Name, memberAssigment.Member.Name);

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
				//TODO extract conversion to separate method and guard against nulls
				var factory = Query.ConverterFactory.GetStringFactory(expression.Type);
				Query.AddSelectPart(
					null,
					factory != null ? factory(expression.Value) : expression.Value.ToString(),
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
				//TODO even uglier hack if AS was used
				var asInd = last.Sql.IndexOf(" AS ");
				var plain = asInd > 0 ? last.Sql.Substring(0, asInd) : last.Sql;
				var ctx = new QueryContext(true, false, false);// Query.Context.Select();
				var sb = new StringBuilder();
				foreach (var mm in Query.MemberMatchers)
					if (mm.TryMatch(expression, sb, exp => sb.Append(plain), ctx))
						break;
				foreach (var mm in QueryParts.StaticMemberMatchers)
					if (mm.TryMatch(expression, sb, exp => sb.Append(plain), ctx))
						break;
				if (sb.Length > 0)
				{
					if (last.Name != null)//TODO: last.name!?
						sb.Append(" AS \"").Append(last.Name).Append("\"");
					else
						sb.Append(" AS \"").Append(expression.Member.Name).Append("\"");
					last.Sql = sb.ToString();
				}
				else
				{
					if (asInd > 0 && last.Name != null)//TODO: change last name!?
						last.Sql = "{0}.\"{1}\" AS \"{2}\"".With(plain, expression.Member.Name, last.Name);
					else
					{
						last.Sql = "{0}.\"{1}\"".With(last.Sql, expression.Member.Name);
						last.Name = expression.Member.Name;
					}
				}
				last.ItemType = expression.Type;
				last.Expression = expression;
			}
			return expression;
		}

		protected override Expression VisitSubQueryExpression(SubQueryExpression expression)
		{
			var sqParts = new SubqueryParts(Query, expression.QueryModel.SelectClause.Selector, Query.ContextName, Query.Context.Select());
			var sq = SubqueryGeneratorQueryModelVisitor.ParseSubquery(expression.QueryModel, sqParts, Query.ContextName, Query.Context.Select());
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

			throw new NotSupportedException("Unsupported method call: " + expression.Method.Name
				+ " in query " + FormattingExpressionTreeVisitor.Format(expression) + ".");
		}

		protected override Expression VisitConditionalExpression(ConditionalExpression expression)
		{
			if (!Query.CanQueryInMemory)
			{
				var ctx = new QueryContext(true, false, false);// Query.Context.Select();
				var caseWhen = Query.GetSqlExpression(expression, string.Empty, ctx);
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