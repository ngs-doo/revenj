using System.Runtime.Serialization;

namespace Revenj.Api
{
	/// <summary>
	/// SOAP command description.
	/// </summary>
	[DataContract(Namespace = "")]
	public class SoapCommandDescription
	{
		/// <summary>
		/// Request identifier. Result is paired with request id.
		/// </summary>
		[DataMember]
		public string RequestID { get; private set; }
		/// <summary>
		/// Server command name. Command will be looked up from provided name.
		/// </summary>
		[DataMember]
		public string CommandName { get; private set; }
		/// <summary>
		/// Server command argument serialized as XML.
		/// </summary>
		[DataMember]
		public string Data { get; private set; }
		/// <summary>
		/// Create SOAP command description.
		/// </summary>
		/// <param name="requestID">request identifier</param>
		/// <param name="commandName">server command</param>
		/// <param name="data">XML as string</param>
		/// <returns>command description</returns>
		public static SoapCommandDescription Create(string requestID, string commandName, string data)
		{
			return new SoapCommandDescription
			{
				RequestID = requestID,
				CommandName = commandName,
				Data = data
			};
		}
	}
}
