using Microsoft.AspNetCore.Http;
using System.IO;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace Revenj.AspNetCore
{
	internal static class Utility
	{
		public static Task WriteError(this HttpResponse response, string message, HttpStatusCode code)
		{
			response.StatusCode = (int)code;
			response.ContentType = "text/plain; charset=UTF-8";
			var ms = new MemoryStream(Encoding.UTF8.GetBytes(message));
			return ms.CopyToAsync(response.Body);
		}
	}
}
