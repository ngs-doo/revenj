using System;
using System.Linq;
using System.Linq.Expressions;
using System.Net;
using System.Security.Principal;
using NGS.Security;

namespace Revenj.Http
{
	public class NoAuth : HttpAuth, IPermissionManager
	{
		private readonly string[] NoRoles = new string[0];

		public NoAuth() : base(null, null, null) { }

		public override AuthorizeOrError TryAuthorize(HttpListenerContext context, RouteHandler route)
		{
			return AuthorizeOrError.Success(new GenericPrincipal(new GenericIdentity("guest"), NoRoles));
		}

		public bool CanAccess(string identifier, IPrincipal user) { return true; }
		public IQueryable<T> ApplyFilters<T>(IPrincipal user, IQueryable<T> data) { return data; }
		public T[] ApplyFilters<T>(IPrincipal user, T[] data) { return data; }
		public IDisposable RegisterFilter<T>(Expression<System.Func<T, bool>> filter, string role, bool inverse) { return null; }
	}
}
