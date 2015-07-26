using System;
using Oracle.DataAccess.Types;
using Revenj.DatabasePersistence.Oracle.Converters;

namespace Revenj.DatabasePersistence.Oracle
{
	public interface IOracleDto : IOracleCustomType, IOracleCustomTypeFactory, IOracleTypeConverter
	{
		object Convert(IServiceProvider locator);
	}
}
