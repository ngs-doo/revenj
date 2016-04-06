using System.ComponentModel;
using System.IO;
using System.ServiceModel;
using System.ServiceModel.Web;

namespace Revenj.Plugins.Rest.Commands
{
	/// <summary>
	/// Server Create Read Update and Delete REST-like API.
	/// Content-type specifies body serialization.
	/// Accept specifies result serialization.
	/// </summary>
	[ServiceContract(Namespace = "https://dsl-platform.com")]
	public interface ICrudCommands
	{
		/// <summary>
		/// Create aggregate root from message body.
		/// Aggregate root is defined in URL.
		/// Return created aggregate root or it's URI (based on result = [uri|instance]) argument.
		/// Default is instance
		/// </summary>
		/// <param name="root">aggregate root name</param>
		/// <param name="result">return URI or whole object</param>
		/// <param name="body">serialized aggregate root</param>
		/// <returns>saved aggregate root</returns>
		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/{root}?result={result}")]
		[Description("Create aggregate root")]
		Stream Create(string root, string result, Stream body);
		/// <summary>
		/// Read domain object based on provided identifier.
		/// Return found domain object.
		/// If object is not found 404 is returned.
		/// </summary>
		/// <param name="domainObject">domain object name</param>
		/// <param name="uri">identifier</param>
		/// <returns>found domain object</returns>
		[OperationContract]
		[WebGet(UriTemplate = "/{domainObject}/{*uri}")]
		[Description("Get domain object with specified URI")]
		Stream Read(string domainObject, string uri);
		/// <summary>
		/// Read domain object based on provided identifier.
		/// Return found domain object.
		/// If object is not found 404 is returned.
		/// </summary>
		/// <param name="domainObject">domain object name</param>
		/// <param name="uri">identifier</param>
		/// <returns>found domain object</returns>
		[OperationContract]
		[WebGet(UriTemplate = "/{domainObject}?uri={uri}")]
		[Description("Get domain object with specified URI")]
		Stream ReadQuery(string domainObject, string uri);
		/// <summary>
		/// Change existing aggregate root.
		/// Return changed aggregate.
		/// Return created aggregate root or it's URI (based on result = [uri|instance]) argument.
		/// Default is instance
		/// </summary>
		/// <param name="root">aggregate root name</param>
		/// <param name="uri">identifier</param>
		/// <param name="body">serialized changed aggregate root</param>
		/// <returns>persisted aggregate root</returns>
		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/{root}/{*uri}")]
		[Description("Change aggregate root")]
		Stream Update(string root, string uri, Stream body);
		/// <summary>
		/// Change existing aggregate root.
		/// Return changed aggregate.
		/// </summary>
		/// <param name="root">aggregate root name</param>
		/// <param name="uri">identifier</param>
		/// <param name="result">return URI or whole object</param>
		/// <param name="body">serialized changed aggregate root</param>
		/// <returns>persisted aggregate root</returns>
		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/{root}?uri={uri}&result={result}")]
		[Description("Change aggregate root")]
		Stream UpdateQuery(string root, string uri, string result, Stream body);
		/// <summary>
		/// Delete existing aggregate root.
		/// Return deleted aggregate.
		/// </summary>
		/// <param name="root">aggregate root name</param>
		/// <param name="uri">identifier</param>
		/// <returns>deleted aggregate root</returns>
		[OperationContract]
		[WebInvoke(Method = "DELETE", UriTemplate = "/{root}/{*uri}")]
		[Description("Delete aggregate root")]
		Stream Delete(string root, string uri);
		/// <summary>
		/// Delete existing aggregate root.
		/// Return deleted aggregate.
		/// </summary>
		/// <param name="root">aggregate root name</param>
		/// <param name="uri">identifier</param>
		/// <returns>deleted aggregate root</returns>
		[OperationContract]
		[WebInvoke(Method = "DELETE", UriTemplate = "/{root}?uri={uri}")]
		[Description("Delete aggregate root")]
		Stream DeleteQuery(string root, string uri);
	}
}
