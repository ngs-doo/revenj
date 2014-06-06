using System;
using System.ServiceModel;
using System.ServiceModel.Web;
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
		public DateTime? IfModifiedSince
		{
			get
			{
				var hms = Request.Headers["If-Modified-Since"];
				if (string.IsNullOrEmpty(hms))
					return null;
				System.DateTime dt;
				System.DateTime.TryParse(hms, out dt);
				return dt;
				//return Request.IfModifiedSince; TODO: Mono doesnt support this parameter
			}
		}
		public DateTime? IfUnmodifiedSince
		{
			get
			{
				var hms = Request.Headers["If-Unmodified-Since"];
				if (string.IsNullOrEmpty(hms))
					return null;
				System.DateTime dt;
				System.DateTime.TryParse(hms, out dt);
				return dt;
				//return Request.IfUnmodifiedSince; TODO: Mono doesnt support this parameter
			}
		}
		public string GetHeader(string name) { return Request.Headers[name]; }

		public Uri RequestUri
		{
			get { return OperationContext.Current.RequestContext.RequestMessage.Headers.To; }
		}
	}
}
