using System.Collections.Generic;
using System.Threading.Tasks;
using Revenj.DomainPatterns;

namespace Revenj
{
	internal class ClientRepository<T> : ClientSearchableRepository<T>, IRepository<T>
		where T : class, IIdentifiable
	{
		public ClientRepository(IDomainProxy domainProxy)
			: base(domainProxy) { }

		public Task<T[]> Find(IEnumerable<string> uris)
		{
			return DomainProxy.Find<T>(uris);
		}
	}
}
