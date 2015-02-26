using Revenj.DomainPatterns;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public interface IPostgresTypeConverter
	{
		object CreateInstance(object value, BufferedTextReader reader, IServiceLocator locator);
		IPostgresTuple ToTuple(object instance);
	}
}
