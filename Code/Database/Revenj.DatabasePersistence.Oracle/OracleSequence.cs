using System;

namespace Revenj.DatabasePersistence.Oracle
{
	public static class OracleSequence
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
					@"SELECT {0} FROM dual CONNECT BY LEVEL <= {1}".With(sequenceName, data.Length),
					dr => setProperty(data[cnt++], (TProperty)Convert.ChangeType(dr.GetValue(0), typeof(TProperty))));
			}
		}
	}
}
