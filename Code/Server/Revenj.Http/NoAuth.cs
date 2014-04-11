using System.Net;
using System.Security.Principal;

namespace Revenj.Http
{
	public class NoAuth : HttpAuth
	{
		private readonly string[] NoRoles = new string[0];

		public NoAuth() : base(null, null, null) { }

		public override AuthorizeOrError TryAuthorize(HttpListenerContext context, RouteHandler route)
		{
			return AuthorizeOrError.Success(new GenericPrincipal(new GenericIdentity("guest"), NoRoles));
		}
	}
}
