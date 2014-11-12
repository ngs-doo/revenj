using System.ComponentModel;
using System.IO;
using System.ServiceModel;
using System.ServiceModel.Web;

namespace Revenj.Plugins.Rest.Commands
{
	[ServiceContract(Namespace = "https://dsl-platform.com")]
	public interface IDomainCommands
	{
		[OperationContract]
		[WebGet(UriTemplate = "/find/{domainObject}/{*uris}")]
		[Description("Find domain objects by their URIs")]
		Stream Find(string domainObject, string uris);

		[OperationContract]
		[WebGet(UriTemplate = "/find/{domainObject}?uris={uris}")]
		[Description("Find domain objects by their URIs")]
		Stream FindQuery(string domainObject, string uris);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/find/{domainObject}")]
		[Description("Find domain objects by their URIs")]
		Stream FindFrom(string domainObject, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/search/{domainObject}/{specification}?limit={limit}&offset={offset}&order={order}&count={count}")]
		[Description("Search domain object with specification")]
		Stream SearchWithSpecification(string domainObject, string specification, string limit, string offset, string order, string count, Stream body);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/search/{domainObject}?specification={specification}&limit={limit}&offset={offset}&order={order}&count={count}")]
		[Description("Search domain object with specification")]
		Stream SearchWithSpecificationQuery(string domainObject, string specification, string limit, string offset, string order, string count, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/search/{domainObject}?specification={specification}&limit={limit}&offset={offset}&order={order}&count={count}")]
		[Description("Search domain object with optional specification from query parameters")]
		Stream SearchQuery(string domainObject, string specification, string limit, string offset, string order, string count);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/search-generic/{domainObject}?limit={limit}&offset={offset}&order={order}&count={count}")]
		[Description("Search domain object using generic specification")]
		Stream SearchWithGenericSpecification(string domainObject, string limit, string offset, string order, string count, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/search-generic/{domainObject}?limit={limit}&offset={offset}&order={order}&count={count}")]
		[Description("Search domain object using generic specification. Search arguments are in query string")]
		Stream SearchWithGenericSpecificationQuery(string domainObject, string limit, string offset, string order, string count);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/search-expression/{domainObject}?limit={limit}&offset={offset}&order={order}&count={count}")]
		[Description("Search domain object using expression")]
		Stream SearchWithExpression(string domainObject, string limit, string offset, string order, string count, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/count/{domainObject}/{specification}")]
		[Description("Count domain object with specification")]
		Stream CountWithSpecification(string domainObject, string specification, Stream body);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/count/{domainObject}?specification={specification}")]
		[Description("Count domain object with specification")]
		Stream CountWithSpecificationQuery(string domainObject, string specification, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/count/{domainObject}?specification={specification}")]
		[Description("Count domain object with optional specification from query parameters")]
		Stream CountQuery(string domainObject, string specification);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/count-generic/{domainObject}")]
		[Description("Count domain object using generic specification")]
		Stream CountWithGenericSpecification(string domainObject, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/count-generic/{domainObject}")]
		[Description("Count domain object using generic specification. Search arguments are in query string")]
		Stream CountWithGenericSpecificationQuery(string domainObject);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/count-expression/{domainObject}")]
		[Description("Count domain object using expression")]
		Stream CountWithExpression(string domainObject, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/submit/{domainEvent}?result={result}")]
		[Description("Submit domain event")]
		Stream SubmitEvent(string domainEvent, string result, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/submit/{aggregate}/{domainEvent}/{*uri}")]
		[Description("Submit domain event")]
		Stream SubmitAggregateEvent(string aggregate, string domainEvent, string uri, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/submit/{aggregate}/{domainEvent}?uri={uri}")]
		[Description("Submit domain event")]
		Stream SubmitAggregateEventQuery(string aggregate, string domainEvent, string uri, Stream body);
	}
}
