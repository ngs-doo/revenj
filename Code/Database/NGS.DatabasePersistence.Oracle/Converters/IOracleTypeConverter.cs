using System.Collections;
using Oracle.DataAccess.Client;

namespace NGS.DatabasePersistence.Oracle.Converters
{
	public interface IOracleTypeConverter
	{
		string ToString(object value);
		OracleParameter ToParameter(object value);
	}
	public interface IOracleVarrayConverter
	{
		string ToStringVarray(IEnumerable value);
		OracleParameter ToParameterVarray(IEnumerable value);
	}
}
