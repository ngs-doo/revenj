using NGS.DomainPatterns;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public interface IPostgresTypeConverter
	{
		object CreateInstance(object value, IServiceLocator locator);
		PostgresTuple ToTuple(object instance);
	}
}
