using System.IO;
using System.ServiceModel;
using System.ServiceModel.Web;

namespace Revenj.Api
{
	/// <summary>
	/// Generic RPC-like API.
	/// Server command is loaded from uri argument.
	/// Result stream will be serialized with requested serialization.
	/// Request is executed inside a single transaction.
	/// </summary>
	[ServiceContract(Namespace = "https://dsl-platform.com")]
	public interface IRestApplication
	{
		/// <summary>
		/// Call server command without argument.
		/// </summary>
		/// <returns>serialized response</returns>
		[OperationContract]
		[WebGet(UriTemplate = "*")]
		Stream Get();
		/// <summary>
		/// Call server command with provided argument.
		/// Argument is deserialized using specified serialization.
		/// </summary>
		/// <param name="argument">serialized request argument</param>
		/// <returns>serialized response</returns>
		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "*")]
		Stream Post(Stream argument);
	}
}
