using System.Collections.Generic;
using Remotion.Linq.Clauses;
using Remotion.Linq.Clauses.Expressions;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration
{
	public class ResultObjectMapping
	{
		private readonly Dictionary<IQuerySource, object> ResultObjectsBySource = new Dictionary<IQuerySource, object>();

		public void Add(IQuerySource querySource, object resultObject)
		{
			ResultObjectsBySource[querySource] = resultObject;
		}

		public void Add(ResultObjectMapping parent)
		{
			if (parent != null)
				foreach (var kv in parent.ResultObjectsBySource)
					ResultObjectsBySource[kv.Key] = kv.Value;
		}

		public T GetObject<T>(IQuerySource source)
		{
			return (T)ResultObjectsBySource[source];
		}

		public T EvaluateSubquery<T>(SubQueryExpression source)
		{
			return (T)ResultObjectsBySource[source.QueryModel.MainFromClause];
		}
	}
}
