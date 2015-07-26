using System.Runtime.Serialization;

namespace Revenj.Api
{
	/// <summary>
	/// Collection of SOAP command results.
	/// </summary>
	[DataContract(Namespace = "")]
	public class SoapResultDescription
	{
		/// <summary>
		/// Global response message.
		/// </summary>
		[DataMember]
		public string Message { get; private set; }
		/// <summary>
		/// Results from executed commands.
		/// </summary>
		[DataMember]
		public SoapCommandResultDescription[] ExecutedCommands { get; private set; }
		/// <summary>
		/// Create SOAP result.
		/// </summary>
		/// <param name="message">global response message</param>
		/// <param name="executedCommands">executed command results</param>
		/// <returns>created result</returns>
		public static SoapResultDescription Create(string message, SoapCommandResultDescription[] executedCommands)
		{
			return new SoapResultDescription
			{
				Message = message,
				ExecutedCommands = executedCommands
			};
		}
	}
}
