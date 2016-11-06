using System;
using System.Security.Principal;

namespace Revenj.Api
{
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
		/// Returns null if not found.
		/// Use lowercase values for lookup
		/// </summary>
		/// <param name="name">header name</param>
		/// <returns>found value</returns>
		string GetHeaderLowercase(string name);
		/// <summary>
		/// Security context
		/// </summary>
		IPrincipal Principal { get; }
	}

	public static class RequestContextHelper
	{
		public static string GetHeader(this IRequestContext request, string name)
		{
			if (name == null) return null;
			return request.GetHeaderLowercase(name.ToLowerInvariant());
		}
	}
}
