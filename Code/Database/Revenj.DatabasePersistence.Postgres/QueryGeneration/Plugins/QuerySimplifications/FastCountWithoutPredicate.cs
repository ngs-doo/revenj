using System;
using System.ComponentModel.Composition;
using System.Linq;
using Remotion.Linq.Clauses.ResultOperators;
using Revenj.DatabasePersistence.Postgres.QueryGeneration;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DomainPatterns;

namespace Revenj.DatabasePersistence.Postgres.Plugins.QuerySimplifications
{
	[Export(typeof(IQuerySimplification))]
	public class FastCountWithoutPredicate : IQuerySimplification
	{
		private static readonly Type QueryableType = typeof(Queryable<>);

		public bool CanSimplify(QueryParts query)
		{
			var type = query.MainFrom != null ? query.MainFrom.FromExpression.Type : null;
			return type != null
				&& query.ResultOperators.Count == 1
				&& query.Joins.Count == 0 && query.AdditionalJoins.Count == 0 && query.Conditions.Count == 0
				&& (query.ResultOperators[0] is CountResultOperator
					|| query.ResultOperators[0] is LongCountResultOperator)
				&& type.IsGenericType
				&& type.GetGenericTypeDefinition() == QueryableType
				&& typeof(IEntity).IsAssignableFrom(type.GetGenericArguments()[0]);
		}

		public string Simplify(QueryParts query)
		{
			var co = query.ResultOperators[0];
			var prefix = "SELECT COUNT(*)";
			query.Selects.Clear();
			query.CurrentSelectIndex = 0;

			if (co is LongCountResultOperator)
			{
				query.AddSelectPart(query.MainFrom, "COUNT(*)", "count", typeof(long), (_, __, dr) => dr.GetInt64(0));
			}
			else
			{
				query.AddSelectPart(query.MainFrom, "COUNT(*)::int", "count", typeof(int), (_, __, dr) => dr.GetInt32(0));
				prefix += "::int";
			}
			prefix += " FROM ";
			var type = query.MainFrom.FromExpression.Type.GetGenericArguments()[0];
			var isRoot = typeof(IAggregateRoot).IsAssignableFrom(type);
			if (isRoot)
				return prefix + @"""{0}"".""{1}""".With(type.Namespace, type.Name);
			var isEvent = typeof(IDomainEvent).IsAssignableFrom(type);
			if (isEvent)
				return type.IsNested
					? prefix + @"""{0}"".""{1}.{2}""".With(type.Namespace, type.DeclaringType.Name, type.Name)
					: prefix + @"""{0}"".""{1}""".With(type.Namespace, type.Name);
			var ent =
				(from i in type.GetInterfaces()
				 where i.IsGenericType
				 && i.GetGenericTypeDefinition() == typeof(ISnowflake<>)
				 select i.GetGenericArguments()[0])
				 .FirstOrDefault() ?? type;
			return prefix + @"""{0}"".""{1}""".With(ent.Namespace, ent.Name);
		}
	}
}
