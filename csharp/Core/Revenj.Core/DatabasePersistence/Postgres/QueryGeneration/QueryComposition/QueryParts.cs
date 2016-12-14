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
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;
using Revenj.DomainPatterns;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition
{
	public abstract class QueryParts
	{
		public readonly IServiceProvider Locator;
		public readonly IPostgresConverterFactory ConverterFactory;

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
			QueryContext context,
			IPostgresConverterFactory converterFactory,
			IEnumerable<IQuerySimplification> simplifications,
			IEnumerable<IExpressionMatcher> expressionMatchers,
			IEnumerable<IMemberMatcher> memberMatchers,
			IEnumerable<IProjectionMatcher> projectionMatchers)
		{
			this.Locator = locator;
			this.ConverterFactory = converterFactory;
			this.Simplifications = new List<IQuerySimplification>(simplifications);
			this.ExpressionMatchers = expressionMatchers;
			this.MemberMatchers = memberMatchers;
			this.ProjectionMatchers = projectionMatchers;
			this.Context = context;
		}

		public class SelectSource
		{
			public IQuerySource QuerySource { get; set; }
			public Expression Expression { get; set; }
			public string Sql { get; set; }
			public string Name { get; set; }
			public Type ItemType { get; set; }
			public Func<ResultObjectMapping, BufferedTextReader, IDataReader, object> Instancer { get; set; }
		}

		public bool AddSelectPart(IQuerySource qs, string sql, string name, Type type, Func<ResultObjectMapping, BufferedTextReader, IDataReader, object> instancer)
		{
			if (Selects.Any(kv => kv.Name == name))
				return false;
			Selects.Add(new SelectSource { QuerySource = qs, Sql = sql, Name = name, ItemType = type, Instancer = instancer });
			CurrentSelectIndex++;
			return true;
		}

		public void SetFrom(MainFromClause from)
		{
			MainFrom = from;
			if (from != QuerySourceConverterFactory.GetOriginalSource(from))
				TryToSimplifyMainFrom();
		}

		private void TryToSimplifyMainFrom()
		{
			var from = MainFrom;
			var sqe = from.FromExpression as SubQueryExpression;
			do
			{
				from = sqe.QueryModel.MainFromClause;
				var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(sqe.QueryModel, this);
				if (subquery.Conditions.Count > 0
					|| subquery.Joins.Count > 0
					|| subquery.ResultOperators.Any(it => it is CastResultOperator == false && it is DefaultIfEmptyResultOperator == false)
					|| subquery.AdditionalJoins.Count > 0)
					return;
				sqe = from.FromExpression as SubQueryExpression;
			} while (sqe != null);
			from.ItemName = MainFrom.ItemName;
			MainFrom = from;
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

		public string FormatObject(object value)
		{
			if (value == null)
				return "NULL";
			var type = value.GetType();
			var serialization = ConverterFactory.GetSerializationFactory(type);
			if (serialization != null)
			{
				///TODO use BuildTuple with quote method
				var result = "('" + serialization(value).Replace("'", "''") + "'::";
				string source = null;
				if (value is IIdentifiable || value is IEntity)
					source = SqlSourceAttribute.FindSource(type);
				else if (value is ICloneable)
					source = "\"" + type.Namespace + "\".\"" + type.Name + "\"";
				if (source == null)
					throw new FrameworkException("Can't find source for " + type.FullName);
				return result + source + ").*";
			}
			else if (type.IsEnum)
			{
				return "'" + value + "'::\"" + type.Namespace + "\".\"" + type.Name + "\"";
			}
			if (NpgsqlTypes.TypeConverter.CanConvert(type))
			{
				var val = NpgsqlTypes.TypeConverter.Convert(type, value);
				var name = NpgsqlTypes.TypeConverter.GetTypeName(type);
				return val + "::" + name;
			}
			//TODO probably wrong. better to throw an exception!?
			return value.ToString();
		}

		public string FormatArray(object[] values)
		{
			if (values == null || values.Length == 0)
				throw new FrameworkException("Array must have elements!");
			var element = values[0].GetType();
			var converter = ConverterFactory.GetSerializationFactory(element);
			if (converter != null)
			{
				var source =
					SqlSourceAttribute.FindSource(element)
					?? "\"" + element.Namespace + "\".\"" + element.Name + "\"";
				var arr = Postgres.PostgresTypedArray.ToArray(values, converter);
				return arr + "::" + source + "[]";
			}
			if (NpgsqlTypes.TypeConverter.CanConvert(element))
			{
				var val =
					Postgres.PostgresTypedArray.ToArray(
						values,
						v => NpgsqlTypes.TypeConverter.Convert(element, v));
				var name = NpgsqlTypes.TypeConverter.GetTypeName(element);
				return val + "::" + name + "[]";
			}
			throw new NotSupportedException("Don't know how to convert array!");
		}

		public string GetSqlExpression(Expression expression)
		{
			return GetSqlExpression(expression, Context);
		}

		public string GetSqlExpression(Expression expression, QueryContext context)
		{
			return SqlGeneratorExpressionTreeVisitor.GetSqlExpression(expression, this, context);
		}

		private static bool EndsWithQuerySource(MemberExpression me)
		{
			return me != null
				&& (me.Expression is QuerySourceReferenceExpression
					|| EndsWithQuerySource(me.Expression as MemberExpression));
		}

		public string GetFromPart()
		{
			if (MainFrom == null)
				throw new InvalidOperationException("A query must have a from part");

			var sb = new StringBuilder();

			if (AdditionalJoins.Any(it => EndsWithQuerySource(it.FromExpression as MemberExpression)))
				sb.AppendFormat("FROM ({0}) sq ", GetInnerFromPart());
			else
				sb.AppendFormat("FROM {0}", GetQuerySourceFromExpression(MainFrom.ItemName, MainFrom.ItemType, MainFrom.FromExpression));

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
				if (EndsWithQuerySource(me))
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
			foreach (var j in Joins)
			{
				sb.AppendFormat("{0}	INNER JOIN {1} ON ({2}) = ({3}){0}",
					Environment.NewLine,
					GetQuerySourceFromExpression(j.ItemName, j.ItemType, j.InnerSequence),
					GetSqlExpression(j.InnerKeySelector),
					GetSqlExpression(j.OuterKeySelector));
			}
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

		private string GetInnerFromPart()
		{
			var sb = new StringBuilder("SELECT ");
			sb.AppendFormat("\"{0}\"", MainFrom.ItemName);

			foreach (var aj in AdditionalJoins)
			{
				var me = aj.FromExpression as MemberExpression;
				if (me != null)
				{
					var src = BuildMemberPath(me, false);
					if (src != null)
						sb.AppendFormat(", unnest({0}) AS \"{1}\"", src, aj.ItemName);
				}
			}

			sb.AppendFormat(" FROM {0}", GetQuerySourceFromExpression(MainFrom.ItemName, MainFrom.ItemType, MainFrom.FromExpression));

			return sb.ToString();
		}

		public string GetWherePart()
		{
			if (Conditions.Count == 0)
				return string.Empty;

			var whereConditions = new List<string>(Conditions.Count);

			foreach (var c in Conditions)
				whereConditions.Add(GetSqlExpression(c));

			return @"WHERE
	" + string.Join(Environment.NewLine + "	AND ", whereConditions) + @"
";
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

		private string BuildMemberPath(MemberExpression me, bool nest)
		{
			var list = new List<string>();
			while (me != null)
			{
				list.Add(ConverterFactory.GetName(me.Member));
				var qse = me.Expression as QuerySourceReferenceExpression;
				var par = me.Expression as ParameterExpression;
				if (qse != null || par != null)
				{
					var sb = new StringBuilder();
					var name = qse != null ? qse.ReferencedQuerySource.ItemName : par.Name;
					if (par != null && !string.IsNullOrEmpty(Context.Name))
						sb.Append(Context.Name);
					sb.Append('"').Append(name).Append('"');
					list.Reverse();
					foreach (var m in list)
					{
						if (nest)
						{
							sb.Insert(0, '(');
							sb.Append(')');
						}
						sb.Append(".\"").Append(m).Append("\"");
					}
					return sb.ToString();
				}
				me = me.Expression as MemberExpression;
			}
			return null;
		}

		//TODO vjerojatno ponekad ne treba ignorirati expression
		public string GetQuerySourceFromExpression(string name, Type type, Expression fromExpression)
		{
			var me = fromExpression as MemberExpression;
			if (me != null)
			{
				var src = BuildMemberPath(me, true);
				if (src != null)
					return @"(SELECT sq AS ""{1}"" FROM unnest({0}) sq) AS ""{1}""".With(src, name);
			}

			var sqe = fromExpression as SubQueryExpression;
			if (sqe != null)
			{
				if (sqe.QueryModel.CanUseMain())
					return GetQuerySourceFromExpression(name, type, sqe.QueryModel.MainFromClause.FromExpression);
				//TODO hack za replaceanje generiranog id-a
				var subquery = SubqueryGeneratorQueryModelVisitor.ParseSubquery(sqe.QueryModel, this);
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
				if (ce.Type.IsArray || ce.Value is Array)
					return FormatStringArray(ce.Value, name, ce.Type);
				else if (ce.Value is IEnumerable)
					return FormatStringEnumerable(ce.Value, name, ce.Type);
				return "(SELECT {0} AS \"{1}\") AS \"{1}\"".With(ce.Value, name);
			}

			var nae = fromExpression as NewArrayExpression;
			if (nae != null)
			{
				if (nae.Expressions.Count == 0)
					//TODO support for zero
					throw new NotSupportedException("Expecting NewArray expressions. None found");
				var inner = string.Join(" UNION ALL ", nae.Expressions.Select(it => "SELECT {0} AS \"{1}\"".With(GetSqlExpression(it), name)));
				return "(" + inner + ") AS \"{0}\" ".With(name);
			}

			if (fromExpression is QuerySourceReferenceExpression && fromExpression.Type.IsGrouping())
			{
				var qse = fromExpression as QuerySourceReferenceExpression;
				return
					"(SELECT (\"{0}\".\"Values\")[i].* FROM generate_series(1, array_upper(\"{0}\".\"Values\", 1)) i) AS \"{1}\"".With(
						qse.ReferencedQuerySource.ItemName,
						name);
			}

			var pe = fromExpression as ParameterExpression;
			if (pe != null)
				return "UNNEST({0}\"{1}\") AS \"{2}\"".With(Context.Name, pe.Name, name);

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
				//TODO this doesn't work most of the cases
				return "(SELECT {0} AS \"{1}\") AS \"{1}\"".With(ce.Value, name);
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
				return "{0} AS \"{1}\"".With(source, name);

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
				return NpgsqlTypes.TypeConverter.Convert(Type, GetValue(value));
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
			if (NpgsqlTypes.TypeConverter.CanConvert(elementType))
				return
					"(SELECT * FROM unnest(array[]::{1}[]) AS \"{0}\") AS \"{0}\"".With(
						name,
						NpgsqlTypes.TypeConverter.GetTypeName(elementType));
			return "(SELECT * FROM {1} LIMIT 0) AS \"{0}\"".With(name, FromSqlSource("sq", elementType));
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
			var elementType = type.IsArray
				? type.GetElementType()
				: type.IsGenericType
					? type.GetGenericArguments()[0]
					: null;
			if (elementType == null)
				throw new NotSupportedException("Unknown source type: " + type.FullName);
			if (NpgsqlTypes.TypeConverter.CanConvert(elementType))
				return
					"(SELECT * FROM unnest(array[]::{1}[]) AS \"{0}\") AS \"{0}\"".With(
						name,
						NpgsqlTypes.TypeConverter.GetTypeName(elementType));
			return "(SELECT * FROM {1} LIMIT 0) AS \"{0}\"".With(name, FromSqlSource("sq", elementType));
		}

		private string FormatStringValues(string name, Type type, object[] array)
		{
			Type element = null;
			if (type.IsGenericType || type.IsArray)
			{
				element = type.IsArray ? type.GetElementType() : type.GetGenericArguments()[0];
				var converter = ConverterFactory.GetSerializationFactory(element);
				if (converter != null)
				{
					var source = SqlSourceAttribute.FindSource(element)
						?? "\"" + element.Namespace + "\".\"" + element.Name + "\"";
					var arr = Postgres.PostgresTypedArray.ToArray(array, converter);
					return @"(SELECT ""{2}"" FROM unnest({0}::{1}[]) ""{2}"") ""{2}""".With(
							arr,
							source,
							name);
				}
				else if (!NpgsqlTypes.TypeConverter.CanConvert(element))
				{
					var fields = element.GetFields().Select(it => new ColumnValue(it));
					var properties = element.GetProperties().Select(it => new ColumnValue(it));
					var columns = fields.Union(properties).ToList();

					return "(SELECT "
						+ string.Join(", ", columns.Select((it, ind) => "column{0} AS \"{1}\"".With(ind + 1, it.Name)))
						+ " FROM (VALUES"
						+ string.Join(
							", ",
							array.Select(it => "(" + string.Join(", ", columns.Select(c => c.GetBackendValue(it))) + ")"))
						+ ") _sq1 ) \"" + name + "\"";
				}
			}
			var formated = (from i in array select FormatObject(i)).ToList();
			var typeAlias = element != null && NpgsqlTypes.TypeConverter.CanConvert(element)
				? "::" + NpgsqlTypes.TypeConverter.GetTypeName(element)
				: string.Empty;
			return
				"(SELECT {0}{1} AS \"{2}\"{3}) AS \"{2}\"".With(
					formated[0],
					typeAlias,
					name,
					string.Concat(formated.Skip(1).Select(it => " UNION ALL SELECT " + it)));
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
			foreach (var it in operators)
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
					if (OrderBy.Count > 0)
					{
						sb.Insert(0, '(');
						sb.AppendLine(")");
					}
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
					foreach (var ro in ResultOperators)
					{
						if (ro is UnionResultOperator == false && ro is ConcatResultOperator == false)
							sqe.QueryModel.ResultOperators.Add(ro);
					}
					sb.AppendLine(SqlGeneratorExpressionTreeVisitor.GetSqlExpression(sqe, this));
				}
			}
		}

		protected virtual void ProcessGroupOperators(StringBuilder sb, List<GroupResultOperator> groupBy)
		{
			if (groupBy.Count > 1)
				throw new FrameworkException("More than one group operator!?");
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
				sb.AppendLine("LIMIT 1");
				if (offset.Count == 1)
					sb.AppendLine("OFFSET " + GetSqlExpression(offset[0].Count));
			}
			else if (single.Count == 1)
			{
				if (limit.Count == 0)
					sb.Append("LIMIT 2");
				else
				{
					if (limit.TrueForAll(it => it.Count is ConstantExpression))
					{
						var min = limit.Min(it => (int)(it.Count as ConstantExpression).Value);
						if (min > 1) min = 2;
						sb.Append("LIMIT ").Append(min);
					}
					else
					{
						sb.Append("LIMIT LEAST(2,");
						sb.Append(string.Join(", ", limit.Select(it => GetSqlExpression(it.Count))));
						sb.AppendLine(")");
					}
				}
				if (offset.Count == 1)
					sb.AppendLine("OFFSET " + GetSqlExpression(offset[0].Count));
			}
			else if (limit.Count > 0 && offset.Count == 0)
			{
				sb.Append("LIMIT ");
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
				sb.AppendLine("OFFSET " + GetSqlExpression(offset[0].Count));
				for (int i = 1; i < offset.Count; i++)
					sb.Append(" + " + GetSqlExpression(offset[i].Count));
			}
			else if (limit.Count == 1 && offset.Count == 1)
			{
				if (ResultOperators.IndexOf(limit[0]) < ResultOperators.IndexOf(offset[0]))
					sb.AppendLine("LIMIT ({0} - {1})".With(GetSqlExpression(limit[0].Count), GetSqlExpression(offset[0].Count)));
				else
					sb.AppendLine("LIMIT " + GetSqlExpression(limit[0].Count));
				sb.AppendLine("OFFSET " + GetSqlExpression(offset[0].Count));
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
			sb.Insert(0, "SELECT EXISTS(");
			sb.Append(") sq ");

			Selects.Clear();
			CurrentSelectIndex = 0;
			AddSelectPart(MainFrom, sb.ToString(), "sq", typeof(bool), (_, __, dr) => dr.GetBoolean(0));
		}

		protected string BuildCountQuery(ResultOperatorBase countOperator)
		{
			Selects.Clear();
			CurrentSelectIndex = 0;

			var sb = new StringBuilder();
			sb.Append("SELECT ");
			if (countOperator is LongCountResultOperator)
			{
				AddSelectPart(MainFrom, "COUNT(*)", "count", typeof(long), (_, __, dr) => dr.GetInt64(0));
				sb.AppendLine("COUNT(*)");
			}
			else
			{
				AddSelectPart(MainFrom, "COUNT(*)::int", "count", typeof(int), (_, __, dr) => dr.GetInt32(0));
				sb.AppendLine("COUNT(*)::int");
			}
			sb.Append(GetFromPart());
			sb.Append(GetWherePart());

			return sb.ToString();
		}
	}
}