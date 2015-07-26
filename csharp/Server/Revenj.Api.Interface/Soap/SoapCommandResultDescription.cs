using System.Net;
using System.Runtime.Serialization;

namespace Revenj.Api
{
	/// <summary>
	/// Response for SOAP command from SOAP command description.
	/// </summary>
	[DataContract(Namespace = "")]
	public class SoapCommandResultDescription
	{
		/// <summary>
		/// Request id saved to response
		/// </summary>
		[DataMember]
		public string ResponseID { get; private set; }
		/// <summary>
		/// Command execution status.
		/// </summary>
		[DataMember]
		public HttpStatusCode Status { get; private set; }
		/// <summary>
		/// Server command message.
		/// </summary>
		[DataMember]
		public string Message { get; private set; }
		/// <summary>
		/// XML result serialized as string
		/// </summary>
		[DataMember]
		public string Data { get; private set; }
		/// <summary>
		/// Create SOAP command result.
		/// </summary>
		/// <param name="responseID">paired with request id</param>
		/// <param name="status">execution status</param>
		/// <param name="message">result message</param>
		/// <param name="data">serialized result</param>
		/// <returns>created command result</returns>
		public static SoapCommandResultDescription Create(string responseID, HttpStatusCode status, string message, string data)
		{
			return new SoapCommandResultDescription
			{
				ResponseID = responseID,
				Status = status,
				Message = message,
				Data = data
			};
		}
	}
}
