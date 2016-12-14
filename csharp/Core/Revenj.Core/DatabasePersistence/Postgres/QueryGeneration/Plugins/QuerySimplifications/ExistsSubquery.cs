using System.ComponentModel.Composition;
using System.Linq.Expressions;
using Remotion.Linq.Clauses.Expressions;
using Remotion.Linq.Clauses.ResultOperators;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Postgres.Plugins.QuerySimplifications
{
	[Export(typeof(IQuerySimplification))]
	public class ExistsSubquery : IQuerySimplification
	{
		public bool CanSimplify(QueryParts query)
		{
			var parts = query as SubqueryParts;
			return parts != null
				&& !parts.ShouldQueryInMemory
				&& parts.ResultOperators.Count == 1
				&& parts.ResultOperators[0] is AnyResultOperator;
		}

		public string Simplify(QueryParts query)
		{
			var parts = query as SubqueryParts;

			var me = parts.MainFrom.FromExpression as MemberExpression;
			if (me != null && parts.Conditions.Count == 0)
			{
				var qse = me.Expression as QuerySourceReferenceExpression;
				if (qse != null)
					return
						"EXISTS (SELECT * FROM UNNEST((\"{0}\").\"{1}\"))".With(
							qse.ReferencedQuerySource.ItemName,
							query.ConverterFactory.GetName(me.Member));
			}

			return "EXISTS (SELECT * {0} {1})".With(parts.GetFromPart(), parts.GetWherePart());
		}
	}
}
