using System;
using System.Collections.Generic;
using System.Linq.Expressions;
using Remotion.Linq.Clauses;
using Remotion.Linq.Clauses.Expressions;
using Revenj.Common;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration
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

			return CreateResult(qs.ItemName, qs.ItemType, qs, parts);
		}

		public class Result
		{
			public IQuerySource QuerySource { get; set; }
			public string Name { get; set; }
			public Type Type { get; set; }
			public Func<object, BufferedTextReader, object> Instancer { get; set; }
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
						return CreateResult(qs.ItemName, type.GetGenericArguments()[0], qs, parts);
					else
						return CreateResult(qs.ItemName, qs.ItemType, qs, parts);
				}
				else if (fc.FromExpression is QuerySourceReferenceExpression)
				{
					return CreateResult(qs.ItemName, qs.ItemType, qs, parts);
				}
				else if (fc.FromExpression is SubQueryExpression)
				{
					var sqe = fc.FromExpression as SubQueryExpression;
					var source = GetOriginalSource(sqe.QueryModel.MainFromClause);
					return CreateResult(qs.ItemName, source.ItemType, qs, parts);
				}
			}
			return CreateResult(qs.ItemName, qs.ItemType, qs, parts);
		}

		private static Result CreateResult(FromClauseBase fromClause, QueryParts parts)
		{
			var ce = fromClause.FromExpression as ConstantExpression;
			if (ce != null)
			{
				var type = ce.Value.GetType();
				if (type.IsQueryable())
					return CreateResult(fromClause.ItemName, type.GetGenericArguments()[0], fromClause, parts);
				return CreateResult(fromClause.ItemName, fromClause.ItemType, fromClause, parts);
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
								return CreateResult(fromClause.ItemName, smf, fromClause, parts);
						}
					}
				}
				return CreateResult(fromClause.ItemName, fromClause.ItemType, fromClause, parts);
			}
			var sqe = fromClause.FromExpression as SubQueryExpression;
			if (sqe != null)
			{
				var source = GetOriginalSource(sqe.QueryModel.MainFromClause);
				if (fromClause.ItemType == source.ItemType
					|| fromClause.ItemType.IsAssignableFrom(source.ItemType)
					|| fromClause.ItemType.IsGrouping())
					return CreateResult(fromClause.ItemName, source.ItemType, fromClause, parts);
				return CreateProjector(fromClause.ItemName, fromClause, sqe, parts);
			}
			var nae = fromClause.FromExpression as NewArrayExpression;
			if (nae != null)
				return CreateResult(fromClause.ItemName, fromClause.ItemType, fromClause, parts);
			var me = fromClause.FromExpression as MemberExpression;
			if (me != null)
				return CreateResult(fromClause.ItemName, fromClause.ItemType, fromClause, parts);
			throw new NotImplementedException("Unknown from clause. Please provide feedback about missing feature.");
		}

		public static Result CreateResult(
			string name,
			Type type,
			IQuerySource qs,
			QueryParts parts)
		{
			var result = new Result { QuerySource = qs, Name = name, Type = type };
			var instanceFactory = parts.ConverterFactory.GetInstanceFactory(type);
			if (instanceFactory != null)
				result.Instancer = (value, reader) => instanceFactory(value, reader, parts.Locator);
			//TODO: custom converters
			else if (type.IsNullable())
			{
				var baseType = type.GetGenericArguments()[0];
				result.Instancer = (value, _) => value != null ? Convert.ChangeType(value, baseType) : null;
			}
			else result.Instancer = (value, _) => Convert.ChangeType(value, type);
			return result;
		}

		//TODO Func<object, object>
		private static Func<SubQueryExpression, QueryParts, Func<string, BufferedTextReader, object>> ProjectionMethod = ProjectExpression<object>;

		private static Func<string, BufferedTextReader, T> ProjectExpression<T>(SubQueryExpression sqe, QueryParts parts)
		{
			var mqp =
				new MainQueryParts(
					parts.Locator,
					parts.ConverterFactory,
					parts.Simplifications,
					parts.ExpressionMatchers,
					parts.MemberMatchers,
					parts.ProjectionMatchers);
			var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(sqe.QueryModel, mqp);

			var newExpression = sqe.QueryModel.SelectClause.Selector as NewExpression;
			if (newExpression != null)
				return ProjectNew<T>(newExpression, parts, subquery);
			return ProjectMapping<T>(sqe, parts, subquery);
		}

		private static Func<string, BufferedTextReader, T> ProjectMapping<T>(SubQueryExpression sqe, QueryParts parts, SubqueryParts subquery)
		{
			Func<string, BufferedTextReader, T> result;
			var projector = ProjectorBuildingExpressionTreeVisitor<T>.BuildProjector(sqe.QueryModel);
			if (subquery.Selects.Count == 1)
			{
				var sel = subquery.Selects[0];
				var factory = CreateResult(sel.Name, sel.ItemType, sel.QuerySource, parts);
				result = (value, reader) => (T)factory.Instancer(value, reader);
			}
			else
			{
				var ssd = SelectSubqueryData.Create(parts, subquery);
				//TODO TextReader/string !?
				result = (value, reader) =>
				{
					var items = PostgresRecordConverter.ParseRecord(value);
					var rom = ssd.ProcessRow(null, reader, items);
					var res = projector(rom);
					return res;
				};
			}
			return result;
		}

		private static Func<string, BufferedTextReader, T> ProjectNew<T>(NewExpression nex, QueryParts parts, SubqueryParts subquery)
		{
			var results = new Dictionary<Expression, Func<object, BufferedTextReader, object>>();
			var list = new List<Func<object, BufferedTextReader, object>>();
			foreach (var s in subquery.Selects)
			{
				var factory = CreateResult(s.Name, s.ItemType, s.QuerySource, parts);
				if (s.Expression == null)
					throw new FrameworkException("Null expression!?");
				results[s.Expression] = factory.Instancer;
				list.Add(factory.Instancer);
			}

			var arguments = new object[nex.Arguments.Count];

			Func<string, BufferedTextReader, T> result = (value, reader) =>
			{
				if (value == null)
					return default(T);
				var items = PostgresRecordConverter.ParseRecord(value);
				for (int i = 0; i < items.Length; i++)
					arguments[i] = list[i](items[i], reader);

				return (T)nex.Constructor.Invoke(arguments);
			};

			return result;
		}

		public static Result CreateProjector(
			string name,
			FromClauseBase fromClause,
			SubQueryExpression sqe,
			QueryParts parts)
		{
			var result = new Result { QuerySource = fromClause, Name = name, Type = fromClause.ItemType };
			var method = ProjectionMethod.Method.GetGenericMethodDefinition().MakeGenericMethod(fromClause.ItemType);
			var tempInst = method.Invoke(null, new object[] { sqe, parts });
			var instancer = tempInst as Func<string, BufferedTextReader, object>;
			if (instancer == null)
			{
				var invMethod = tempInst.GetType().GetMethod("Invoke");
				instancer = (value, reader) => invMethod.Invoke(tempInst, new object[] { value, reader });
			}
			result.Instancer = (value, reader) => instancer(value.ToString(), reader);
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
