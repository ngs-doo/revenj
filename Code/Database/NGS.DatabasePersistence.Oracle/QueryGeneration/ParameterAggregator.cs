using System.Collections.Generic;
using Oracle.DataAccess.Client;

namespace NGS.DatabasePersistence.Oracle.QueryGeneration
{
	public class ParameterAggregator
	{
		private readonly List<OracleParameter> NamedParameters = new List<OracleParameter>();

		public string Add(OracleParameter parameter)
		{
			var name = ":p" + (NamedParameters.Count + 1);
			parameter.ParameterName = name;
			NamedParameters.Add(parameter);
			return name;
		}

		public IEnumerable<OracleParameter> Parameters { get { return NamedParameters; } }
	}
}