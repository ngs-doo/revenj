using System;
using System.Net;
using System.Security.Principal;
using Revenj.Api;

namespace Revenj.Http
{
	internal class HttpListenerContex : IRequestContext, IResponseContext
	{
		private readonly HttpListenerRequest Request;
		private readonly HttpListenerResponse Response;
		private readonly RouteMatch RouteMatch;

		public HttpListenerContex(
			HttpListenerRequest request,
			HttpListenerResponse response,
			RouteMatch routeMatch,
			IPrincipal principal)
		{
			this.Request = request;
			this.Response = response;
			this.RouteMatch = routeMatch;
			this.Principal = principal;
			var at = request.AcceptTypes;
			if (at != null && at.Length == 1)
				AcceptType = at[0];
			else if (at != null)
				AcceptType = request.Headers["Accept"];
		}

		private readonly string AcceptType;
		public string Accept { get { return AcceptType; } }

		long IRequestContext.ContentLength { get { return Request.ContentLength64; } }
		long IResponseContext.ContentLength
		{
			get { return Response.ContentLength64; }
			set { Response.ContentLength64 = value; }
		}
		string IRequestContext.ContentType { get { return Request.ContentType; } }
		string IResponseContext.ContentType
		{
			get { return Response.ContentType; }
			set { Response.ContentType = value; }//PERF: slow
		}
		public Uri RequestUri { get { return Request.Url; } }
		private UriTemplateMatch TemplateMatch;
		public UriTemplateMatch UriTemplateMatch
		{
			get
			{
				if (TemplateMatch == null)
					TemplateMatch = RouteMatch.CreateTemplateMatch();
				return TemplateMatch;
			}
			set { TemplateMatch = value; }
		}
		public string GetHeaderLowercase(string name)
		{
			return Request.Headers.Get(name);
		}
		public void AddHeader(string type, string value)
		{
			Response.Headers.Add(type, value);
		}
		public IPrincipal Principal { get; private set; }
		public HttpStatusCode StatusCode
		{
			get { return (HttpStatusCode)Response.StatusCode; }
			set { Response.StatusCode = (int)value; }
		}

		public override string ToString()
		{
			long cl = -1;
			try { cl = Request.ContentLength64; }
			catch { }
			return string.Format(@"HTTP REQ:
URL: {0}
Accept: {1}
Content type: {2}
Content length: {3}", RequestUri.AbsoluteUri, Accept, Request.ContentType, cl);
		}
	}
}
