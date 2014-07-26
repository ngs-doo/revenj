using System.Data;
using Oracle.DataAccess.Client;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration
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
			var command = new OracleCommand(Statement) { BindByName = true };
			foreach (var p in Query.Parameters.Parameters)
				command.Parameters.Add(p);
			return command;
		}

		public ResultObjectMapping ProcessRow(IDataReader dr)
		{
			var result = new ResultObjectMapping();
			foreach (var it in Query.Selects)
				if (it.Instancer != null)
					result.Add(it.QuerySource, it.Instancer(result, dr));
			return result;
		}
	}
}