using System.Data;
using Revenj.DomainPatterns;

namespace Revenj.DatabasePersistence.Oracle
{
	public interface IOracleReader
	{
		void Read(string prefix, IDataReader dr, IServiceLocator locator);
	}
}
