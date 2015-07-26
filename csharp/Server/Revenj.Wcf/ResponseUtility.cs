using System.IO;
using System.Net;
using System.Text;
using Revenj.Api;

namespace Revenj.Wcf
{
	internal static class ResponseUtility
	{
		public static Stream ReturnError(this IResponseContext response, string message, HttpStatusCode code)
		{
			response.StatusCode = code;
			response.ContentType = "text/plain; charset=UTF-8";
			return new MemoryStream(Encoding.UTF8.GetBytes(message));
		}
	}
}