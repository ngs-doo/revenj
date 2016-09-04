using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Linq;
using System.Linq.Expressions;
using System.Text;
using Remotion.Linq.Clauses;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ResultOperators;
using Revenj.Common;
using Revenj.DatabasePersistence.Oracle.Plugins.ExpressionSupport;
using Revenj.DatabasePersistence.Oracle.Plugins.MemberSupport;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.Visitors;
using Revenj.DomainPatterns;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition
{
	public abstract class QueryParts
	{
		internal static readonly IExpressionMatcher[] StaticExpressionMatchers = new IExpressionMatcher[]{
			new LikeStringComparison(),
			new LinqMethods(),
			new StringMethods()
		};
		internal static readonly IMemberMatcher[] StaticMemberMatchers = new IMemberMatcher[]{
			new CommonMembers(),
			new EnumerableMembers(),
			new DateTimeMembers()
		};

		public readonly IServiceProvider Locator;
		public readonly IOracleConverterFactory ConverterFactory;

		public readonly string ContextName;
		public readonly ParameterAggregator Parameters;
		public readonly QueryContext Context;

		public int CurrentSelectIndex { get; set; }
		public readonly List<SelectSource> Selects = new List<SelectSource>();
		public MainFromClause MainFrom { get; protected set; }
		public readonly List<JoinClause> Joins = new List<JoinClause>();
		public readonly List<AdditionalFromClause> AdditionalJoins = new List<AdditionalFromClause>();
		public readonly List<GroupJoinClause> GroupJoins = new List<GroupJoinClause>();
		public readonly List<Expression> Conditions = new List<Expression>();
		public readonly List<OrderByClause> OrderBy = new List<OrderByClause>();
		public readonly List<ResultOperatorBase> ResultOperators = new List<ResultOperatorBase>();

		internal readonly List<IQuerySimplification> Simplifications;
		internal readonly IEnumerable<IExpressionMatcher> ExpressionMatchers;
		internal readonly IEnumerable<IMemberMatcher> MemberMatchers;
		internal readonly IEnumerable<IProjectionMatcher> ProjectionMatchers;

		protected QueryParts(
			IServiceProvider locator,
			string contextName,
			IOracleConverterFactory factory,
			ParameterAggregator parameters,
			QueryContext context,
			IEnumerable<IQuerySimplification> simplifications,
			IEnumerable<IExpressionMatcher> expressionMatchers,
			IEnumerable<IMemberMatcher> memberMatchers,
			IEnumerable<IProjectionMatcher> projectionMatchers)
		{
			this.Locator = locator;
			this.ConverterFactory = factory;
			this.Parameters = parameters;
			this.Context = context;
			this.Simplifications = new List<IQuerySimplification>(simplifications);
			this.ExpressionMatchers = expressionMatchers;
			this.MemberMatchers = memberMatchers;
			this.ProjectionMatchers = projectionMatchers;
			this.ContextName = contextName;
		}

		public class SelectSource
		{
			public IQuerySource QuerySource { get; set; }
			public Expression Expression { get; set; }
			public string Sql { get; set; }
			public string Name { get; set; }
			public Type ItemType { get; set; }
			public Func<ResultObjectMapping, IDataReader, object> Instancer { get; set; }
		}

		public bool AddSelectPart(IQuerySource qs, string sql, string name, Type type, Func<ResultObjectMapping, IDataReader, object> instancer)
		{
			if (Selects.Any(kv => kv.Name == name))
				return false;
			Selects.Add(new SelectSource { QuerySource = qs, Sql = sql, Name = name, ItemType = type, Instancer = instancer });
			if (sql != null)
				CurrentSelectIndex++;
			return true;
		}

		public void SetFrom(MainFromClause from)
		{
			MainFrom = from;
			if (from != QuerySourceConverterFactory.GetOriginalSource(from))
				MainFrom = TryToSimplifyMainFrom(MainFrom);
		}

		internal MainFromClause TryToSimplifyMainFrom(MainFromClause from)
		{
			var name = from.ItemName;
			var sqe = from.FromExpression as SubQueryExpression;
			while (sqe != null)
			{
				var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(sqe.QueryModel, this, ContextName, Context.Select());
				if (subquery.Conditions.Count > 0
					|| subquery.Joins.Count > 0
					|| subquery.ResultOperators.Any(it => it is CastResultOperator == false && it is DefaultIfEmptyResultOperator == false)
					|| subquery.AdditionalJoins.Count > 0)
					return from;
				from = sqe.QueryModel.MainFromClause;
				sqe = from.FromExpression as SubQueryExpression;
			}
			from.ItemName = name;
			return from;
		}

		internal FromClauseBase TryToSimplifyAdditionalFrom(AdditionalFromClause additionalFrom)
		{
			FromClauseBase from = additionalFrom;
			var sqe = from.FromExpression as SubQueryExpression;
			if (sqe != null)
			{
				var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(sqe.QueryModel, this, ContextName, Context.Select());
				if (subquery.Joins.Count > 0
					|| subquery.ResultOperators.Any(it => it is CastResultOperator == false && it is DefaultIfEmptyResultOperator == false)
					|| subquery.AdditionalJoins.Count > 0)
					return from;
				return TryToSimplifyMainFrom(sqe.QueryModel.MainFromClause);
			}
			return from;
		}

		public void AddJoin(JoinClause join)
		{
			Joins.Add(join);
		}

		public void AddJoin(AdditionalFromClause join)
		{
			AdditionalJoins.Add(join);
		}

		public void AddJoin(GroupJoinClause join)
		{
			GroupJoins.Add(join);
		}

		public void AddCondition(Expression condition)
		{
			var cc = condition as ConstantExpression;
			if (cc == null || cc.Type != typeof(bool) || !(bool)cc.Value)
				Conditions.Add(condition);
		}

		public void AddOrderBy(OrderByClause orderBy)
		{
			OrderBy.Add(orderBy);
		}

		public void AddResultOperator(ResultOperatorBase resultOperator)
		{
			if (resultOperator is CastResultOperator == false)
				ResultOperators.Add(resultOperator);
		}

		public string GetSqlExpression(Expression expression)
		{
			return GetSqlExpression(expression, string.Empty, Context);
		}

		public string GetSqlExpression(Expression expression, string contextName, QueryContext context)
		{
			return SqlGeneratorExpressionTreeVisitor.GetSqlExpression(expression, this, contextName, context);
		}

		public string GetFromPart()
		{
			if (MainFrom == null)
				throw new InvalidOperationException("A query must have a from part");

			var sb = new StringBuilder();

			var mainFromQuery = GetQuerySourceFromExpression(MainFrom.ItemName, MainFrom.ItemType, MainFrom.FromExpression);
			if (AdditionalJoins.Any(it =>
			{
				var me = it.FromExpression as MemberExpression;
				return me != null && me.Expression is QuerySourceReferenceExpression;
			}))
			{
				if (MainFrom.ItemType.AsValue())
				{
					sb.Append("FROM ").Append(mainFromQuery);
					sb.AppendLine();
					sb.AppendFormat("INNER JOIN ({0}) sq$ ON sq$.id$ = \"{1}\".ROWID", GetInnerFromPart(true, mainFromQuery), MainFrom.ItemName);
				}
				else
				{
					sb.Append("FROM (").Append(GetInnerFromPart(false, mainFromQuery)).Append(") sq");
				}
			}
			else
			{
				sb.Append("FROM ").Append(mainFromQuery);
			}

			var emptyJoins =
				(from j in AdditionalJoins
				 let sqe = j.FromExpression as SubQueryExpression
				 where sqe != null
				 && sqe.QueryModel.ResultOperators.Count == 1
				 && sqe.QueryModel.ResultOperators[0] is DefaultIfEmptyResultOperator
				 select new { j, sqe })
				 .ToList();

			var groupPairs =
				(from aj in emptyJoins
				 let mfe = aj.sqe.QueryModel.MainFromClause.FromExpression as QuerySourceReferenceExpression
				 where mfe != null
				 select new { aj.j, g = mfe.ReferencedQuerySource })
				 .ToList();

			foreach (var aj in AdditionalJoins)
			{
				var me = aj.FromExpression as MemberExpression;
				if (me != null && me.Expression is QuerySourceReferenceExpression)
					continue;
				if (groupPairs.Any(it => it.j == aj))
					continue;
				var ej = emptyJoins.Find(it => it.j == aj);
				if (ej != null)
				{
					var qm = ej.sqe.QueryModel;
					var sel = qm.SelectClause.Selector as QuerySourceReferenceExpression;
					if (sel != null && sel.ReferencedQuerySource.Equals(qm.MainFromClause) && qm.BodyClauses.Count > 0)
					{
						var wc = qm.BodyClauses.Where(it => it is WhereClause).Cast<WhereClause>().ToList();
						if (wc.Count == qm.BodyClauses.Count)
						{
							var mfc = qm.MainFromClause;
							mfc.ItemName = aj.ItemName;
							sb.AppendFormat("{0}	LEFT JOIN {1} ON {2}",
								Environment.NewLine,
								GetQuerySourceFromExpression(mfc.ItemName, mfc.ItemType, mfc.FromExpression),
								string.Join(" AND ", wc.Select(it => GetSqlExpression(it.Predicate))));
							continue;
						}
					}
				}
				sb.AppendFormat("{0}	CROSS JOIN {1}",
					Environment.NewLine,
					GetQuerySourceFromExpression(aj.ItemName, aj.ItemType, aj.FromExpression));
			}
			if (Joins.Count > 0)
				Joins.ForEach(it => sb.AppendFormat("{0}	INNER JOIN {1} ON ({2}) = ({3}){0}",
														Environment.NewLine,
														GetQuerySourceFromExpression(it.ItemName, it.ItemType, it.InnerSequence),
														GetSqlExpression(it.InnerKeySelector),
														GetSqlExpression(it.OuterKeySelector)));
			foreach (var gj in GroupJoins)
			{
				var aj = groupPairs.FirstOrDefault(it => it.g == gj);
				if (aj == null)
					throw new FrameworkException("Can't find group join part!");
				gj.ItemName = aj.j.ItemName;
				gj.JoinClause.ItemName = aj.j.ItemName;
				sb.AppendFormat("{0}	LEFT JOIN {1} ON ({2}) = ({3}){0}",
					Environment.NewLine,
					GetQuerySourceFromExpression(gj.ItemName, gj.ItemType, gj.JoinClause.InnerSequence),
					GetSqlExpression(gj.JoinClause.InnerKeySelector),
					GetSqlExpression(gj.JoinClause.OuterKeySelector));
			}

			sb.AppendLine();
			return sb.ToString();
		}

		private string GetInnerFromPart(bool asValue, string mainFromQuery)
		{
			var sb = new StringBuilder("SELECT ");
			if (asValue)
				sb.AppendFormat("\"{0}\".ROWID as id$", MainFrom.ItemName);
			else
				sb.AppendFormat("\"{0}\"", MainFrom.ItemName);

			foreach (var aj in AdditionalJoins)
			{
				var me = aj.FromExpression as MemberExpression;
				if (me != null)
				{
					var qsre = me.Expression as QuerySourceReferenceExpression;
					if (qsre != null)
						sb.Append(", \"").Append(aj.ItemName).Append("\".OBJECT_VALUE AS \"").Append(aj.ItemName).Append("\"");
				}
			}

			sb.Append(" FROM ").Append(mainFromQuery);
			foreach (var aj in AdditionalJoins)
			{
				var me = aj.FromExpression as MemberExpression;
				if (me != null)
				{
					var qsre = me.Expression as QuerySourceReferenceExpression;
					if (qsre != null)
					{
						sb.AppendLine();
						sb.AppendFormat(" CROSS JOIN TABLE(\"{0}\".\"{1}\") \"{2}\"", qsre.ReferencedQuerySource.ItemName, me.Member.Name, aj.ItemName);
					}
				}
			}

			return sb.ToString();
		}

		public string GetWherePart()
		{
			if (Conditions.Count == 0)
				return string.Empty;

			var whereConditions = new List<string>();

			Conditions.ForEach(it => whereConditions.Add(GetSqlExpression(it, ContextName, Context.Where())));

			return @"WHERE
	{0}
".With(string.Join(Environment.NewLine + "	AND ", whereConditions));
		}

		public string GetOrderPart()
		{
			return OrderBy.Count == 0
				? string.Empty
				: @"ORDER BY 
	{0}
".With(string.Join(@",
	", OrderBy.Last().Orderings.Select(o => GetSqlExpression(o.Expression) + (o.OrderingDirection == OrderingDirection.Desc ? " DESC " : " ASC "))));
		}

		//TODO vjerojatno ponekad ne treba ignorirati expression
		public string GetQuerySourceFromExpression(string name, Type type, Expression fromExpression)
		{
			var me = fromExpression as MemberExpression;
			if (me != null)
			{
				var qse = me.Expression as QuerySourceReferenceExpression;
				if (qse != null)
					return @"TABLE(""{0}"".""{1}"") ""{2}""".With(
						qse.ReferencedQuerySource.ItemName,
						me.Member.Name,
						name);
			}

			var sqe = fromExpression as SubQueryExpression;
			if (sqe != null)
			{
				if (sqe.QueryModel.CanUseMain())
					return GetQuerySourceFromExpression(name, type, sqe.QueryModel.MainFromClause.FromExpression);
				//TODO hack za replaceanje generiranog id-a
				var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(sqe.QueryModel, this, ContextName, Context.Select());
				var grouping = sqe.QueryModel.ResultOperators.FirstOrDefault(it => it is GroupResultOperator) as GroupResultOperator;
				if (grouping == null && subquery.Selects.Count == 1)
				{
					if (sqe.QueryModel.ResultOperators.Any(it => it is UnionResultOperator || it is ConcatResultOperator))
					{
						var ind = subquery.Selects[0].Sql.IndexOf(" AS ");
						if (ind > 0)
						{
							var asName = subquery.Selects[0].Sql.Substring(ind + 4).Trim().Replace("\"", "");
							if (asName != name)
								subquery.Selects[0].Sql = subquery.Selects[0].Sql.Substring(0, ind + 4) + "\"" + name + "\"";
						}
						else
						{
							subquery.Selects[0].Sql = subquery.Selects[0].Sql + " AS \"" + name + "\"";
						}
						return "(" + subquery.BuildSqlString(true) + ") \"" + name + "\"";
					}
					return "(" + subquery.BuildSqlString(true).Replace("\"" + sqe.QueryModel.MainFromClause.ItemName + "\"", "\"" + name + "\"") + ") \"" + name + "\"";
				}
				return "(" + subquery.BuildSqlString(true) + ") \"" + name + "\"";
			}

			var ce = fromExpression as ConstantExpression;
			if (ce != null)
			{
				var queryable = ce.Value as IQueryable;
				if (queryable != null)
					return GetQueryableExpression(name, queryable);
				var ien = ce.Value as IEnumerable;
				var array = ien != null ? ien.Cast<object>().ToArray() : null;
				var firstElem = array != null ? array.FirstOrDefault(it => it != null) : null;
				var elementType = firstElem != null ? firstElem.GetType()
					: ce.Type.IsArray ? ce.Type.GetElementType()
					: ce.Type.IsGenericType ? ce.Type.GetGenericArguments()[0]
					: null;
				if (Context.CanUseParams && elementType != null)
				{
					var factory = ConverterFactory.GetVarrayParameterFactory(elementType);
					if (factory != null)
					{
						var p = Parameters.Add(factory(ce.Value as IEnumerable));
						return @"(SELECT sq$.OBJECT_VALUE as ""{1}"" FROM TABLE({0}) sq$)".With(p, name);
					}
				}
				if (ce.Type.IsArray || ce.Value is Array)
					return FormatStringArray(ce.Value, name, ce.Type);
				else if (ce.Value is IEnumerable)
					return FormatStringEnumerable(ce.Value, name, ce.Type);
				//TODO: sql injection!?
				return "(SELECT {0} AS \"{1}\" FROM dual) \"{1}\"".With(ce.Value, name);
			}

			var nae = fromExpression as NewArrayExpression;
			if (nae != null)
			{
				if (nae.Expressions.Count == 0)
					//TODO support for zero
					throw new NotImplementedException("Expecting NewArrayExpression arguments. None found.");
				var inner = string.Join(" UNION ALL ", nae.Expressions.Select(it => "SELECT {0} AS \"{1}\"".With(GetSqlExpression(it), name)));
				return "(" + inner + ") \"{0}\" ".With(name);
			}

			if (fromExpression is QuerySourceReferenceExpression && fromExpression.Type.IsGrouping())
			{
				var qse = fromExpression as QuerySourceReferenceExpression;
				//TODO: convert to Oracle version
				return
					"(SELECT (\"{0}\".\"Values\")[i].* FROM generate_series(1, array_upper(\"{0}\".\"Values\", 1)) i) AS \"{1}\"".With(
						qse.ReferencedQuerySource.ItemName,
						name);
			}

			var pe = fromExpression as ParameterExpression;
			if (pe != null)
				return "TABLE({0}\"{1}\") \"{2}\"".With(ContextName, pe.Name, name);

			return FromSqlSource(name, type);
		}

		private string GetQueryableExpression(string name, IQueryable queryable)
		{
			var ce = queryable.Expression as ConstantExpression;
			if (ce != null)
			{
				if (ce.Type.IsGenericType)
				{
					var gtd = ce.Type.GetGenericTypeDefinition();
					if (gtd == typeof(Queryable<>))
						return FromSqlSource(name, ce.Type.GetGenericArguments()[0]);
					if (gtd == typeof(IQueryable<>))
						return GetQuerySourceFromExpression(name, queryable.ElementType, queryable.Expression);
				}
				if (ce.Type.IsArray || ce.Value is Array)
					return FormatStringArray(ce.Value, name, ce.Type);
				else if (ce.Value is IEnumerable)
					return FormatStringEnumerable(ce.Value, name, ce.Type);
				//TODO: sql injection!?
				return "(SELECT {0} FROM dual) \"{1}\"".With(ce.Value, name);
			}

			var mce = queryable.Expression as MethodCallExpression;
			if (mce != null && mce.Method.DeclaringType == typeof(System.Linq.Queryable) && mce.Method.Name == "Cast")
				return GetQuerySourceFromExpression(name, queryable.ElementType, mce.Arguments[0]);

			throw new NotSupportedException("unknown query source expression!");
		}

		private static string FromSqlSource(string name, Type type)
		{
			var source = SqlSourceAttribute.FindSource(type);

			if (!string.IsNullOrEmpty(source))
				return "{0} \"{1}\"".With(source, name);

			throw new NotSupportedException(@"Unknown sql source {0}!
Add {1} attribute or {2} or {3} or {4} interface".With(
				type.FullName,
				typeof(SqlSourceAttribute).FullName,
				typeof(IAggregateRoot).FullName,
				typeof(IIdentifiable).FullName,
				typeof(IEntity).FullName));
		}

		class ColumnValue
		{
			public string Name;
			public Type Type;
			public Func<object, object> GetValue;

			public ColumnValue(System.Reflection.PropertyInfo pi)
			{
				Name = pi.Name;
				Type = pi.PropertyType;
				GetValue = (object v) => pi.GetValue(v, null);
			}

			public ColumnValue(System.Reflection.FieldInfo fi)
			{
				Name = fi.Name;
				Type = fi.FieldType;
				GetValue = (object v) => fi.GetValue(v);
			}

			public string GetBackendValue(object value)
			{
				//TODO fix later
				//return NpgsqlTypes.TypeConverter.Convert(Type, GetValue(value));
				return value.ToString();
			}
		}

		private string FormatStringArray(object value, string name, Type type)
		{
			if (value != null)
			{
				var array = ((Array)value).Cast<object>().ToArray();
				if (array.Length > 0)
					return FormatStringValues(name, type, array);
			}
			if (value != null)
				type = value.GetType();
			var elementType = type.GetElementType();
			return "(SELECT * FROM {1} WHERE 1=0) \"{0}\"".With(name, FromSqlSource("sq", elementType));
		}

		private string FormatStringEnumerable(object value, string name, Type type)
		{
			if (value != null)
			{
				var array = ((IEnumerable)value).Cast<object>().ToArray();
				if (array.Length > 0)
					return FormatStringValues(name, type, array);
			}
			if (value != null)
				type = value.GetType();
			var elementType = type.GetElementType();
			if (type.IsGenericTypeDefinition)
				elementType = type.GetGenericArguments()[0];
			return "(SELECT * FROM {1} WHERE 1=0) \"{0}\"".With(name, FromSqlSource("sq", elementType));
		}

		private string FormatStringValues(string name, Type type, object[] array)
		{
			//TODO find best type
			var firstElem = array.FirstOrDefault(it => it != null);
			var element = firstElem != null ? firstElem.GetType()
				: type.IsArray ? type.GetElementType()
				: type.IsGenericType ? type.GetGenericArguments()[0]
				: null;
			if (element == null)
				throw new ArgumentException("Can't convert collection");

			if (Context.CanUseParams)
			{
				var paramFactory = ConverterFactory.GetVarrayParameterFactory(element);
				if (paramFactory != null)
				{
					var pn = Parameters.Add(paramFactory(array));
					return "(SELECT * FROM TABLE({0})) \"{1}\"".With(pn, name);
				}
			}

			var stringFactory = ConverterFactory.GetVarrayStringFactory(element);
			if (stringFactory == null)
				throw new NotSupportedException("Unable to convert collection " + type.FullName);

			return "(SELECT * FROM TABLE({0})) \"{1}\"".With(stringFactory(array), name);
		}

		protected virtual void ProcessResultOperators(StringBuilder sb)
		{
			ProcessSetOperators(
				sb,
				ResultOperators
				.Where(it => it is ExceptResultOperator || it is IntersectResultOperator
					|| it is UnionResultOperator || it is ConcatResultOperator)
				.ToList());

			ProcessGroupOperators(
				sb,
				ResultOperators.FindAll(it => it is GroupResultOperator).Cast<GroupResultOperator>().ToList());

			ProcessLimitAndOffsetOperators(
				sb,
				ResultOperators.FindAll(it => it is TakeResultOperator).Cast<TakeResultOperator>().ToList(),
				ResultOperators.FindAll(it => it is SkipResultOperator).Cast<SkipResultOperator>().ToList(),
				ResultOperators.FindAll(it => it is FirstResultOperator).Cast<FirstResultOperator>().ToList(),
				ResultOperators.FindAll(it => it is SingleResultOperator).Cast<SingleResultOperator>().ToList());

			ProcessInOperators(
				sb,
				ResultOperators.FindAll(it => it is ContainsResultOperator).Cast<ContainsResultOperator>().ToList());

			ProcessAllOperators(
				sb,
				ResultOperators.FindAll(it => it is AllResultOperator).Cast<AllResultOperator>().ToList());

			if (ResultOperators.Exists(it => it is CountResultOperator || it is LongCountResultOperator))
				ProcessCountOperators(sb);

			if (ResultOperators.Exists(it => it is AnyResultOperator))
				ProcessAnyOperators(sb);
		}

		private void ProcessSetOperators(StringBuilder sb, List<ResultOperatorBase> operators)
		{
			operators.ForEach(it =>
			{
				sb.AppendLine();
				var ero = it as ExceptResultOperator;
				var iro = it as IntersectResultOperator;
				var uro = it as UnionResultOperator;
				var cro = it as ConcatResultOperator;
				if (ero != null)
				{
					sb.AppendLine("EXCEPT");
					sb.AppendLine(SqlGeneratorExpressionTreeVisitor.GetSqlExpression(ero.Source2, this));
				}
				else if (iro != null)
				{
					sb.AppendLine("INTERSECT");
					sb.AppendLine(SqlGeneratorExpressionTreeVisitor.GetSqlExpression(iro.Source2, this));
				}
				else
				{
					SubQueryExpression sqe;
					if (uro != null)
					{
						sb.AppendLine("UNION");
						sqe = uro.Source2 as SubQueryExpression;
					}
					else
					{
						sb.AppendLine("UNION ALL");
						sqe = cro.Source2 as SubQueryExpression;
					}
					//TODO if order is used, Oracle will fail anyway
					foreach (var ro in ResultOperators)
					{
						if (ro is UnionResultOperator == false && ro is ConcatResultOperator == false)
							sqe.QueryModel.ResultOperators.Add(ro);
					}
					sb.AppendLine(SqlGeneratorExpressionTreeVisitor.GetSqlExpression(sqe, this));
				}
			});
		}

		protected virtual void ProcessGroupOperators(StringBuilder sb, List<GroupResultOperator> groupBy)
		{
			if (groupBy.Count > 1)
				throw new NotSupportedException("More than one group operator!?");
			else if (groupBy.Count == 1)
			{
				var group = groupBy[0];
				sb.AppendLine("GROUP BY");
				sb.AppendLine(GetSqlExpression(group.KeySelector));
			}
		}

		protected virtual void ProcessLimitAndOffsetOperators(
			StringBuilder sb,
			List<TakeResultOperator> limit,
			List<SkipResultOperator> offset,
			List<FirstResultOperator> first,
			List<SingleResultOperator> single)
		{
			if (first.Count == 1)
			{
				if (offset.Count == 0)
				{
					sb.Insert(0, "SELECT * FROM (");
					sb.Append(") sq WHERE RowNum = 1");
				}
				if (offset.Count == 1)
				{
					var exp = GetSqlExpression(offset[0].Count);
					sb.Insert(0, "SELECT * FROM ( SELECT /*+ FIRST_ROWS(n) */ sq.*, RowNum rn$ FROM (");
					sb.Append(") sq WHERE RowNum <= 1 + ");
					sb.Append(exp);
					sb.Append(") WHERE rn$ > ");
					sb.Append(exp);
					CurrentSelectIndex++;
				}
			}
			else if (single.Count == 1)
			{
				var minLimit = "2";
				if (limit.Count != 0)
				{
					if (limit.TrueForAll(it => it.Count is ConstantExpression))
					{
						var min = limit.Min(it => (int)(it.Count as ConstantExpression).Value);
						if (min > 1) min = 2;
						minLimit = min.ToString();
					}
					else
					{
						minLimit = "LEAST(2," + string.Join(", ", limit.Select(it => GetSqlExpression(it.Count))) + ")";
					}
				}
				if (offset.Count == 0)
				{
					sb.Insert(0, "SELECT * FROM (");
					sb.Append(") sq WHERE RowNum <= ");
					sb.Append(minLimit);
				}
				if (offset.Count == 1)
				{
					var exp = GetSqlExpression(offset[0].Count);
					sb.Insert(0, "SELECT * FROM ( SELECT /*+ FIRST_ROWS(n) */ sq.*, RowNum rn$ FROM (");
					sb.Append(") sq WHERE RowNum <= ");
					sb.Append(minLimit);
					sb.Append(" + ");
					sb.Append(exp);
					sb.Append(") WHERE rn$ > ");
					sb.Append(exp);
					CurrentSelectIndex++;
				}
			}
			else if (limit.Count > 0 && offset.Count == 0)
			{
				sb.Insert(0, "SELECT * FROM (");
				sb.Append(") sq WHERE RowNum <= ");
				if (limit.Count > 1)
					sb.Append("LEAST(")
						.Append(
							string.Join(
								", ",
								limit.Select(it => GetSqlExpression(it.Count))))
						.AppendLine(")");
				else sb.AppendLine(GetSqlExpression(limit[0].Count));
			}
			else if (limit.Count == 0 && offset.Count > 0)
			{
				sb.Insert(0, "SELECT * FROM ( SELECT /*+ FIRST_ROWS(n) */ sq.*, RowNum rn$ FROM (");
				sb.Append(") sq ) WHERE rn$ > ");
				sb.Append(GetSqlExpression(offset[0].Count));
				for (int i = 1; i < offset.Count; i++)
					sb.Append(" + " + GetSqlExpression(offset[i].Count));
				CurrentSelectIndex++;
			}
			else if (limit.Count == 1 && offset.Count == 1)
			{
				sb.Insert(0, "SELECT * FROM ( SELECT /*+ FIRST_ROWS(n) */ sq.*, RowNum rn$ FROM (");
				sb.Append(") sq WHERE RowNum <= ");
				if (ResultOperators.IndexOf(limit[0]) < ResultOperators.IndexOf(offset[0]))
					sb.AppendLine(GetSqlExpression(limit[0].Count));
				else
					sb.AppendLine("{0} + {1}".With(GetSqlExpression(limit[0].Count), GetSqlExpression(offset[0].Count)));
				sb.Append(") WHERE rn$ > ");
				sb.AppendLine(GetSqlExpression(offset[0].Count));
				CurrentSelectIndex++;
			}
			else if (limit.Count > 1 || offset.Count > 1)
				throw new NotSupportedException("Unsupported combination of limits and offsets in query. More than one offset and more than one limit found.");
		}

		protected virtual void ProcessInOperators(StringBuilder sb, List<ContainsResultOperator> contains)
		{
			if (contains.Count > 1)
				throw new FrameworkException("More than one contains operator!?");
			else if (contains.Count == 1)
			{
				sb.Insert(0, GetSqlExpression(contains[0].Item) + " IN (");
				sb.Append(")");
			}
		}

		protected virtual void ProcessAllOperators(StringBuilder sb, List<AllResultOperator> all)
		{
			if (all.Count > 1)
				throw new FrameworkException("More than one all operator!?");
			else if (all.Count == 1)
			{
				sb.Insert(0, "SELECT NOT EXISTS(SELECT * FROM (");
				var where = GetSqlExpression(all[0].Predicate);
				sb.Append(") sq WHERE (" + where + ") = false)");
			}
		}

		protected virtual void ProcessCountOperators(StringBuilder sb)
		{
			sb.Insert(0, "SELECT COUNT(*) FROM (");
			sb.Append(") sq ");
		}

		protected virtual void ProcessAnyOperators(StringBuilder sb)
		{
			//TODO hack to detect bool result
			if (Context.InSelect)
			{
				sb.Insert(0, "CASE WHEN EXISTS(");
				sb.Append(") THEN 'Y' ELSE 'N' END");
			}
			else if (Context.InWhere)
			{
				sb.Insert(0, "EXISTS(");
				sb.Append(")");
			}
			else
			{
				sb.Insert(0, "SELECT CASE WHEN EXISTS(");
				sb.Append(") THEN 'Y' ELSE 'N' END FROM dual");
			}

			Selects.Clear();
			CurrentSelectIndex = 0;
			AddSelectPart(MainFrom, sb.ToString(), "sq", typeof(bool), (_, dr) => dr.GetString(0) == "Y");
		}

		protected string BuildCountQuery(ResultOperatorBase countOperator)
		{
			Selects.Clear();
			CurrentSelectIndex = 0;

			var sb = new StringBuilder();
			sb.Append("SELECT ");
			if (countOperator is LongCountResultOperator)
				AddSelectPart(MainFrom, "COUNT(*)", "count", typeof(long), (_, dr) => (long)dr.GetDecimal(0));
			else
				AddSelectPart(MainFrom, "COUNT(*)", "count", typeof(int), (_, dr) => (int)dr.GetDecimal(0));
			sb.AppendLine("COUNT(*)");
			sb.Append(GetFromPart());
			sb.Append(GetWherePart());

			return sb.ToString();
		}

		protected string BuildAnyQuery()
		{
			var sb = new StringBuilder();
			if (Context.InSelect)
			{
				Selects.Clear();
				CurrentSelectIndex = 0;
				sb.Append("CASE WHEN ");
				AddSelectPart(MainFrom, "sq", "any", typeof(bool), (_, dr) => dr.GetString(0) == "Y");
			}
			sb.Append("EXISTS(SELECT * ");
			sb.Append(GetFromPart());
			sb.Append(GetWherePart());
			sb.Append(")");
			if (Context.InSelect)
				sb.Append(" THEN 'Y' ELSE 'N' END");
			return sb.ToString();
		}
	}
}