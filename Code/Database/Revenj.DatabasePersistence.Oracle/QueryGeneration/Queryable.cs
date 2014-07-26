using System.Linq;
using System.Linq.Expressions;
using Remotion.Linq;
using Remotion.Linq.Parsing.Structure;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration
{
	public class Queryable<TEntity> : QueryableBase<TEntity>
	{
		public Queryable(IQueryExecutor executor)
			: base(QueryParser.CreateDefault(), executor) { }

		public Queryable(IQueryProvider queryProvider, Expression expression)
			: base(queryProvider, expression) { }
	}
}