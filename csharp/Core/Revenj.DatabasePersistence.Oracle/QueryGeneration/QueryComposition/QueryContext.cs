namespace Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition
{
	public struct QueryContext
	{
		public readonly bool InSelect;
		public readonly bool InWhere;
		public readonly bool CanUseParams;

		public QueryContext(bool inSelect, bool inWhere, bool canUseParams)
		{
			this.InSelect = inSelect;
			this.InWhere = inWhere;
			this.CanUseParams = canUseParams;
		}

		public QueryContext Select()
		{
			return new QueryContext(true, false, CanUseParams);
		}

		public QueryContext Where()
		{
			return new QueryContext(false, true, CanUseParams);
		}
	}
}
