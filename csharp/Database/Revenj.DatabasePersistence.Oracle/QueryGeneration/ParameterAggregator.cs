using System.Collections.Generic;
using System.Data.Common;

namespace Revenj.DatabasePersistence.Oracle.QueryGeneration
{
	public class ParameterAggregator
	{
		private readonly List<DbParameter> NamedParameters = new List<DbParameter>();

		public string Add(DbParameter parameter)
		{
			var name = ":p" + (NamedParameters.Count + 1);
			parameter.ParameterName = name;
			NamedParameters.Add(parameter);
			return name;
		}

		public IEnumerable<DbParameter> Parameters { get { return NamedParameters; } }
	}
}