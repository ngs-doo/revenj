using System.ComponentModel.Composition;
using Remotion.Linq.Clauses.ResultOperators;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Postgres.Plugins.QuerySimplifications
{
	[Export(typeof(IQuerySimplification))]
	public class InverseExistsSubquery : IQuerySimplification
	{
		public bool CanSimplify(QueryParts query)
		{
			var parts = query as SubqueryParts;
			return parts != null
				&& !parts.ShouldQueryInMemory
				&& parts.ResultOperators.Count == 1
				&& parts.Conditions.Count == 0 && parts.AdditionalJoins.Count == 0 && parts.Joins.Count == 0
				&& parts.ResultOperators[0] is AllResultOperator;
		}

		public string Simplify(QueryParts query)
		{
			var parts = query as SubqueryParts;
			var aro = parts.ResultOperators[0] as AllResultOperator;
			return " NOT EXISTS (SELECT * {0} WHERE NOT ({1}))".With(parts.GetFromPart(), parts.GetSqlExpression(aro.Predicate));
		}
	}
}
