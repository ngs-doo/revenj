using System.ServiceModel;

namespace Revenj.Api
{
	/// <summary>
	/// Generic SOAP API service.
	/// Allows for multiple commands at once.
	/// All commands are executed inside a single transaciton.
	/// SOAP uses XML serialization.
	/// </summary>
	[ServiceContract(Namespace = "https://dsl-platform.com")]
	public interface ISoapApplication
	{
		/// <summary>
		/// Execute requested commands defined by description.
		/// </summary>
		/// <param name="soapCommands">comand description</param>
		/// <returns>executed results</returns>
		[OperationContract]
		SoapResultDescription Execute(SoapCommandDescription[] soapCommands);
	}
	/// <summary>
	/// Utility for SOAP API
	/// </summary>
	public static class SoapApplicationHelper
	{
		/// <summary>
		/// Execute requested command(s) defined by description(s).
		/// </summary>
		/// <param name="app">soap application service</param>
		/// <param name="commands">comand descriptions</param>
		/// <returns>executed results</returns>
		public static SoapResultDescription Execute(this ISoapApplication app, params SoapCommandDescription[] commands)
		{
			return app.Execute(commands);
		}
	}
}
