using System;
using System.Collections.Generic;
using NGS.Common;

namespace NGS.DatabasePersistence.Postgres
{
	public static class PostgresSequence
	{
		public static void Fill<TValue, TProperty>(
			this IDatabaseQuery query,
			List<TValue> data,
			string sequenceName,
			Action<TValue, TProperty> setProperty)
		{
			if (data.Count > 0)
			{
				var seqence =
					query.Fill(
						@"/*NO LOAD BALANCE*/SELECT {0} FROM generate_series(1, {1})".With(sequenceName, data.Count),
						dr => (TProperty)dr.GetValue(0));
				if (seqence.Count != data.Count)
					throw new FrameworkException("Expected {0} new sequence. Got only {1}".With(data.Count, seqence.Count));
				for (int i = 0; i < seqence.Count; i++)
					setProperty(data[i], seqence[i]);
			}
		}
	}
}
