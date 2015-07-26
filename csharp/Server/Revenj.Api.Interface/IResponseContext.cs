using System.Net;

namespace Revenj.Api
{
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
		/// Add response headers
		/// </summary>
		void AddHeader(string type, string value);
		/// <summary>
		/// Response status code
		/// </summary>
		HttpStatusCode StatusCode { get; set; }
	}
}
