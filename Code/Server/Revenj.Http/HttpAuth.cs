using System;
using System.Collections.Generic;
using System.Configuration;
using System.Net;
using System.Security.Principal;
using System.Text;
using NGS;
using NGS.Security;

namespace Revenj.Http
{
	public class HttpAuth
	{
		protected readonly IPrincipalFactory PrincipalFactory;
		private static readonly string DefaultAuthorization = ConfigurationManager.AppSettings["DefaultAuthorization"];
		protected readonly IAuthentication<string> PassAuthentication;
		protected readonly IAuthentication<byte[]> HashAuthentication;
		protected static readonly HashSet<string> PublicUrl = new HashSet<string>();
		protected static readonly HashSet<UriTemplate> PublicTemplate = new HashSet<UriTemplate>();
		private static readonly string MissingBasicAuth = "Basic realm=\"" + Environment.MachineName + "\"";

		static HttpAuth()
		{
			foreach (string key in ConfigurationManager.AppSettings.Keys)
			{
				if (key.StartsWith("PublicUrl"))
					PublicUrl.Add(ConfigurationManager.AppSettings[key]);
				else if (key.StartsWith("PublicTemplate"))
					PublicTemplate.Add(new UriTemplate(ConfigurationManager.AppSettings[key]));
			}
		}

		public HttpAuth(
			IPrincipalFactory principalFactory,
			IAuthentication<string> passAuthentication,
			IAuthentication<byte[]> hashAuthentication)
		{
			this.PrincipalFactory = principalFactory;
			this.PassAuthentication = passAuthentication;
			this.HashAuthentication = hashAuthentication;
		}

		private struct RestIdentity : IIdentity
		{
			private readonly string authenticationType;
			private readonly bool isAuthenticated;
			private readonly string name;

			public RestIdentity(string authType, bool isAuth, string name)
			{
				this.authenticationType = authType;
				this.isAuthenticated = isAuth;
				this.name = name;
			}

			public string AuthenticationType { get { return authenticationType; } }
			public bool IsAuthenticated { get { return isAuthenticated; } }
			public string Name { get { return name; } }
		}

		public struct AuthorizeOrError
		{
			public readonly IPrincipal Principal;
			public readonly string Error;
			public readonly HttpStatusCode ResponseCode;

			private AuthorizeOrError(IPrincipal principal, string error, HttpStatusCode responseCode)
			{
				this.Principal = principal;
				this.Error = error;
				this.ResponseCode = responseCode;
			}

			public static AuthorizeOrError Success(IPrincipal principal)
			{
				return new AuthorizeOrError(principal, null, HttpStatusCode.OK);
			}

			public static AuthorizeOrError Fail(string error, HttpStatusCode response)
			{
				return new AuthorizeOrError(null, error, response);
			}
		}

		public virtual AuthorizeOrError TryAuthorize(HttpListenerContext context, RouteHandler route)
		{
			var request = context.Request;
			var authorization = request.Headers["Authorization"] ?? DefaultAuthorization;
			if (authorization == null)
			{
				context.Response.AddHeader("WWW-Authenticate", MissingBasicAuth);
				return AuthorizeOrError.Fail("Authorization header not provided.", HttpStatusCode.Unauthorized);
			}

			var splt = authorization.Split(' ');
			var authType = splt[0];
			if (splt.Length != 2)
				return AuthorizeOrError.Fail("Invalid authorization header.", HttpStatusCode.Unauthorized);

			var cred = Encoding.UTF8.GetString(Convert.FromBase64String(splt[1])).Split(':');
			if (cred.Length != 2)
				return AuthorizeOrError.Fail("Invalid authorization header content.", HttpStatusCode.Unauthorized);

			var user = cred[0];

			if (string.IsNullOrEmpty(user))
				return AuthorizeOrError.Fail("User not specified in authorization header.", HttpStatusCode.Unauthorized);

			var isAuthenticated = authType == "Hash"
				? HashAuthentication.IsAuthenticated(user, Convert.FromBase64String(cred[1]))
				: PassAuthentication.IsAuthenticated(user, cred[1]);

			var identity = new RestIdentity(authType, isAuthenticated, user);
			if (!identity.IsAuthenticated)
			{
				if ((PublicUrl.Contains(request.RawUrl)
					|| PublicTemplate.Contains(route.Template)))
					return AuthorizeOrError.Success(PrincipalFactory.Create(identity));
				return AuthorizeOrError.Fail("User {0} was not authenticated.".With(user), HttpStatusCode.Forbidden);
			}
			return AuthorizeOrError.Success(PrincipalFactory.Create(identity));
		}
	}
}
