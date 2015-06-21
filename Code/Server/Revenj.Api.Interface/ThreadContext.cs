using System;
using System.Collections.Generic;
using System.Net;

namespace Revenj.Api
{
	/// <summary>
	/// Thread context for current HTTP request
	/// </summary>
	public static class ThreadContext
	{
		/// <summary>
		/// Input request
		/// </summary>
		[ThreadStatic]
		public static IRequestContext Request;
		/// <summary>
		/// Output response
		/// </summary>
		[ThreadStatic]
		public static IResponseContext Response;
	}

	/// <summary>
	/// Input HTTP request context.
	/// Contains basic HTTP information
	/// </summary>
	public interface IRequestContext
	{
		/// <summary>
		/// Accept MIME input type
		/// </summary>
		string Accept { get; }
		/// <summary>
		/// Content-length argument
		/// </summary>
		long ContentLength { get; }
		/// <summary>
		/// Content-type argument
		/// </summary>
		string ContentType { get; }
		/// <summary>
		/// Uri on which the request was received
		/// </summary>
		Uri RequestUri { get; }
		/// <summary>
		/// Template Uri matching information.
		/// Contains query arguments, matching template, etc...
		/// </summary>
		UriTemplateMatch UriTemplateMatch { get; set; }
		/// <summary>
		/// Read custom header.
		/// Returns null if not found
		/// </summary>
		/// <param name="name">header name</param>
		/// <returns>found value</returns>
		string GetHeader(string name);
	}

	/// <summary>
	/// Output HTTP response context.
	/// Contains basic HTTP information.
	/// Stream is separately returned
	/// </summary>
	public interface IResponseContext
	{
		/// <summary>
		/// Content-type response MIME type
		/// </summary>
		string ContentType { get; set; }
		/// <summary>
		/// Response Content-length
		/// </summary>
		long ContentLength { get; set; }
		/// <summary>
		/// All HTTP response headers
		/// </summary>
		IDictionary<string, string> Headers { get; }
		/// <summary>
		/// Response status code
		/// </summary>
		HttpStatusCode StatusCode { get; set; }
	}
}
