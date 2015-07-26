using System.Collections;
using System.Data.Common;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	public interface IOracleTypeConverter
	{
		string ToString(object value);
		DbParameter ToParameter(object value);
	}
	public interface IOracleVarrayConverter
	{
		string ToStringVarray(IEnumerable value);
		DbParameter ToParameterVarray(IEnumerable value);
	}
}
