using System.Data;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration
{
	internal class SqlCommandData
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

		public IDbCommand CreateQuery(Revenj.Utility.ChunkedMemoryStream cms)
		{
			var writer = cms.GetWriter();
			writer.Write(Statement);
			writer.Flush();
			cms.Position = 0;
			return PostgresCommandFactory.NewCommand(cms, Statement, Query.Selects.Count == 1);
		}

		public ResultObjectMapping ProcessRow(IDataReader dr, BufferedTextReader reader)
		{
			var result = new ResultObjectMapping();
			foreach (var sel in Query.Selects)
				result.Add(sel.QuerySource, sel.Instancer(result, reader, dr));
			return result;
		}
	}
}