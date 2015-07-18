using System;
using System.Collections.Generic;
using System.Configuration;
using System.Net;
using System.Security.Principal;
using System.Text;
using Revenj.Security;

namespace Revenj.Http
{
	public class HttpAuth
	{
		protected readonly IPrincipalFactory PrincipalFactory;
		private static readonly string DefaultAuthorization = ConfigurationManager.AppSettings["DefaultAuthorization"];
		protected readonly IAuthentication<string> PassAuthentication;
		protected readonly IAuthentication<byte[]> HashAuthentication;
		protected static readonly HashSet<string> PublicUrl = new HashSet<string>();
		protected static readonly HashSet<string> PublicTemplate = new HashSet<string>();

		static HttpAuth()
		{
			foreach (string key in ConfigurationManager.AppSettings.Keys)
			{
				if (key.StartsWith("PublicUrl"))
					PublicUrl.Add(ConfigurationManager.AppSettings[key]);
				else if (key.StartsWith("PublicTemplate"))
					PublicTemplate.Add(ConfigurationManager.AppSettings[key]);
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
			public readonly bool SendAuthenticate;

			private AuthorizeOrError(IPrincipal principal, string error, HttpStatusCode responseCode, bool sendAuthenticate)
			{
				this.Principal = principal;
				this.Error = error;
				this.ResponseCode = responseCode;
				this.SendAuthenticate = sendAuthenticate;
			}

			public static AuthorizeOrError Success(IPrincipal principal)
			{
				return new AuthorizeOrError(principal, null, HttpStatusCode.OK, false);
			}

			public static AuthorizeOrError Unauthorized(string error, bool sendAuthenticate)
			{
				return new AuthorizeOrError(null, error, HttpStatusCode.Unauthorized, sendAuthenticate);
			}

			public static AuthorizeOrError Fail(string error, HttpStatusCode response)
			{
				return new AuthorizeOrError(null, error, response, false);
			}
		}

		public virtual AuthorizeOrError TryAuthorize(string authorization, string url, RouteHandler route)
		{
			if (authorization == null)
				return AuthorizeOrError.Unauthorized("Authorization header not provided.", true);

			var splt = authorization.Split(' ');
			var authType = splt[0];
			if (splt.Length != 2)
				return AuthorizeOrError.Unauthorized("Invalid authorization header.", false);

			var cred = Encoding.UTF8.GetString(Convert.FromBase64String(splt[1])).Split(':');
			if (cred.Length != 2)
				return AuthorizeOrError.Unauthorized("Invalid authorization header content.", false);

			var user = cred[0];

			if (string.IsNullOrEmpty(user))
				return AuthorizeOrError.Fail("User not specified in authorization header.", HttpStatusCode.Unauthorized);

			var isAuthenticated = authType == "Hash"
				? HashAuthentication.IsAuthenticated(user, Convert.FromBase64String(cred[1]))
				: PassAuthentication.IsAuthenticated(user, cred[1]);

			var identity = new RestIdentity(authType, isAuthenticated, user);
			if (!identity.IsAuthenticated)
			{
				if ((PublicUrl.Contains(url)
					|| PublicTemplate.Contains(route.Template)))
					return AuthorizeOrError.Success(PrincipalFactory.Create(identity));
				return AuthorizeOrError.Fail("User {0} was not authenticated.".With(user), HttpStatusCode.Forbidden);
			}
			return AuthorizeOrError.Success(PrincipalFactory.Create(identity));
		}
	}
}
