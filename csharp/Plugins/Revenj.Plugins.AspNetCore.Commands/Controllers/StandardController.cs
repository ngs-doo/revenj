using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Text;

namespace Revenj.Plugins.AspNetCore.Commands
{
	[Route("Commands.svc")]
	public class StandardController : ControllerBase
	{
		private readonly IServiceProvider Locator;
		private readonly CommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public StandardController(
			IServiceProvider locator,
			CommandConverter converter,
			IDomainModel domainModel,
			IWireSerialization serialization)
		{
			this.Locator = locator;
			this.Converter = converter;
			this.DomainModel = domainModel;
			this.Serialization = serialization;
		}

		enum MethodEnum { Insert, Update, Delete }

		private void Persist(MethodEnum method, Try<Type> maybeRoot, HttpContext context)
		{
			if (maybeRoot.IsFailure) return;
			var rootType = maybeRoot.Result;
			var array = Utility.ParseObject(Serialization, rootType.MakeArrayType(), context.Request.Body, false, Locator, context.Request, context.Response);
			if (array.IsFailure) return;
			var arg = (object[])array.Result;
			Converter.PassThrough<PersistAggregateRoot, PersistAggregateRoot.Argument<object>>(
				context,
				new PersistAggregateRoot.Argument<object>
				{
					RootName = rootType.FullName,
					ToInsert = method == MethodEnum.Insert ? arg : null,
					ToUpdate = method == MethodEnum.Update ? CreateKvMethod.MakeGenericMethod(rootType).Invoke(this, new[] { arg }) : null,
					ToDelete = method == MethodEnum.Delete ? arg : null
				});
		}

		private static MethodInfo CreateKvMethod = ((Func<object[], object>)CreateKeyValueArray<object>).Method.GetGenericMethodDefinition();

		private static KeyValuePair<T, T>[] CreateKeyValueArray<T>(object[] array)
		{
			return array != null ? array.Select(it => new KeyValuePair<T, T>(default(T), (T)it)).ToArray() : null;
		}

		private void Persist(MethodEnum method, string root, HttpContext context)
		{
			var type = Utility.CheckAggregateRoot(DomainModel, root, context.Response);
			Persist(method, type, context);
		}

		[HttpPost("persist/{root}")]
		public void Insert(string root)
		{
			Persist(MethodEnum.Insert, root, HttpContext);
		}

		[HttpPut("persist/{root}")]
		public void Update(string root)
		{
			Persist(MethodEnum.Update, root, HttpContext);
		}

		[HttpPut("olap/{cube}")]
		public void OlapCubeWithSpecification(
			string cube,
			[FromQuery(Name = "specification")] string specification,
			[FromQuery(Name = "dimensions")] string dimensions = null,
			[FromQuery(Name = "facts")] string facts = null,
			[FromQuery(Name = "order")] string order = null,
			[FromQuery(Name = "limit")] int? limit = null,
			[FromQuery(Name = "offset")] int? offset = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			if (string.IsNullOrEmpty(specification))
			{
				Utility.WriteError(response, "Specification must be specified", HttpStatusCode.BadRequest);
				return;
			}
			var cubeType = Utility.CheckDomainObject(DomainModel, cube, response);
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification, response);
			if (specType.IsFailure) return;
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
			{
				Utility.WriteError(response, "At least one dimension or fact must be specified", HttpStatusCode.BadRequest);
				return;
			}
			var ordDict = Utility.ParseOrder(order);

			var spec = Utility.ParseObject(Serialization, specType, request.Body, true, Locator, request, response);
			if (spec.IsFailure) return;

			Converter.PassThrough<AnalyzeOlapCube, AnalyzeOlapCube.Argument<object>>(
				HttpContext,
				new AnalyzeOlapCube.Argument<object>
				{
					CubeName = cubeType.Result.FullName,
					SpecificationName = specType.Result != null ? specType.Result.FullName : null,
					Specification = spec.Result,
					Dimensions = dimArr,
					Facts = factArr,
					Order = ordDict,
					Limit = limit,
					Offset = offset
				});
		}

		private void OlapCube(
			Try<Type> cubeType, 
			string dimensions, 
			string facts, 
			string order, 
			int? limit,
			int? offset,
			Try<object> spec, 
			HttpContext context)
		{
			if (spec.IsFailure) return;
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
			{
				Utility.WriteError(context.Response, "At least one dimension or fact must be specified", HttpStatusCode.BadRequest);
				return;
			}
			var ordDict = Utility.ParseOrder(order);

			Converter.PassThrough<AnalyzeOlapCube, AnalyzeOlapCube.Argument<object>>(
				HttpContext,
				new AnalyzeOlapCube.Argument<object>
				{
					CubeName = cubeType.Result.FullName,
					SpecificationName = null,
					Specification = spec.Result,
					Dimensions = dimArr,
					Facts = factArr,
					Order = ordDict,
					Limit = limit,
					Offset = offset
				});
		}

		[HttpGet("olap/{cube}")]
		public void OlapCubeWithOptionalSpecification(
			string cube,
			[FromQuery(Name = "specification")] string specification = null,
			[FromQuery(Name = "dimensions")] string dimensions = null,
			[FromQuery(Name = "facts")] string facts = null,
			[FromQuery(Name = "order")] string order = null,
			[FromQuery(Name = "limit")] int? limit = null,
			[FromQuery(Name = "offset")] int? offset = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var cubeType = Utility.CheckDomainObject(DomainModel, cube, response);
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification, response);
			var spec = Utility.ObjectFromQuery(specType, request, response);
			OlapCube(cubeType, dimensions, facts, order, limit, offset, spec, HttpContext);
		}

		[HttpPut("olap-generic/{cube}")]
		public void OlapCubeWithGenericSpecification(
			string cube,
			[FromQuery(Name = "dimensions")] string dimensions = null,
			[FromQuery(Name = "facts")] string facts = null,
			[FromQuery(Name = "order")] string order = null,
			[FromQuery(Name = "limit")] int? limit = null,
			[FromQuery(Name = "offset")] int? offset = null)
		{
			var cubeType = Utility.CheckDomainObject(DomainModel, cube, HttpContext.Response);
			var spec = Serialization.ParseGenericSpecification(cubeType, HttpContext);
			OlapCube(cubeType, dimensions, facts, order, limit, offset, spec, HttpContext);
		}

		[HttpPost("execute/{service}")]
		public void Execute(string service)
		{
			var request = HttpContext.Request;
			switch (Utility.GetIncomingFormat(request))
			{
				//TODO: maybe it's ok to use stream reader now!?
				case MessageFormat.Json:
					Execute(HttpContext, service, new StreamReader(request.Body, Encoding.UTF8).ReadToEnd());
					break;
				case MessageFormat.ProtoBuf:
					Execute<Stream>(HttpContext, service, request.Body);
					break;
				default:
					var xml = Utility.ParseXml(request.Body, HttpContext.Response);
					if (xml.IsFailure) return;
					Execute(HttpContext, service, xml.Result);
					break;
			}
		}

		private void Execute<TFormat>(HttpContext context, string service, TFormat data)
		{
			Converter.ConvertStream<ExecuteService, ExecuteService.Argument<TFormat>>(
				context,
				new ExecuteService.Argument<TFormat>
				{
					Name = service,
					Data = data
				});
		}
	}
}
