using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using Remotion.Linq.Clauses;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ResultOperators;
using Revenj.Common;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration
{
	public static class QuerySourceConverterFactory
	{
		public static Result Create(IQuerySource qs, QueryParts parts)
		{
			var qsre = qs as QuerySourceReferenceExpression;
			if (qsre != null)
				return CreateResult(qsre, parts);
			var fc = qs as FromClauseBase;
			if (fc != null)
				return CreateResult(fc, parts);

			return CreateResult(qs.ItemName, qs.ItemType, qs, parts, true);
		}

		public class Result
		{
			public IQuerySource QuerySource { get; set; }
			public string Name { get; set; }
			public Type Type { get; set; }
			public Func<object, object> Instancer { get; set; }
			public bool AsValue { get; set; }
			public bool CanBeNull { get; set; }
		}

		private static Result CreateResult(QuerySourceReferenceExpression expression, QueryParts parts)
		{
			var qs = expression.ReferencedQuerySource;

			var fc = qs as FromClauseBase;
			if (fc != null)
			{
				var ce = fc.FromExpression as ConstantExpression;
				if (ce != null)
				{
					var type = ce.Value.GetType();
					if (type.IsQueryable())
						return CreateResult(qs.ItemName, type.GetGenericArguments()[0], qs, parts, true);
					else
						return CreateResult(qs.ItemName, qs.ItemType, qs, parts, true);
				}
				else if (fc.FromExpression is QuerySourceReferenceExpression)
				{
					return CreateResult(qs.ItemName, qs.ItemType, qs, parts, true);
				}
				else if (fc.FromExpression is SubQueryExpression)
				{
					var sqe = fc.FromExpression as SubQueryExpression;
					var source = GetOriginalSource(sqe.QueryModel.MainFromClause);
					return CreateResult(qs.ItemName, source.ItemType, qs, parts, false);
				}
			}
			return CreateResult(qs.ItemName, qs.ItemType, qs, parts, true);
		}

		private static Result CreateResult(FromClauseBase fromClause, QueryParts parts)
		{
			var ce = fromClause.FromExpression as ConstantExpression;
			if (ce != null)
			{
				var type = ce.Value.GetType();
				if (type.IsQueryable())
					return CreateResult(fromClause.ItemName, type.GetGenericArguments()[0], fromClause, parts, true);
				return CreateResult(fromClause.ItemName, fromClause.ItemType, fromClause, parts, true);
			}
			var qsre = fromClause.FromExpression as QuerySourceReferenceExpression;
			if (qsre != null)
			{
				if (fromClause.ItemType.IsInterface && qsre.ReferencedQuerySource.ItemType.IsGrouping())
				{
					var mfc = qsre.ReferencedQuerySource as MainFromClause;
					if (mfc != null)
					{
						var sq = mfc.FromExpression as SubQueryExpression;
						if (sq != null)
						{
							var smf = sq.QueryModel.MainFromClause.ItemType;
							if (fromClause.ItemType.IsAssignableFrom(smf))
								return CreateResult(fromClause.ItemName, smf, fromClause, parts, false);
						}
					}
				}
				return CreateResult(fromClause.ItemName, fromClause.ItemType, fromClause, parts, true);
			}
			var sqe = fromClause.FromExpression as SubQueryExpression;
			if (sqe != null)
			{
				var source = GetOriginalSource(sqe.QueryModel.MainFromClause);
				//TODO: ugly hack to decide if VALUE(X) should be used or not
				var simplified = fromClause is MainFromClause
					? parts.TryToSimplifyMainFrom(fromClause as MainFromClause)
					: fromClause is AdditionalFromClause
					? parts.TryToSimplifyAdditionalFrom(fromClause as AdditionalFromClause)
					: null;
				if (fromClause.ItemType == source.ItemType
					|| fromClause.ItemType.IsAssignableFrom(source.ItemType)
					|| fromClause.ItemType.IsGrouping())
					return CreateResult(fromClause.ItemName, source.ItemType, fromClause, parts, source == simplified);
				return CreateProjector(fromClause.ItemName, fromClause, sqe, parts);
			}
			var nae = fromClause.FromExpression as NewArrayExpression;
			if (nae != null)
				return CreateResult(fromClause.ItemName, fromClause.ItemType, fromClause, parts, true);
			var me = fromClause.FromExpression as MemberExpression;
			if (me != null)
				return CreateResult(fromClause.ItemName, fromClause.ItemType, fromClause, parts, true);
			throw new NotImplementedException("Unknown from clause. Please provide feedback about missing feature.");
		}

		private static bool CanBeNull(IQuerySource qs)
		{
			if (qs is MainFromClause)
				return false;
			var afs = qs as AdditionalFromClause;
			if (afs != null)
			{
				var sqe = afs.FromExpression as SubQueryExpression;
				return sqe == null || sqe.QueryModel.ResultOperators.Any(it => it is DefaultIfEmptyResultOperator);
			}
			var jc = qs as JoinClause;
			if (jc != null)
			{
				var sqe = jc.InnerSequence as SubQueryExpression;
				return sqe == null || sqe.QueryModel.ResultOperators.Any(it => it is DefaultIfEmptyResultOperator);
			}
			return true;
		}

		public static Result CreateResult(
			string name,
			Type type,
			IQuerySource qs,
			QueryParts parts,
			bool canConvert)
		{
			var asValue = type.AsValue();
			var result =
				new Result
				{
					QuerySource = qs,
					Name = name,
					Type = type,
					AsValue = canConvert && asValue,
					CanBeNull = CanBeNull(qs)
				};
			if (asValue)
				result.Instancer = value => (value as IOracleDto).Convert(parts.Locator);
			else if (type.IsNullable())
			{
				var baseType = type.GetGenericArguments()[0];
				result.Instancer = value => value != null ? Convert.ChangeType(value, baseType) : null;
			}
			//TODO better conversion. using oracle converters
			else result.Instancer = value => Convert.ChangeType(value, type);
			return result;
		}

		//TODO Func<object, object>
		private static Func<SubQueryExpression, QueryParts, Func<string, object>> ProjectionMethod = ProjectExpression<object>;

		private static Func<string, T> ProjectExpression<T>(SubQueryExpression sqe, QueryParts parts)
		{
			var mqp =
				new MainQueryParts(
					parts.Locator,
					parts.ConverterFactory,
					parts.Simplifications,
					parts.ExpressionMatchers,
					parts.MemberMatchers,
					parts.ProjectionMatchers);
			var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(sqe.QueryModel, mqp, parts.ContextName, parts.Context.Select());

			var newExpression = sqe.QueryModel.SelectClause.Selector as NewExpression;
			if (newExpression != null)
				return ProjectNew<T>(newExpression, parts, subquery);
			return ProjectMapping<T>(sqe, parts, subquery);
		}

		private static Func<string, T> ProjectMapping<T>(SubQueryExpression sqe, QueryParts parts, SubqueryParts subquery)
		{
			Func<string, T> result;
			var projector = ProjectorBuildingExpressionTreeVisitor<T>.BuildProjector(sqe.QueryModel);
			if (subquery.Selects.Count == 1)
			{
				var sel = subquery.Selects[0];
				var factory = CreateResult(sel.Name, sel.ItemType, sel.QuerySource, parts, true);
				result = value => (T)factory.Instancer(value);
			}
			else
			{
				var ssd = SelectSubqueryData.Create(parts, subquery);
				result = value =>
				{
					//TODO fix later
					return default(T);
					/*
					var items = OracleRecordConverter.ParseRecord(value);
					var rom = ssd.ProcessRow(null, items);
					var res = projector(rom);
					return res;*/
				};
			}
			return result;
		}

		private static Func<string, T> ProjectNew<T>(NewExpression nex, QueryParts parts, SubqueryParts subquery)
		{
			var results = new Dictionary<Expression, Func<object, object>>();
			var list = new List<Func<object, object>>();
			foreach (var s in subquery.Selects)
			{
				var factory = CreateResult(s.Name, s.ItemType, s.QuerySource, parts, true);
				if (s.Expression == null)
					throw new FrameworkException("Null expression!?");
				results[s.Expression] = factory.Instancer;
				list.Add(factory.Instancer);
			}

			var arguments = new object[nex.Arguments.Count];

			Func<string, T> result = value =>
			{
				//TODO fix later
				//if (value == null)
				return default(T);
				/*var items = OracleRecordConverter.ParseRecord(value);
				for (int i = 0; i < items.Length; i++)
					arguments[i] = list[i](items[i]);

				return (T)nex.Constructor.Invoke(arguments);*/
			};

			return result;
		}

		public static Result CreateProjector(
			string name,
			FromClauseBase fromClause,
			SubQueryExpression sqe,
			QueryParts parts)
		{
			var type = fromClause.ItemType;
			var result =
				new Result
				{
					QuerySource = fromClause,
					Name = name,
					Type = type,
					AsValue = type.AsValue(),
					CanBeNull = true
				};
			var method = ProjectionMethod.Method.GetGenericMethodDefinition().MakeGenericMethod(type);
			var tempInst = method.Invoke(null, new object[] { sqe, parts });
			var instancer = tempInst as Func<string, object>;
			if (instancer == null)
			{
				var invMethod = tempInst.GetType().GetMethod("Invoke");
				instancer = value => invMethod.Invoke(tempInst, new object[] { value });
			}
			result.Instancer = value => instancer(value.ToString());
			return result;
		}

		public static IQuerySource GetOriginalSource(MainFromClause from)
		{
			if (from.ItemType.IsGrouping())
				return from;
			SubQueryExpression sqe = from.FromExpression as SubQueryExpression;
			while (sqe != null)
			{
				from = sqe.QueryModel.MainFromClause;
				sqe = from.FromExpression as SubQueryExpression;
			}
			return from;
		}
	}
}
