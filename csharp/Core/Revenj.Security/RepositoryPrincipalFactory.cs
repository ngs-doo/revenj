using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Security.Principal;

namespace Revenj.Security
{
	internal class RepositoryPrincipalFactory : IPrincipalFactory, IDisposable
	{
		private readonly IQueryable<IUserRoles> LoadRoles;
		private Dictionary<string, HashSet<string>> RoleCache = new Dictionary<string, HashSet<string>>();
		private readonly IDisposable Subscription;

		public RepositoryPrincipalFactory(
			IQueryable<IUserRoles> loadRoles,
			IObservable<Lazy<IUserRoles>> roleChanges)
		{
			Contract.Requires(loadRoles != null);
			Contract.Requires(roleChanges != null);

			this.LoadRoles = loadRoles;
			Subscription = roleChanges.Subscribe(_ => RefreshCache());
			RefreshCache();
		}

		private void RefreshCache()
		{
			var allRoles = LoadRoles.ToList();
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
