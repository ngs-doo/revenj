using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using Remotion.Linq.Clauses;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration
{
	internal class SelectSubqueryData
	{
		private readonly List<KeyValuePair<IQuerySource, Func<object, BufferedTextReader, object>>> Selects = new List<KeyValuePair<IQuerySource, Func<object, BufferedTextReader, object>>>();

		private SelectSubqueryData(IEnumerable<KeyValuePair<IQuerySource, Func<object, BufferedTextReader, object>>> selects)
		{
			Selects.AddRange(selects);
		}

		public static SelectSubqueryData Create(QueryParts parts, SubqueryParts subquery)
		{
			Contract.Requires(parts != null);
			Contract.Requires(subquery != null);

			var selects = new List<KeyValuePair<IQuerySource, Func<object, BufferedTextReader, object>>>();
			foreach (var s in subquery.Selects)
			{
				var factory = QuerySourceConverterFactory.Create(s.QuerySource, parts);
				selects.Add(new KeyValuePair<IQuerySource, Func<object, BufferedTextReader, object>>(factory.QuerySource, factory.Instancer));
			}

			return new SelectSubqueryData(selects);
		}

		public ResultObjectMapping ProcessRow(ResultObjectMapping parent, BufferedTextReader reader, object row)
		{
			var result = new ResultObjectMapping();
			Selects.ForEach(it => result.Add(it.Key, it.Value(row, reader)));
			result.Add(parent);
			return result;
		}

		public ResultObjectMapping ProcessRow(ResultObjectMapping parent, BufferedTextReader reader, string[] items)
		{
			var result = new ResultObjectMapping();
			for (int i = 0; i < Selects.Count; i++)
			{
				var sel = Selects[i];
				result.Add(sel.Key, sel.Value(items[i], reader));
			}
			result.Add(parent);
			return result;
		}
	}
}