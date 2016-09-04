using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using Remotion.Linq;
using Remotion.Linq.Clauses.ResultOperators;
using Revenj.DatabasePersistence.Postgres.QueryGeneration;

namespace Revenj.DatabasePersistence.Postgres
{
	public static class TypeUtility
	{
		public static bool CanUseMain(this QueryModel queryModel)
		{
			return queryModel.IsIdentityQuery()
				&& (queryModel.ResultOperators.Count == 0
					|| queryModel.ResultOperators.Count == 1 && queryModel.ResultOperators[0] is DefaultIfEmptyResultOperator);
		}

		public static bool IsNullable(this Type type)
		{
			return type.IsGenericType && type.GetGenericTypeDefinition() == typeof(Nullable<>);
		}

		public static bool IsGrouping(this Type type)
		{
			return type.IsGenericType && type.GetGenericTypeDefinition() == typeof(IGrouping<,>);
		}

		public static bool IsQueryable(this Type type)
		{
			return type.IsGenericType && type.GetGenericTypeDefinition() == typeof(Queryable<>);
		}

		class Grouping<TK, TE> : IGrouping<TK, TE>
		{
			public TK Key { get; private set; }
			private readonly List<TE> Values;

			public Grouping(TK tk, IEnumerable te)
			{
				this.Key = tk;
				this.Values = te.Cast<TE>().ToList();
			}

			IEnumerator IEnumerable.GetEnumerator() { return GetEnumerator(); }
			public IEnumerator<TE> GetEnumerator()
			{
				foreach (var item in Values)
					yield return item;
			}
		}

		public static Type CreateGrouping(this Type type)
		{
			return typeof(Grouping<,>).MakeGenericType(type.GetGenericArguments());
		}
	}
}
