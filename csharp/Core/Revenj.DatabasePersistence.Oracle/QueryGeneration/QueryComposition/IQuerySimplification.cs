namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition
{
	public interface IQuerySimplification
	{
		bool CanSimplify(QueryParts query);
		string Simplify(QueryParts query);
	}
}
