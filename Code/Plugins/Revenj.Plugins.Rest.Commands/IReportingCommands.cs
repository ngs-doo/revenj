using System.ComponentModel;
using System.IO;
using System.ServiceModel;
using System.ServiceModel.Web;

namespace Revenj.Plugins.Rest.Commands
{
	[ServiceContract(Namespace = "https://dsl-platform.com")]
	public interface IReportingCommands
	{
		[OperationContract]
		[WebGet(UriTemplate = "/report/{report}")]
		[Description("Populate predefined report")]
		Stream PopulateReportQuery(string report);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/report/{report}")]
		[Description("Populate predefined report")]
		Stream PopulateReport(string report, Stream body);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/report/{report}/{templater}")]
		[Description("Create predefined report")]
		Stream CreateReport(string report, string templater, Stream body);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/olap/{cube}/{templater}/{specification}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Create simple olap report with specification")]
		Stream OlapCubeWithSpecification(string cube, string templater, string specification, string dimensions, string facts, string order, string limit, string offset, Stream body);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/olap/{cube}/{templater}?specification={specification}&dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Create simple olap report with specification")]
		Stream OlapCubeWithSpecificationQuery(string cube, string templater, string specification, string dimensions, string facts, string order, string limit, string offset, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/olap/{cube}/{templater}?specification={specification}&dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Create simple olap report with optional specification from query parameters")]
		Stream OlapCubeQuery(string cube, string templater, string specification, string dimensions, string facts, string order, string limit, string offset);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/olap-generic/{cube}/{templater}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Create simple olap report with generic specification")]
		Stream OlapCubeWithGenericSpecification(string cube, string templater, string dimensions, string facts, string order, string limit, string offset, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/olap-generic/{cube}/{templater}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Create simple olap report with generic specification. Search arguments are in query string")]
		Stream OlapCubeWithGenericSpecificationQuery(string cube, string templater, string dimensions, string facts, string order, string limit, string offset);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/olap-expression/{cube}/{templater}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Create simple olap report with using expression")]
		Stream OlapCubeWithExpression(string cube, string templater, string dimensions, string facts, string order, string limit, string offset, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/history/{root}/{*uris}")]
		[Description("Get aggregate root's history")]
		Stream GetHistory(string root, string uris);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/history/{root}")]
		[Description("Get aggregate root's history")]
		Stream GetHistoryFrom(string root, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/templater/{file}/{domainObject}/{*uri}")]
		[Description("Run Templater with specified file for domain object with provided uri")]
		Stream FindTemplater(string file, string domainObject, string uri);

		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/templater/{file}/{domainObject}/{specification}")]
		[Description("Run Templater with specified file for domain object using provided specification")]
		Stream SearchTemplaterWithSpecification(string file, string domainObject, string specification, Stream body);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/templater/{file}/{domainObject}?specification={specification}")]
		[Description("Run Templater with specified file for domain object using provided specification")]
		Stream SearchTemplaterWithSpecificationQuery(string file, string domainObject, string specification, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/templater/{file}/{domainObject}?specification={specification}")]
		[Description("Run Templater with specified file for domain object using provided specification arguments in query")]
		Stream SearchTemplaterQuery(string file, string domainObject, string specification);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/templater-generic/{file}/{domainObject}")]
		[Description("Run Templater with specified file for domain object using generic specification")]
		Stream SearchTemplaterWithGenericSpecification(string file, string domainObject, Stream body);

		[OperationContract]
		[WebGet(UriTemplate = "/templater-generic/{file}/{domainObject}")]
		[Description("Run Templater with specified file for domain object using generic specification built from arguments in query")]
		Stream SearchTemplaterWithGenericSpecificationQuery(string file, string domainObject);

		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/templater-expression/{file}/{domainObject}")]
		[Description("Run Templater with specified file for domain object using provided expression")]
		Stream SearchTemplaterWithExpression(string file, string domainObject, Stream body);
	}
}
