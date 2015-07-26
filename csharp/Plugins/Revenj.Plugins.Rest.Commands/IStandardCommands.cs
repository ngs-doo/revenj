using System.ComponentModel;
using System.IO;
using System.ServiceModel;
using System.ServiceModel.Web;

namespace Revenj.Plugins.Rest.Commands
{
	/// <summary>
	/// API for various server commands: bulk insert/update,
	/// access to olap cubes, RPC call to server service.
	/// Cotent-type and Accept are used for protocol definition.
	/// </summary>
	[ServiceContract(Namespace = "https://dsl-platform.com")]
	public interface IStandardCommands
	{
		/// <summary>
		/// Bulk insert command. For inserting multiple aggregate roots at once.
		/// URI collection for created aggregates is returned.
		/// </summary>
		/// <param name="root">aggregate root name</param>
		/// <param name="body">serialized aggregates</param>
		/// <returns>new URIs</returns>
		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/persist/{root}")]
		[Description("Insert aggregate root data. Provide data array")]
		Stream Insert(string root, Stream body);
		/// <summary>
		/// Bulk update command. For updating multiple aggregate roots at once.
		/// Old aggregate version is looked up from server.
		/// </summary>
		/// <param name="root">aggregate root name</param>
		/// <param name="body">serialized aggregates</param>
		/// <returns>nothing</returns>
		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/persist/{root}")]
		[Description("Update aggregate root data. Provide data array")]
		Stream Update(string root, Stream body);
		/// <summary>
		/// Analyze olap cube using provided specification with defined dimensions and facts.
		/// Result can be ordered and limited. If required offset can be used.
		/// Referenced specification from cube or underlying data source will be used.
		/// Returns data analysis collection with specified facts + dimensions.
		/// </summary>
		/// <param name="cube">olap cube name</param>
		/// <param name="specification">filter data source with predicate</param>
		/// <param name="dimensions">group result by dimensions</param>
		/// <param name="facts">aggregate result by facts</param>
		/// <param name="order">order result</param>
		/// <param name="limit">limit total number of items</param>
		/// <param name="offset">skip initial number of items</param>
		/// <param name="body">serialized specification</param>
		/// <returns>analysis result as collection of dimensions + facts</returns>
		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/olap/{cube}/{specification}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Get simple olap report with specification")]
		Stream OlapCubeWithSpecification(string cube, string specification, string dimensions, string facts, string order, string limit, string offset, Stream body);
		/// <summary>
		/// Analyze olap cube using provided specification with defined dimensions and facts.
		/// Result can be ordered and limited. If required offset can be used.
		/// Referenced specification from cube or underlying data source will be used.
		/// Returns data analysis collection with specified facts + dimensions.
		/// </summary>
		/// <param name="cube">olap cube name</param>
		/// <param name="specification">filter data source with predicate</param>
		/// <param name="dimensions">group result by dimensions</param>
		/// <param name="facts">aggregate result by facts</param>
		/// <param name="order">order result</param>
		/// <param name="limit">limit total number of items</param>
		/// <param name="offset">skip initial number of items</param>
		/// <param name="body">serialized specification</param>
		/// <returns>analysis result as collection of dimensions + facts</returns>
		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/olap/{cube}?specification={specification}&dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Get simple olap report")]
		Stream OlapCubeWithSpecificationQuery(string cube, string specification, string dimensions, string facts, string order, string limit, string offset, Stream body);
		/// <summary>
		/// Analyze olap cube using defined dimensions and facts.
		/// Result can be ordered and limited. If required offset can be used.
		/// Optional specification from cube or underlying data source can be used.
		/// Specification arguments are sent as query parameters.
		/// Returns data analysis collection with specified facts + dimensions.
		/// </summary>
		/// <param name="cube">olap cube name</param>
		/// <param name="specification">filter data source with predicate</param>
		/// <param name="dimensions">group result by dimensions</param>
		/// <param name="facts">aggregate result by facts</param>
		/// <param name="order">order result</param>
		/// <param name="limit">limit total number of items</param>
		/// <param name="offset">skip initial number of items</param>
		/// <returns>analysis result as collection of dimensions + facts</returns>
		[OperationContract]
		[WebGet(UriTemplate = "/olap/{cube}?specification={specification}&dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Get simple olap report with optional specification from query parameters")]
		Stream OlapCubeQuery(string cube, string specification, string dimensions, string facts, string order, string limit, string offset);
		/// <summary>
		/// Analyze olap cube using generic specification with defined dimensions and facts.
		/// Result can be ordered and limited. If required offset can be used.
		/// Specification will be dynamically reconstructed from body as Content-type and used for filtering.
		/// Returns data analysis collection with specified facts + dimensions.
		/// </summary>
		/// <param name="cube">olap cube name</param>
		/// <param name="dimensions">group result by dimensions</param>
		/// <param name="facts">aggregate result by facts</param>
		/// <param name="order">order result</param>
		/// <param name="limit">limit total number of items</param>
		/// <param name="offset">skip initial number of items</param>
		/// <param name="body">serialized specification</param>
		/// <returns>analysis result as collection of dimensions + facts</returns>
		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/olap-generic/{cube}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Get simple olap report with generic specification")]
		Stream OlapCubeWithGenericSpecification(string cube, string dimensions, string facts, string order, string limit, string offset, Stream body);
		/// <summary>
		/// Analyze olap cube using generic specification with defined dimensions and facts.
		/// Result can be ordered and limited. If required offset can be used.
		/// Specification will be dynamically reconstructed from query parameters and used for filtering.
		/// Returns data analysis collection with specified facts + dimensions.
		/// </summary>
		/// <param name="cube">olap cube name</param>
		/// <param name="dimensions">group result by dimensions</param>
		/// <param name="facts">aggregate result by facts</param>
		/// <param name="order">order result</param>
		/// <param name="limit">limit total number of items</param>
		/// <param name="offset">skip initial number of items</param>
		/// <returns>analysis result as collection of dimensions + facts</returns>
		[OperationContract]
		[WebGet(UriTemplate = "/olap-generic/{cube}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Get simple olap report with generic specification. Search arguments are in query string")]
		Stream OlapCubeWithGenericSpecificationQuery(string cube, string dimensions, string facts, string order, string limit, string offset);
		/// <summary>
		/// Analyze olap cube using provided lambda expresion specification with defined dimensions and facts.
		/// Result can be ordered and limited. If required offset can be used.
		/// Specification will be dynamically reconstructed from body as Content-type and used for filtering.
		/// Returns data analysis collection with specified facts + dimensions.
		/// </summary>
		/// <param name="cube">olap cube name</param>
		/// <param name="dimensions">group result by dimensions</param>
		/// <param name="facts">aggregate result by facts</param>
		/// <param name="order">order result</param>
		/// <param name="limit">limit total number of items</param>
		/// <param name="offset">skip initial number of items</param>
		/// <param name="body">serialized specification</param>
		/// <returns>analysis result as collection of dimensions + facts</returns>
		[OperationContract]
		[WebInvoke(Method = "PUT", UriTemplate = "/olap-expression/{cube}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}")]
		[Description("Get simple olap report with using expression")]
		Stream OlapCubeWithExpression(string cube, string dimensions, string facts, string order, string limit, string offset, Stream body);
		/// <summary>
		/// Execute remote service. Service must implement IServerService&lt;TInput, TOutput&gt;.
		/// Input argument is deserialized using defined Content-type header.
		/// Result is serialized using defined Accept header.
		/// Full service name must be provided.
		/// </summary>
		/// <param name="service">service name</param>
		/// <param name="body">serialized input argument</param>
		/// <returns>serialized result</returns>
		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/execute/{service}")]
		[Description("Execute service")]
		Stream Execute(string service, Stream body);
	}
}
