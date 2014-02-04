using System.Data;
using NGS.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Npgsql;

namespace NGS.DatabasePersistence.Postgres.QueryGeneration
{
	public class SqlCommandData
	{
		private readonly MainQueryParts Query;

		public SqlCommandData(MainQueryParts query)
		{
			this.Query = query;
			Statement = query.BuildSqlString();
			var mainIndex = query.Selects.FindIndex(it => it.QuerySource == query.MainFrom);
			if (mainIndex > 0)
			{
				var main = query.Selects[mainIndex];
				query.Selects.RemoveAt(mainIndex);
				query.Selects.Insert(0, main);
			}
		}

		public string Statement { get; private set; }

		public IDbCommand CreateQuery()
		{
			return new NpgsqlCommand(Statement);
		}

		public ResultObjectMapping ProcessRow(IDataReader dr)
		{
			var result = new ResultObjectMapping();
			foreach (var sel in Query.Selects)
				result.Add(sel.QuerySource, sel.Instancer(result, dr));
			return result;
		}
	}
}