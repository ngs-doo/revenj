using System;
using System.Linq;
using System.Linq.Expressions;
using System.Security.Principal;
using Revenj.Security;

namespace Revenj.Http
{
	public class NoAuth : HttpAuth, IPermissionManager
	{
		private static readonly GenericPrincipal Guest = new GenericPrincipal(new GenericIdentity("guest"), new string[0]);

		public NoAuth() : base(null, null, null) { }

		public override AuthorizeOrError TryAuthorize(string authorization, string url, RouteHandler route)
		{
			return AuthorizeOrError.Success(Guest);
		}

		public bool CanAccess(string identifier, IPrincipal user) { return true; }
		public IQueryable<T> ApplyFilters<T>(IPrincipal user, IQueryable<T> data) { return data; }
		public T[] ApplyFilters<T>(IPrincipal user, T[] data) { return data; }
		public IDisposable RegisterFilter<T>(Expression<System.Func<T, bool>> filter, string role, bool inverse) { return null; }
	}
}
