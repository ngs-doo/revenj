using System;

namespace Revenj.DomainPatterns
{
	//TODO: remove!?
	public interface IAggregateRootRepository<out TRoot> : IQueryableRepository<TRoot>, IRepository<TRoot>
		where TRoot : IAggregateRoot
	{
		TRoot[] Create(int count, Action<TRoot[]> initialize);
		TRoot[] Update(string[] uris, Action<TRoot[]> change);
		void Delete(string[] uris);
	}
	//TODO: remove!?
	public static class AggregateRootRepositoryHelper
	{
		public static TRoot Create<TRoot>(this IAggregateRootRepository<TRoot> repository, Action<TRoot> initialize)
			where TRoot : IAggregateRoot
		{
			return repository.Create(1, roots => initialize(roots[0]))[0];
		}

		public static TRoot Update<TRoot>(this IAggregateRootRepository<TRoot> repository, string uri, Action<TRoot> change)
			where TRoot : IAggregateRoot
		{
			return repository.Update(new[] { uri }, roots => change(roots[0]))[0];
		}

		public static void Delete<TRoot>(this IAggregateRootRepository<TRoot> repository, string uri)
			where TRoot : IAggregateRoot
		{
			repository.Delete(new[] { uri });
		}
	}
}
