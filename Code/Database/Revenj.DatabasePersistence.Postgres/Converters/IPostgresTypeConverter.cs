using Revenj.DomainPatterns;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public interface IPostgresTypeConverter
	{
		object CreateInstance(object value, IServiceLocator locator);
		IPostgresTuple ToTuple(object instance);
	}
}
