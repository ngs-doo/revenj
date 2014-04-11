using System;
using System.Collections.Generic;
using System.Configuration;
using System.IdentityModel.Claims;
using System.IdentityModel.Policy;
using System.Net;
using System.Security.Principal;
using System.Text;
using NGS;
using NGS.Security;
using Revenj.Api;

namespace Revenj.Wcf
{
	public class StandardAuthorizationPolicy : IAuthorizationPolicy
	{
		private readonly IPrincipalFactory PrincipalFactory = ContainerWcfHost.Resolve<IPrincipalFactory>();
		private static readonly string DefaultAuthorization = ConfigurationManager.AppSettings["DefaultAuthorization"];
		protected readonly IAuthentication<string> PassAuthentication = ContainerWcfHost.Resolve<IAuthentication<string>>();
		protected readonly IAuthentication<byte[]> HashAuthentication = ContainerWcfHost.Resolve<IAuthentication<byte[]>>();
		private static readonly HashSet<string> PublicUrl = new HashSet<string>();
		private static readonly HashSet<string> PublicTemplate = new HashSet<string>();
		private static readonly string MissingBasicAuth = "Basic realm=\"" + Environment.MachineName + "\"";

		static StandardAuthorizationPolicy()
		{
			foreach (string key in ConfigurationManager.AppSettings.Keys)
			{
				if (key.StartsWith("PublicUrl"))
					PublicUrl.Add(ConfigurationManager.AppSettings[key]);
				else if (key.StartsWith("PublicTemplate"))
					PublicTemplate.Add(ConfigurationManager.AppSettings[key]);
			}
		}

		public StandardAuthorizationPolicy()
		{
			id = GetHashCode().ToString();
		}

		public bool Evaluate(EvaluationContext evaluationContext, ref object state)
		{
			var client = GetClientIdentity(evaluationContext);
			evaluationContext.Properties["Principal"] = PrincipalFactory.Create(client);

			return true;
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

		protected virtual IIdentity GetIdentity(string authHeader)
		{
			var splt = authHeader.Split(' ');
			var authType = splt[0];
			if (splt.Length != 2)
				Utility.ThrowError("Invalid authorization header.", HttpStatusCode.Unauthorized);

			var token = Encoding.UTF8.GetString(Convert.FromBase64String(splt[1]));
			var cred = token.Split(':');

			if (cred.Length != 2)
				Utility.ThrowError("Invalid authorization header content.", HttpStatusCode.Unauthorized);

			var user = cred[0];

			if (string.IsNullOrEmpty(user))
				Utility.ThrowError("User not specified in authorization header.", HttpStatusCode.Unauthorized);

			var isAuthenticated = authType == "Hash"
				? HashAuthentication.IsAuthenticated(user, Convert.FromBase64String(cred[1]))
				: PassAuthentication.IsAuthenticated(user, cred[1]);

			return new RestIdentity(authType, isAuthenticated, user);
		}

		private IIdentity GetClientIdentity(EvaluationContext evaluationContext)
		{
			object obj;
			if (!evaluationContext.Properties.TryGetValue("Identities", out obj))
			{
				var authorization = ThreadContext.Request.GetHeader("Authorization") ?? DefaultAuthorization;
				if (authorization == null)
				{
					ThreadContext.Response.Headers["WWW-Authenticate"] = MissingBasicAuth;
					Utility.ThrowError("Authorization header not provided.", HttpStatusCode.Unauthorized);
				}

				var identity = GetIdentity(authorization);
				var template = ThreadContext.Request.UriTemplateMatch;
				if (!identity.IsAuthenticated)
				{
					if (template != null
						&& (template.RequestUri != null && PublicUrl.Contains(template.RequestUri.LocalPath)
						|| template.Template != null && PublicTemplate.Contains(template.Template.ToString())))
						return identity;
					Utility.ThrowError("User {0} was not authenticated.".With(identity.Name), HttpStatusCode.Forbidden);
				}
				else if (template == null)
				{
					var url = ThreadContext.Request.RequestUri;
					Utility.ThrowError("Unknown route: " + url.PathAndQuery, HttpStatusCode.NotFound);
				}
				return identity;
			}

			var identities = obj as IList<IIdentity>;
			if (identities == null || identities.Count < 1)
				Utility.ThrowError("No Identity found.", HttpStatusCode.Unauthorized);

			return identities[0];
		}

		public ClaimSet Issuer { get { return ClaimSet.System; } }
		private string id;
		public string Id { get { return id; } }
	}
}
