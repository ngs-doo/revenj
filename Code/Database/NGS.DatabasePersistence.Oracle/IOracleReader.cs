using System.Data;
using NGS.DomainPatterns;

namespace NGS.DatabasePersistence.Oracle
{
	public interface IOracleReader
	{
		void Read(string prefix, IDataReader dr, IServiceLocator locator);
	}
}
