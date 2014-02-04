using System.IO;
using System.Net;
using System.ServiceModel.Web;
using System.Text;
using Revenj.Api;

namespace Revenj.Wcf
{
	public static class Utility
	{
		public static void ThrowError(string message, HttpStatusCode code)
		{
			ThreadContext.Response.StatusCode = code;
			//TODO: should return text/plain, but that doesn't work
			switch (ThreadContext.Request.Accept)
			{
				case "application/json":
					ThreadContext.Response.ContentType = "application/json";
					break;
				default:
					ThreadContext.Response.ContentType = "application/xml; charset=\"utf-8\"";
					break;
			}
#if MONO
			throw new System.Exception(message);
#else
			throw new WebFaultException<string>(message, code);
#endif
		}

		public static Stream ReturnError(string message, HttpStatusCode code)
		{
			ThreadContext.Response.StatusCode = code;
			ThreadContext.Response.ContentType = "text/plain; charset=\"utf-8\"";
			return new MemoryStream(Encoding.UTF8.GetBytes(message));
		}
	}
}