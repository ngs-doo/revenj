using System;
using System.Security.Principal;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Threading;
using Revenj.Api;

namespace Revenj.Wcf
{
	internal class WcfRequest : IRequestContext
	{
		private IncomingWebRequestContext Request { get { return WebOperationContext.Current.IncomingRequest; } }

		public string Accept { get { return Request.Accept; } }
		public long ContentLength { get { return Request.ContentLength; } }
		public string ContentType { get { return Request.ContentType; } }
		public UriTemplateMatch UriTemplateMatch
		{
			get { return Request.UriTemplateMatch; }
			set { Request.UriTemplateMatch = value; }
		}
		public string GetHeaderLowercase(string name) { return Request.Headers[name]; }

		public Uri RequestUri
		{
			get { return OperationContext.Current.RequestContext.RequestMessage.Headers.To; }
		}

		public IPrincipal Principal { get { return Thread.CurrentPrincipal; } }

		public override string ToString()
		{
			long cl = -1;
			try { cl = ContentLength; }
			catch { }
			return @"WCF
URL: {0}
Accept: {1}
Content type: {2}
Content length: {3}".With(RequestUri.AbsoluteUri, Accept, ContentType, cl);
		}
	}
}
