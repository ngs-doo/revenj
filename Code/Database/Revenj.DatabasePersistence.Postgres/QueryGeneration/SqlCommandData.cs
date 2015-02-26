using System.Data;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration
{
	public class SqlCommandData
	{
		private readonly MainQueryParts Query;
		private readonly bool Reordered;

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
				Reordered = true;
			}
		}

		public string Statement { get; private set; }

		public IDbCommand CreateQuery(Revenj.Utility.ChunkedMemoryStream cms)
		{
			var writer = cms.GetWriter();
			writer.Write(Statement);
			writer.Flush();
			cms.Position = 0;
			return PostgresDatabaseQuery.NewCommand(cms, Statement, !Reordered);
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