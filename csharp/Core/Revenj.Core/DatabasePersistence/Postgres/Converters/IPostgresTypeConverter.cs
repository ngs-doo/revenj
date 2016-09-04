using System;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public interface IPostgresTypeConverter
	{
		object CreateInstance(object value, BufferedTextReader reader, IServiceProvider locator);
		IPostgresTuple ToTuple(object instance);
	}
}
