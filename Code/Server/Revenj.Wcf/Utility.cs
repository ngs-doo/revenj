using System.IO;
using System.Net;
using System.Text;
using Revenj.Api;

namespace Revenj.Wcf
{
	public static class Utility
	{
		public static Stream ReturnError(string message, HttpStatusCode code)
		{
			ThreadContext.Response.StatusCode = code;
			ThreadContext.Response.ContentType = "text/plain; charset=\"utf-8\"";
			return new MemoryStream(Encoding.UTF8.GetBytes(message));
		}
	}
}