using Oracle.DataAccess.Types;
using Revenj.DatabasePersistence.Oracle.Converters;
using Revenj.DomainPatterns;

namespace Revenj.DatabasePersistence.Oracle
{
	public interface IOracleDto : IOracleCustomType, IOracleCustomTypeFactory, IOracleTypeConverter
	{
		object Convert(IServiceLocator locator);
	}
}
