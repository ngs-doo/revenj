using System;
using System.Collections.Generic;
using NGS.Common;

namespace NGS.DatabasePersistence.Oracle
{
	public static class OracleSequence
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
						@"SELECT {0} FROM dual CONNECT BY LEVEL <= {1}".With(sequenceName, data.Count),
						dr => (decimal)dr.GetValue(0));
				if (seqence.Count != data.Count)
					throw new FrameworkException("Expected {0} from sequence. Got only {1}".With(data.Count, seqence.Count));
				for (int i = 0; i < seqence.Count; i++)
					setProperty(data[i], (TProperty)Convert.ChangeType(seqence[i], typeof(TProperty)));
			}
		}
	}
}
