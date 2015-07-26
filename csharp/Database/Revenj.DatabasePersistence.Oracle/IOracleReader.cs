using System;
using System.Data;

namespace Revenj.DatabasePersistence.Oracle
{
	public interface IOracleReader
	{
		void Read(string prefix, IDataReader dr, IServiceProvider locator);
	}
}
