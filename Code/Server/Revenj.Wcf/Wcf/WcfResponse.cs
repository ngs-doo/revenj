using System.Net;
using System.ServiceModel.Web;
using Revenj.Api;

namespace Revenj.Wcf
{
	internal class WcfResponse : IResponseContext
	{
		private OutgoingWebResponseContext Response { get { return WebOperationContext.Current.OutgoingResponse; } }

		string IResponseContext.ContentType
		{
			get { return Response.ContentType; }
			set
			{
				Response.ContentType = value;
				//if (value == "application/json") Response.Format = WebMessageFormat.Json; TODO: Mono doesnt support Response.Format
			}
		}

		public long ContentLength
		{
			get { return Response.ContentLength; }
			set { Response.ContentLength = value; }
		}

		public void AddHeader(string type, string value)
		{
			Response.Headers.Add(type, value);
		}

		public HttpStatusCode StatusCode
		{
			get { return Response.StatusCode; }
			set { Response.StatusCode = value; }
		}
	}
}
