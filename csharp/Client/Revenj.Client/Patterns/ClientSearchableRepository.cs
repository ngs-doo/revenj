using System.Collections.Generic;
using System.Threading.Tasks;
using Revenj.DomainPatterns;

namespace Revenj
{
	internal class ClientSearchableRepository<T> : ISearchableRepository<T>
		where T : class, ISearchable
	{
		protected readonly IDomainProxy DomainProxy;

		public ClientSearchableRepository(IDomainProxy domainProxy)
		{
			this.DomainProxy = domainProxy;
		}

		public Task<T[]> Search(ISpecification<T> specification, int? limit, int? offset, IDictionary<string, bool> order)
		{
			return DomainProxy.Search<T>(specification, limit, offset, order);
		}

		public Task<long> Count(ISpecification<T> specification)
		{
			return DomainProxy.Count<T>(specification);
		}
	}
}
