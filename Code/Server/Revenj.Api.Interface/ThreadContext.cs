using System;
using System.Collections.Generic;
using System.Net;

namespace Revenj.Api
{
	public static class ThreadContext
	{
		[ThreadStatic]
		public static IRequestContext Request;
		[ThreadStatic]
		public static IResponseContext Response;
	}

	public interface IRequestContext
	{
		string Accept { get; }
		long ContentLength { get; }
		string ContentType { get; }
		Uri RequestUri { get; }
		UriTemplateMatch UriTemplateMatch { get; set; }
		string GetHeader(string name);
	}

	public interface IResponseContext
	{
		string ContentType { get; set; }
		long ContentLength { get; set; }
		IDictionary<string, string> Headers { get; }
		HttpStatusCode StatusCode { get; set; }
	}
}
