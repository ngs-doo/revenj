using System.Linq;
using System.Linq.Expressions;
using Remotion.Linq;
using Remotion.Linq.Parsing.Structure;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration
{
	public class Queryable<TEntity> : QueryableBase<TEntity>
	{
		private static QueryParser DefaultParser = QueryParser.CreateDefault();

		public Queryable(IQueryExecutor executor)
			: base(DefaultParser, executor) { }

		public Queryable(IQueryProvider queryProvider, Expression expression)
			: base(queryProvider, expression) { }
	}
}