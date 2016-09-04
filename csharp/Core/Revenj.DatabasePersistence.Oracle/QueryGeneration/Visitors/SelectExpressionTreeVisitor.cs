using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using Remotion.Linq;
using Remotion.Linq.Clauses;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ExpressionTreeVisitors;
using Remotion.Linq.Clauses.ResultOperators;
using Remotion.Linq.Parsing;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors
{
	public class SelectGeneratorExpressionTreeVisitor : ThrowingExpressionTreeVisitor
	{
		public static void ProcessExpression(Expression linqExpression, MainQueryParts query, QueryModel queryModel)
		{
			var visitor =
				new SelectGeneratorExpressionTreeVisitor(
					query,
					queryModel.ResultOperators.Any(it => it is AllResultOperator));
			visitor.VisitExpression(linqExpression);
		}

		private readonly MainQueryParts Query;
		private readonly bool IsSimple;
		private readonly List<Expression> DataSources = new List<Expression>();

		private SelectGeneratorExpressionTreeVisitor(MainQueryParts query, bool simple)
		{
			this.Query = query;
			this.IsSimple = simple;
		}

		protected override Expression VisitQuerySourceReferenceExpression(QuerySourceReferenceExpression expression)
		{
			if (IsSimple)
			{
				var qs = expression.ReferencedQuerySource;
				Query.AddSelectPart(qs, qs.ItemName, qs.ItemName, typeof(bool), (_, dr) => dr.GetBoolean(0));
			}
			else
			{
				CreateSelector(expression.ReferencedQuerySource);
			}
			return expression;
		}

		class PropertySource : IQuerySource
		{
			public IQuerySource Parent;
			public PropertyInfo Property;

			public PropertySource(IQuerySource parent, PropertyInfo pi)
			{
				this.Parent = parent;
				this.Property = pi;
				this.ItemName = parent.ItemName + "." + pi.Name;
				this.ItemType = pi.PropertyType;
			}

			public string ItemName { get; set; }
			public Type ItemType { get; set; }
		}


		private void CreateSelector(IQuerySource qs)
		{
			var index = Query.CurrentSelectIndex;
			if (qs.ItemType.IsGrouping())
			{
				var factoryKey =
					QuerySourceConverterFactory.CreateResult(
						"Key",
						qs.ItemType.GetGenericArguments()[0],
						qs,
						Query,
						false);
				var factoryValue = QuerySourceConverterFactory.Create(qs, Query);
				var genericType = qs.ItemType.CreateGrouping();
				if (Query.AddSelectPart(
					qs,
					"\"{0}\".\"Key\" AS \"{0}.Key\", \"{0}\".\"Values\" AS \"{0}\"".With(factoryValue.Name),
					factoryValue.Name,
					qs.ItemType,
					(_, dr) =>
					{
						var keyInstance = factoryKey.Instancer(dr.GetValue(index));
						//TODO fix later
						return null;
						/*var array = dr.IsDBNull(index + 1) ? new string[0] : OracleRecordConverter.ParseArray(dr.GetString(index + 1));
						var valueInstances = array.Select(it => factoryValue.Instancer(it));
						return Activator.CreateInstance(genericType, keyInstance, valueInstances);*/
					}))
					Query.CurrentSelectIndex++;
			}
			else if (typeof(IOracleReader).IsAssignableFrom(qs.ItemType))
			{
				var props = qs.ItemType.GetProperties();
				var selectors = new PropertySource[props.Length];
				for (int i = 0; i < selectors.Length; i++)
				{
					var s = selectors[i] = new PropertySource(qs, props[i]);
					Query.AddSelectPart(
						s,
						"\"{0}\".\"{1}\" AS \"{0}.{1}\"".With(s.Parent.ItemName, s.Property.Name),
						s.ItemName,
						s.ItemType,
						null);
				}
				Query.AddSelectPart(
					qs,
					null,
					qs.ItemName,
					qs.ItemType,
					(_, dr) =>
					{
						var inst = (IOracleReader)Activator.CreateInstance(qs.ItemType);
						inst.Read(qs.ItemName, dr, Query.Locator);
						return inst;
					});
			}
			else
			{
				var factory = QuerySourceConverterFactory.Create(qs, Query);
				var ii = factory.CanBeNull && factory.AsValue;
				Query.AddSelectPart(
					factory.QuerySource,
					ii ? "CASE WHEN \"{0}\".URI IS NULL THEN NULL ELSE VALUE(\"{0}\") END".With(factory.Name)
						: factory.AsValue ? "VALUE(\"{0}\")".With(factory.Name)
						: "\"{0}\"".With(factory.Name),
					factory.Name,
					factory.Type,
					(_, dr) => dr.IsDBNull(index) ? null : factory.Instancer(dr.GetValue(index)));
			}
		}

		protected override Expression VisitNewExpression(NewExpression expression)
		{
			if (expression.CanReduce)
				return expression.Reduce();

			foreach (var arg in expression.Arguments)
				VisitExpression(arg);

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

			var expression = VisitExpression(memberAssigment.Expression);

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

			return expression;
		}

		protected override Expression VisitMethodCallExpression(MethodCallExpression expression)
		{
			VisitExpression(expression.Object);

			foreach (var arg in expression.Arguments)
				VisitExpression(arg);

			return expression;
		}

		protected override Expression VisitConditionalExpression(ConditionalExpression expression)
		{
			VisitExpression(expression.Test);
			VisitExpression(expression.IfTrue);
			VisitExpression(expression.IfFalse);

			return expression;
		}

		private readonly List<Expression> ProcessedExpressions = new List<Expression>();

		protected override Expression VisitSubQueryExpression(SubQueryExpression expression)
		{
			if (ProcessedExpressions.Contains(expression))
				return expression;

			if (DataSources.Count == 0)
			{
				DataSources.Add(Query.MainFrom.FromExpression);
				DataSources.AddRange(Query.Joins.Select(it => it.InnerSequence));
				DataSources.AddRange(Query.AdditionalJoins.Select(it => it.FromExpression));
			}

			var model = expression.QueryModel;

			if (model.BodyClauses.Count == 0
				&& DataSources.Contains(model.MainFromClause.FromExpression))
				return expression;

			ProcessedExpressions.Add(expression);

			foreach (var candidate in Query.ProjectionMatchers)
				if (candidate.TryMatch(expression, Query, exp => VisitExpression(exp)))
					return expression;

			var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(expression.QueryModel, Query, true, Query.ContextName, Query.Context.Select());

			var cnt = Query.CurrentSelectIndex;
			//TODO true or false
			var sql = subquery.BuildSqlString(true);

			var selector = model.SelectClause.Selector;
			var projector = (IExecuteFunc)Activator.CreateInstance(typeof(GenericProjection<>).MakeGenericType(selector.Type), model);
			var ssd = SelectSubqueryData.Create(Query, subquery);

			if (subquery.ResultOperators.Any(it => it is FirstResultOperator))
			{
				//TODO fix later
				Query.AddSelectPart(
					expression.QueryModel.MainFromClause,
					@"(SELECT ""{1}"" FROM ({2}) ""{1}"" WHERE RowNum = 1) AS ""{0}""".With(
						"_first_or_default_" + cnt,
						expression.QueryModel.MainFromClause.ItemName,
						sql),
					"_first_or_default_" + cnt,
					expression.QueryModel.ResultTypeOverride,
					(rom, dr) =>
					{
						if (dr.IsDBNull(cnt))
							return null;
						var tuple = new[] { dr.GetValue(cnt).ToString() };
						var result = ssd.ProcessRow(rom, tuple);
						return projector.Eval(result);
					});
			}
			else
			{
				//TODO fix later
				Query.AddSelectPart(
					expression.QueryModel.MainFromClause,
					@"(SELECT ARRAY_AGG(""{1}"") FROM ({2}) ""{1}"") AS ""{0}""".With(
						"_subquery_" + cnt,
						expression.QueryModel.MainFromClause.ItemName,
						sql),
					"_subquery_" + cnt,
					expression.QueryModel.ResultTypeOverride,
					(rom, dr) =>
					{
						//TODO fix later
						return null;
						/*
						string[] array;
						if (dr.IsDBNull(cnt))
							array = new string[0];
						else
						{
							var value = dr.GetValue(cnt);
							array = value is string[] ? value as string[] : OracleRecordConverter.ParseArray(dr.GetString(cnt));
						}
						var resultItems = new List<ResultObjectMapping>();
						array.Foreach(it => resultItems.Add(ssd.ProcessRow(rom, it)));
						return projector.Process(resultItems);*/
					});
			}
			return expression;
		}

		class GenericProjection<TOut> : IExecuteFunc
		{
			private readonly Func<ResultObjectMapping, TOut> Func;

			public GenericProjection(QueryModel model)
			{
				Func = ProjectorBuildingExpressionTreeVisitor<TOut>.BuildProjector(model);
			}

			public IQueryable Process(IEnumerable<ResultObjectMapping> items)
			{
				return items.Select(it => Func(it)).ToList().AsQueryable();
			}

			public object Eval(ResultObjectMapping item)
			{
				return Func(item);
			}
		}

		interface IExecuteFunc
		{
			IQueryable Process(IEnumerable<ResultObjectMapping> items);
			object Eval(ResultObjectMapping item);
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