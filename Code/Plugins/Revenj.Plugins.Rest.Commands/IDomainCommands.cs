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
		[WebGet(UriTemplate = "/find/{domainObject}?uris={uris}&order={order}")]
		[Description("Find domain objects by their URIs")]
		Stream FindQuery(string domainObject, string uris, string order);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/find/{domainObject}?order={order}")]
		[Description("Find domain objects by their URIs")]
		Stream FindFrom(string domainObject, string order, Stream body);

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
		[WebInvoke(Method = "POST", UriTemplate = "/exists/{domainObject}/{specification}")]
		[Description("Check if domain object with specification exists")]
		Stream ExistsWithSpecification(string domainObject, string specification, Stream body);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/exists/{domainObject}?specification={specification}")]
		[Description("Check if domain object with specification exists")]
		Stream ExistsWithSpecificationQuery(string domainObject, string specification, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/exists/{domainObject}?specification={specification}")]
		[Description("Check if domain object exists using optional specification from query parameters")]
		Stream ExistsQuery(string domainObject, string specification);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/exists-generic/{domainObject}")]
		[Description("Check if domain object exists using generic specification")]
		Stream ExistsWithGenericSpecification(string domainObject, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/exists-generic/{domainObject}")]
		[Description("Check if domain object exists using generic specification. Search arguments are in query string")]
		Stream ExistsWithGenericSpecificationQuery(string domainObject);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/exists-expression/{domainObject}")]
		[Description("Check if domain object exists using expression")]
		Stream ExistsWithExpression(string domainObject, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/check/{domainObject}?uri={uri}")]
		[Description("Check if domain object with specified URI exists")]
		Stream Check(string domainObject, string uri);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/submit/{domainEvent}?result={result}")]
		[Description("Submit domain event")]
		Stream SubmitEvent(string domainEvent, string result, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/queue/{domainEvent}")]
		[Description("Queue domain event")]
		Stream QueueEvent(string domainEvent, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/submit/{aggregate}/{domainEvent}/{*uri}")]
		[Description("Submit aggregate domain event")]
		Stream SubmitAggregateEvent(string aggregate, string domainEvent, string uri, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/submit/{aggregate}/{domainEvent}?uri={uri}")]
		[Description("Submit aggregate domain event")]
		Stream SubmitAggregateEventQuery(string aggregate, string domainEvent, string uri, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/queue/{aggregate}/{domainEvent}/{*uri}")]
		[Description("Queue aggregate domain event")]
		Stream QueueAggregateEvent(string aggregate, string domainEvent, string uri, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/queue/{aggregate}/{domainEvent}?uri={uri}")]
		[Description("Queue aggregate domain event")]
		Stream QueueAggregateEventQuery(string aggregate, string domainEvent, string uri, Stream body);
	}
}
