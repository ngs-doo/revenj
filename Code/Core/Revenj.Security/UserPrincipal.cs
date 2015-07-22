using System;
using System.Collections.Generic;
using System.Security.Principal;

namespace Revenj.Security
{
	internal class UserPrincipal : IPrincipal
	{
		private readonly IIdentity User;
		private readonly Lazy<HashSet<string>> Roles;

		public UserPrincipal(IIdentity user, Lazy<HashSet<string>> roles)
		{
			this.User = user;
			this.Roles = roles;
		}

		public IIdentity Identity { get { return User; } }

		public bool IsInRole(string role)
		{
			return role == Identity.Name || Roles.Value.Contains(role);
		}
	}
}
