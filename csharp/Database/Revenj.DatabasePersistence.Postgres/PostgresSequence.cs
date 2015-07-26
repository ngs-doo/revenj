using System;

namespace Revenj.DatabasePersistence.Postgres
{
	public static class PostgresSequence
	{
		public static void Fill<TValue, TProperty>(
			this IDatabaseQuery query,
			TValue[] data,
			string sequenceName,
			Action<TValue, TProperty> setProperty)
		{
			if (data.Length != 0)
			{
				int cnt = 0;
				query.Execute(
					@"/*NO LOAD BALANCE*/SELECT {0} FROM generate_series(1, {1})".With(sequenceName, data.Length),
					dr => setProperty(data[cnt++], (TProperty)dr.GetValue(0)));
			}
		}
	}
}
