using Microsoft.AspNetCore.Http;
using System.Linq;
using Revenj.AspNetCore;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;
using System;
using System.Collections.Generic;
using System.Net;
using System.Reflection;
using System.Threading.Tasks;
using System.Text;
using System.IO;

namespace Revenj.Plugins.AspNetCore.Commands
{
	public class CommandsMiddleware
	{
		private readonly IServiceProvider Locator;
		private readonly CommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public CommandsMiddleware(
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

		public Task Handle(HttpContext context, int prefixLength)
		{
			var path = context.Request.Path.Value;
			if (path.Length == prefixLength)
				return context.Response.WriteError("Path not specified", HttpStatusCode.BadRequest);
			var name = path.Substring(prefixLength + 1);
			switch (context.Request.Method)
			{
				case "POST":
					if (name.StartsWith("persist/", StringComparison.OrdinalIgnoreCase))
						return Persist(MethodEnum.Insert, name.Substring(8), context);
					if (name.StartsWith("execute/", StringComparison.OrdinalIgnoreCase))
						return Execute(name.Substring(8), context);

					return Utility.WriteError(context.Response, "Unknown route", HttpStatusCode.NotFound);
				case "GET":
					if (name.StartsWith("olap/", StringComparison.OrdinalIgnoreCase))
						return OlapCubeFromGet(name.Substring(5), context);

					return Utility.WriteError(context.Response, "Unknown route", HttpStatusCode.NotFound);
				case "PUT":
					if (name.StartsWith("persist/", StringComparison.OrdinalIgnoreCase))
						return Persist(MethodEnum.Update, name.Substring(8), context);
					if (name.StartsWith("olap/", StringComparison.OrdinalIgnoreCase))
						return OlapCubeWithBody(name.Substring(5), context);

					return Utility.WriteError(context.Response, "Unknown route", HttpStatusCode.NotFound);
				default:
					return Utility.WriteError(context.Response, "Unsuported method type", HttpStatusCode.MethodNotAllowed);
			}
		}

		enum MethodEnum { Insert, Update, Delete }

		private Task Persist(MethodEnum method, Try<Type> maybeRoot, HttpContext context)
		{
			if (maybeRoot.IsFailure) return Task.CompletedTask;
			var rootType = maybeRoot.Result;
			var array = Utility.ParseObject(Serialization, rootType.MakeArrayType(), context.Request.Body, false, Locator, context);
			if (array.IsFailure) return Task.CompletedTask;
			var arg = (object[])array.Result;
			return Converter.PassThrough<PersistAggregateRoot, PersistAggregateRoot.Argument<object>>(
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

		private Task Persist(MethodEnum method, string root, HttpContext context)
		{
			var type = Utility.CheckAggregateRoot(DomainModel, root, context.Response);
			return Persist(method, type, context);
		}

		public Task Insert(string root, HttpContext context)
		{
			return Persist(MethodEnum.Insert, root, context);
		}

		public Task Update(string root, HttpContext context)
		{
			return Persist(MethodEnum.Update, root, context);
		}

		public Task OlapCubeWithBody(string cube, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var dimensions = request.Query.ContainsKey("dimensions") ? request.Query["dimensions"][0] : null;
			var facts = request.Query.ContainsKey("facts") ? request.Query["facts"][0] : null;
			var order = request.Query.ContainsKey("order") ? request.Query["order"][0] : null;
			int? limit, offset;
			Utility.ParseLimitOffset(request.Query, out limit, out offset);
			var cubeType = Utility.CheckDomainObject(DomainModel, cube, response);
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification, response);
			if (specType.IsFailure) return Task.CompletedTask;
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
				return Utility.WriteError(response, "At least one dimension or fact must be specified", HttpStatusCode.BadRequest);
			var ordDict = Utility.ParseOrder(order);

			var spec = Utility.ParseObject(Serialization, specType, request.Body, true, Locator, context);
			if (spec.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<AnalyzeOlapCube, AnalyzeOlapCube.Argument<object>>(
				context,
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

		private Task OlapCube(Try<Type> cubeType, Try<object> spec, HttpContext context)
		{
			if (spec.IsFailure) return Task.CompletedTask;
			var query = context.Request.Query;
			var dimensions = query.ContainsKey("dimensions") ? query["dimensions"][0] : null;
			var facts = query.ContainsKey("facts") ? query["facts"][0] : null;
			var order = query.ContainsKey("order") ? query["order"][0] : null;
			int? limit, offset;
			Utility.ParseLimitOffset(query, out limit, out offset);
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
				return Utility.WriteError(context.Response, "At least one dimension or fact must be specified", HttpStatusCode.BadRequest);
			var ordDict = Utility.ParseOrder(order);

			return Converter.PassThrough<AnalyzeOlapCube, AnalyzeOlapCube.Argument<object>>(
				context,
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

		public Task OlapCubeFromGet(string cube, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var cubeType = Utility.CheckDomainObject(DomainModel, cube, response);
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification, response);
			var spec = Utility.ObjectFromQuery(specType, context);
			return OlapCube(cubeType, spec, context);
		}

		public Task Execute(string service, HttpContext context)
		{
			var request = context.Request;
			switch (Utility.GetIncomingFormat(request))
			{
				//TODO: maybe it's ok to use stream reader now!?
				case MessageFormat.Json:
					return Execute(context, service, new StreamReader(request.Body, Encoding.UTF8).ReadToEnd());
				case MessageFormat.ProtoBuf:
					return Execute<Stream>(context, service, request.Body);
				default:
					var xml = Utility.ParseXml(request.Body, context.Response);
					if (xml.IsFailure) return Task.CompletedTask;
					return Execute(context, service, xml.Result);
			}
		}

		private Task Execute<TFormat>(HttpContext context, string service, TFormat data)
		{
			return Converter.ConvertStream<ExecuteService, ExecuteService.Argument<TFormat>>(
				context,
				new ExecuteService.Argument<TFormat>
				{
					Name = service,
					Data = data
				});
		}
	}
}
