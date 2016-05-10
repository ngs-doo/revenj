using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Remotion.Linq.Clauses;
using Remotion.Linq.Clauses.ResultOperators;
using Remotion.Linq.Parsing.Structure;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.Visitors;
using Revenj.DomainPatterns;
using Revenj.Extensibility;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition
{
	public class MainQueryParts : QueryParts
	{
		public MainQueryParts(
			IServiceProvider locator,
			IPostgresConverterFactory factory,
			IEnumerable<IQuerySimplification> simplifications,
			IEnumerable<IExpressionMatcher> expressionMatchers,
			IEnumerable<IMemberMatcher> memberMatchers,
			IEnumerable<IProjectionMatcher> projectionMatchers)
			: base(
				locator,
				QueryContext.Standard,
				factory,
				simplifications,
				expressionMatchers,
				memberMatchers,
				projectionMatchers)
		{
		}

		public string BuildSqlString()
		{
			if (Selects.Count == 0)
			{
				var countOp = ResultOperators.FirstOrDefault(it => it is CountResultOperator || it is LongCountResultOperator);
				if (countOp != null)
					return BuildCountQuery(countOp);
				throw new InvalidOperationException("A query must have a select part");
			}

			foreach (var qs in Simplifications)
				if (qs.CanSimplify(this))
					return qs.Simplify(this);

			var countOperator = ResultOperators.FirstOrDefault(it => it is CountResultOperator || it is LongCountResultOperator);
			if (countOperator != null)
				return BuildCountQuery(countOperator);

			var sb = new StringBuilder();
			sb.Append("SELECT ");
			sb.Append(string.Join(@", 
	", Selects.Select(it => it.Sql))).AppendLine();
			sb.Append(GetFromPart());
			sb.Append(GetWherePart());
			sb.Append(GetOrderPart());

			ProcessResultOperators(sb);

			return sb.ToString();
		}

		public static void AddFilter<TSource>(
			IServiceProvider Locator,
			IDatabaseQuery query,
			ISpecification<TSource> filter,
			StringBuilder sb)
		{
			var cf = Locator.Resolve<IPostgresConverterFactory>();
			var ep = Locator.Resolve<IExtensibilityProvider>();
			var qp =
				new MainQueryParts(
					Locator,
					cf,
					ep.ResolvePlugins<IQuerySimplification>(),
					ep.ResolvePlugins<IExpressionMatcher>(),
					ep.ResolvePlugins<IMemberMatcher>(),
					new IProjectionMatcher[0]);
			var linq = new Queryable<TSource>(new QueryExecutor(query, Locator, cf, ep)).Filter(filter);
			var parser = QueryParser.CreateDefault();
			var model = parser.GetParsedQuery(linq.Expression);
			if (model.BodyClauses.Count > 0)
			{
				sb.AppendLine("WHERE");
				for (int i = 0; i < model.BodyClauses.Count; i++)
				{
					var wc = model.BodyClauses[i] as WhereClause;
					if (wc == null)
						continue;
					sb.Append("	");
					if (i > 0)
						sb.Append("AND ");
					sb.Append(qp.GetSqlExpression(wc.Predicate));
				}
			}
		}
	}
}