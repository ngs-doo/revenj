using System;
using System.Linq;
using System.Linq.Expressions;
using System.Text;
using Remotion.Linq.Clauses.ResultOperators;
using Revenj.Common;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition
{
	public class SubqueryParts : QueryParts
	{
		public bool CanQueryInMemory { get; private set; }
		public bool ShouldQueryInMemory { get; set; }
		internal readonly QueryParts ParentQuery;
		public readonly Expression Selector;

		public SubqueryParts(QueryParts parentQuery, Expression selector, string contextName)
			: this(parentQuery, false, selector, contextName) { }

		public SubqueryParts(QueryParts parentQuery, bool canQueryInMemory, Expression selector, string contextName)
			: base(
				parentQuery.Locator,
				contextName,
				parentQuery.ConverterFactory,
				parentQuery.Simplifications,
				parentQuery.ExpressionMatchers,
				parentQuery.MemberMatchers,
				parentQuery.ProjectionMatchers)
		{
			this.CanQueryInMemory = canQueryInMemory;
			this.ParentQuery = parentQuery;
			this.Selector = selector;
		}

		public string BuildSqlString(bool canUseOperators)
		{
			if (MainFrom == null)
				throw new ApplicationException("From !?");

			foreach (var qs in Simplifications)
				if (qs.CanSimplify(this))
					return qs.Simplify(this);

			var sb = new StringBuilder();
			sb.Append("SELECT ");

			var groupOperator = ResultOperators.FirstOrDefault(it => it is GroupResultOperator) as GroupResultOperator;
			if (groupOperator != null)
			{
				if (Selects.Count != 1)
					throw new FrameworkException("Select count can only be two when grouping!");
				sb.AppendFormat(
					"{0} AS \"Key\", ARRAY_AGG(\"{1}\") AS \"Values\"",
						GetSqlExpression(groupOperator.KeySelector),
						Selects[0].QuerySource.ItemName);
			}
			else
			{
				if (canUseOperators)
				{
					var countOperator = ResultOperators.FirstOrDefault(it => it is CountResultOperator || it is LongCountResultOperator);
					if (countOperator != null)
						return BuildCountQuery(countOperator);
					var sumOperator = ResultOperators.FirstOrDefault(it => it is SumResultOperator);
					if (sumOperator != null)
					{
						if (Selects.Count > 1)
						{
							//TOOD hack to fix problem with select
							Selects.Clear();
							Selects.Add(new SelectSource { Sql = GetSqlExpression(Selector), ItemType = Selector.Type });
						}
						sb.AppendFormat("COALESCE(SUM({0}), 0)", Selects[0].Sql);
						//TODO use actual type
						if (Selects[0].ItemType == typeof(int) || Selects[0].ItemType == typeof(int?))
							sb.Append("::int");
						if (Selects[0].Name != null)
							sb.AppendFormat(" AS \"{0}\"", Selects[0].Name);
						sb.AppendLine();
						sb.Append(GetFromPart());
						sb.Append(GetWherePart());
						return sb.ToString();
					}
					var containsOperator = ResultOperators.FirstOrDefault(it => it is ContainsResultOperator);
					if (containsOperator != null)
					{
						if (Selects.Count > 1)
						{
							//TOOD hack to fix problem with select
							Selects.Clear();
							Selects.Add(new SelectSource { Sql = GetSqlExpression(Selector) });
						}
					}
				}
				sb.Append(string.Join(@", ", Selects.Select(it => it.Sql)));
			}

			sb.AppendLine();
			sb.Append(GetFromPart());
			sb.Append(GetWherePart());
			sb.Append(GetOrderPart());

			ProcessResultOperators(sb);

			return sb.ToString();
		}
	}
}