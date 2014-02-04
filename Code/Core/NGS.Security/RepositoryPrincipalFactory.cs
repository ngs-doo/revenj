using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Security.Principal;
using NGS.DomainPatterns;

namespace NGS.Security
{
	public class RepositoryPrincipalFactory : IPrincipalFactory, IDisposable
	{
		private readonly IQueryableRepository<IUserRoles> Repository;
		private Dictionary<string, HashSet<string>> RoleCache = new Dictionary<string, HashSet<string>>();
		private readonly IDisposable Subscription;

		public RepositoryPrincipalFactory(
			IQueryableRepository<IUserRoles> repository,
			IDataChangeNotification notifications)
		{
			Contract.Requires(repository != null);
			Contract.Requires(notifications != null);

			this.Repository = repository;
			Subscription = notifications.Track<IUserRoles>().Subscribe(_ => RefreshCache());
			RefreshCache();
		}

		private void RefreshCache()
		{
			var allRoles = Repository.FindAll().ToList();
			var roles =
				(from r in allRoles
				 group r by r.Name into grp
				 select new
				 {
					 grp.Key,
					 Roles = new HashSet<string>(grp.Select(it => it.ParentName))
				 }).ToList();
			RoleCache = roles.ToDictionary(it => it.Key, it => it.Roles);
		}

		private static HashSet<string> NoRoles = new HashSet<string>();

		public IPrincipal Create(IIdentity user)
		{
			return
				new UserPrincipal(
					user,
					new Lazy<HashSet<string>>(() =>
						{
							HashSet<string> roles;
							if (RoleCache.TryGetValue(user.Name, out roles))
								return roles;
							return NoRoles;
						}));
		}

		public void Dispose()
		{
			Subscription.Dispose();
		}
	}
}
