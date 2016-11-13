using System.Collections.Generic;
using System.Threading.Tasks;
using Revenj.DomainPatterns;

namespace Revenj
{
	internal class ClientPersistableRepository<T> : ClientRepository<T>, IPersistableRepository<T>
		where T : class, IAggregateRoot
	{
		protected readonly IStandardProxy StandardProxy;

		public ClientPersistableRepository(
			IDomainProxy domainProxy,
			IStandardProxy standardProxy)
			: base(domainProxy)
		{
			this.StandardProxy = standardProxy;
		}

		public Task<string[]> Persist(IEnumerable<T> insert, IEnumerable<KeyValuePair<T, T>> update, IEnumerable<T> delete)
		{
			return StandardProxy.Persist(insert, update, delete);
		}
	}
}
