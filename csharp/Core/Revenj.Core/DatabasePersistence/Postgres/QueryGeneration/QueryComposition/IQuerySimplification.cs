namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition
{
	public interface IQuerySimplification
	{
		bool CanSimplify(QueryParts query);
		string Simplify(QueryParts query);
	}
}
