using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using Remotion.Linq.Clauses;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration
{
	public class SelectSubqueryData
	{
		private readonly List<KeyValuePair<IQuerySource, Func<object, object>>> Selects = new List<KeyValuePair<IQuerySource, Func<object, object>>>();

		private SelectSubqueryData(IEnumerable<KeyValuePair<IQuerySource, Func<object, object>>> selects)
		{
			Selects.AddRange(selects);
		}

		public static SelectSubqueryData Create(QueryParts parts, SubqueryParts subquery)
		{
			Contract.Requires(parts != null);
			Contract.Requires(subquery != null);

			var selects = new List<KeyValuePair<IQuerySource, Func<object, object>>>();
			foreach (var s in subquery.Selects)
			{
				var factory = QuerySourceConverterFactory.Create(s.QuerySource, parts);
				selects.Add(new KeyValuePair<IQuerySource, Func<object, object>>(factory.QuerySource, factory.Instancer));
			}

			return new SelectSubqueryData(selects);
		}

		public ResultObjectMapping ProcessRow(ResultObjectMapping parent, object row)
		{
			var result = new ResultObjectMapping();
			for (int i = 0; i < Selects.Count; i++)
			{
				var sel = Selects[i];
				result.Add(sel.Key, sel.Value(row));
			}
			result.Add(parent);
			return result;
		}

		public ResultObjectMapping ProcessRow(ResultObjectMapping parent, string[] items)
		{
			var result = new ResultObjectMapping();
			for (int i = 0; i < Selects.Count; i++)
			{
				var sel = Selects[i];
				result.Add(sel.Key, sel.Value(items[i]));
			}
			result.Add(parent);
			return result;
		}
	}
}