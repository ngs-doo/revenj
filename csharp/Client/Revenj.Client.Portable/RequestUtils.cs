using System.IO;
using System.Net;

namespace Revenj
{
	internal static class RequestUtils
	{
		public static Stream GetRequestStream(this HttpWebRequest source)
		{
			var ar = source.BeginGetRequestStream(null, null);
			ar.AsyncWaitHandle.WaitOne();
			return source.EndGetRequestStream(ar);
		}

		public static WebResponse GetResponse(this HttpWebRequest source)
		{
			var ar = source.BeginGetResponse(null, null);
			ar.AsyncWaitHandle.WaitOne();
			return source.EndGetResponse(ar);
		}
	}
}
