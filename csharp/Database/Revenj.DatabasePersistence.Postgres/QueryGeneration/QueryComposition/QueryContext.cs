using System;

namespace Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition
{
	public struct QueryContext
	{
		public readonly string Name;
		public readonly bool CanUseParams;
		public readonly Lazy<DateInfo> Time;

		public static readonly QueryContext Standard = new QueryContext(string.Empty, true);

		public QueryContext(string name, bool canUseParams)
		{
			this.Name = name;
			this.CanUseParams = canUseParams;
			Time = new Lazy<DateInfo>(() => new DateInfo(DateTime.Now));
		}

		public struct DateInfo
		{
			public readonly DateTime Now;
			public readonly DateTime UtcNow;
			public readonly DateTime Today;

			public DateInfo(DateTime now)
			{
				Now = now;
				UtcNow = now.ToUniversalTime();
				Today = now.Date;
			}
		}
	}
}
