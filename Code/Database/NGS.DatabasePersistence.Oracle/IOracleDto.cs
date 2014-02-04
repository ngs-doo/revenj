using NGS.DatabasePersistence.Oracle.Converters;
using NGS.DomainPatterns;
using Oracle.DataAccess.Types;

namespace NGS.DatabasePersistence.Oracle
{
	public interface IOracleDto : IOracleCustomType, IOracleCustomTypeFactory, IOracleTypeConverter
	{
		object Convert(IServiceLocator locator);
	}
}
