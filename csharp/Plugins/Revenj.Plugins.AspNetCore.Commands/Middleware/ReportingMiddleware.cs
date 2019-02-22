using Microsoft.AspNetCore.Http;
using Revenj.AspNetCore;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;
using System;
using System.IO;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace Revenj.Plugins.AspNetCore.Commands
{
	public class ReportingMiddleware
	{
		private static readonly AdditionalCommand[] EmptyCommands = new AdditionalCommand[0];

		private readonly IServiceProvider Locator;
		private readonly CommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public ReportingMiddleware(
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
				case "PUT":
					if (name.StartsWith("report/", StringComparison.OrdinalIgnoreCase))
					{
						var ind = name.IndexOf('/', 7);
						if (ind == -1)
							return PopulateReport(name.Substring(7), context);
						if (ind != name.Length)
							return CreateReport(name.Substring(7, ind - 7), name.Substring(ind + 1), context);
					}
					if (name.StartsWith("olap/", StringComparison.OrdinalIgnoreCase))
					{
						var ind = name.IndexOf('/', 5);
						if (ind != -1 && ind != name.Length)
							return OlapCubeFromBody(name.Substring(5, ind - 5), name.Substring(ind + 1), context);
					}
					if (name.StartsWith("history/", StringComparison.OrdinalIgnoreCase))
						return GetHistory(name.Substring(8), context);
					if (name.StartsWith("templater/", StringComparison.OrdinalIgnoreCase))
					{
						var ind1 = name.IndexOf('/', 10);
						if (ind1 != -1 && ind1 != name.Length)
						{
							var ind2 = name.IndexOf('/', ind1 + 1);
							if (ind2 != -1 && ind2 != name.Length)
							{
								var file = name.Substring(10, ind1 - 10);
								var domainObject = name.Substring(ind1 + 1, ind2 - ind1 - 1);
								var specification = name.Substring(ind2 + 1);
								return SearchTemplaterWithSpecification(file, domainObject, specification, context);
							}
						}
					}
					if (name.StartsWith("templater-generic/", StringComparison.OrdinalIgnoreCase))
					{
						var ind = name.IndexOf('/', 19);
						if (ind != -1 && ind != name.Length)
							return SearchTemplaterWithGenericSpecification(name.Substring(18, ind - 18), name.Substring(ind + 1), context);
					}
					if (name.StartsWith("query/", StringComparison.OrdinalIgnoreCase))
						return EvaluateQuery(name.Substring(6), context);

					return Utility.WriteError(context.Response, "Unknown route", HttpStatusCode.NotFound);
				case "GET":
					if (name.StartsWith("olap/", StringComparison.OrdinalIgnoreCase))
					{
						var ind = name.IndexOf('/', 5);
						if (ind != -1)
							return OlapCubeGet(name.Substring(5, ind - 5), name.Substring(ind + 1), context);
					}
					if (name.StartsWith("templater/", StringComparison.OrdinalIgnoreCase))
					{
						var ind1 = name.IndexOf('/', 10);
						if (ind1 != -1 && ind1 != name.Length)
						{
							var ind2 = name.IndexOf('/', ind1 + 1);
							if (ind2 != -1 && ind2 != name.Length)
							{
								var file = name.Substring(10, ind1 - 10);
								var domainObject = name.Substring(ind1 + 1, ind2 - ind1 - 1);
								var uri = name.Substring(ind2 + 1);
								return FindTemplater(file, domainObject, uri, context);
							}
							else if (ind2 == -1)
							{
								var file = name.Substring(10, ind1 - 10);
								var domainObject = name.Substring(ind1 + 1);
								return SearchTemplater(file, domainObject, context);
							}
						}
					}
					if (name.StartsWith("query/", StringComparison.OrdinalIgnoreCase))
						return EvaluateQueryWithoutArguments(name.Substring(6), context);

					return Utility.WriteError(context.Response, "Unknown route", HttpStatusCode.NotFound);
				default:
					return Utility.WriteError(context.Response, "Unsuported method type", HttpStatusCode.MethodNotAllowed);
			}
		}

		public Task PopulateReport(string report, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var reportType = Utility.CheckDomainObject(DomainModel, report, response);
			var validatedData = Utility.ParseObject(Serialization, reportType, request.Body, true, Locator, context);
			if (validatedData.IsFailure) return Task.CompletedTask;
			return Converter.PassThrough<PopulateReport, PopulateReport.Argument<object>>(
				context,
				new PopulateReport.Argument<object>
				{
					ReportName = reportType.Result.FullName,
					Data = validatedData.Result
				});
		}

		public Task CreateReport(string report, string templater, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var reportType = Utility.CheckDomainObject(DomainModel, report, response);
			var templaterType = Utility.CheckDomainObject(DomainModel, reportType, templater, response);
			if (templaterType.IsFailure) return Task.CompletedTask;
			var validatedData = Utility.ParseObject(Serialization, reportType, request.Body, true, Locator, context);
			if (validatedData.IsFailure) return Task.CompletedTask;
			return Converter.PassThrough<CreateReport, CreateReport.Argument<object>>(
				context,
				new CreateReport.Argument<object>
				{
					ReportName = reportType.Result.FullName,
					TemplaterName = templater,
					Data = validatedData.Result
				});
		}

		public Task OlapCubeFromBody(string cube, string templater, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var cubeType = Utility.CheckDomainObject(DomainModel, cube, response);
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification, response);
			if (specType.IsFailure) return Task.CompletedTask;
			var templaterType = Utility.CheckDomainObject(DomainModel, cubeType, templater, response);
			if (templaterType.IsFailure) return Task.CompletedTask;
			var dimensions = request.Query.ContainsKey("dimensions") ? request.Query["dimensions"][0] : null;
			var facts = request.Query.ContainsKey("facts") ? request.Query["facts"][0] : null;
			var order = request.Query.ContainsKey("order") ? request.Query["order"][0] : null;
			int? limit, offset;
			Utility.ParseLimitOffset(request.Query, out limit, out offset);
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
				return Utility.WriteError(response, "At least one dimension or fact must be specified", HttpStatusCode.BadRequest);
			var ordDict = Utility.ParseOrder(order);

			var spec = Utility.ParseObject(Serialization, specType, request.Body, true, Locator, context);
			if (spec.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<OlapCubeReport, OlapCubeReport.Argument<object>>(
				context,
				new OlapCubeReport.Argument<object>
				{
					CubeName = cubeType.Result.FullName,
					TemplaterName = templater,
					SpecificationName = specType.Result != null ? specType.Result.FullName : null,
					Specification = spec.Result,
					Dimensions = dimArr,
					Facts = factArr,
					Order = ordDict,
					Limit = limit,
					Offset = offset
				});
		}

		private Task OlapCube(Type cubeType, Type templaterType, Try<object> spec, HttpContext context)
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

			return Converter.PassThrough<OlapCubeReport, OlapCubeReport.Argument<object>>(
				context,
				new OlapCubeReport.Argument<object>
				{
					CubeName = cubeType.FullName,
					TemplaterName = templaterType.Name,
					SpecificationName = null,
					Specification = spec.Result,
					Dimensions = dimArr,
					Facts = factArr,
					Order = ordDict,
					Limit = limit,
					Offset = offset
				});
		}

		public Task OlapCubeGet(string cube, string templater, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var cubeType = Utility.CheckDomainObject(DomainModel, cube, response);
			if (cubeType.IsFailure) return Task.CompletedTask;
			var templaterType = Utility.CheckDomainObject(DomainModel, cubeType, templater, response);
			if (templaterType.IsFailure) return Task.CompletedTask;
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification, response);
			var spec = Utility.ObjectFromQuery(specType, context);
			return OlapCube(cubeType.Result, templaterType.Result, spec, context);
		}

		public Task GetHistory(string root, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var type = Utility.CheckAggregateRoot(DomainModel, root, response);
			if (type.IsFailure) return Task.CompletedTask;
			var uris = Serialization.TryDeserialize<string[]>(request, response);
			if (uris.IsFailure) return Task.CompletedTask;
			return Converter.PassThrough<GetRootHistory, GetRootHistory.Argument>(
				context,
				new GetRootHistory.Argument
				{
					Name = root,
					Uri = uris.Result
				});
		}

		//TODO: should use uri of query parameter
		public Task FindTemplater(string file, string domainObject, string uri, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var type = Utility.CheckIdentifiable(DomainModel, domainObject, response);
			if (type.IsFailure) return Task.CompletedTask;
			var accept = CommandConverter.Accept(request.Headers);
			var requestPdf = accept == "application/pdf";
			return Converter.PassThrough<TemplaterProcessDocument, TemplaterProcessDocument.Argument<object>>(
				new TemplaterProcessDocument.Argument<object>
				{
					File = Path.GetFileName(file),
					GetSources = new[] { new GetDomainObject.Argument { Name = domainObject, Uri = new[] { uri } } },
					ToPdf = requestPdf
				},
				"application/octet-stream",
				context,
				EmptyCommands,
				requestPdf ? "application/pdf" : null);
		}

		private Task SearchTemplater(
			string file,
			string domainObject,
			Try<object> spec,
			HttpContext context)
		{
			if (spec.IsFailure) return Task.CompletedTask;
			var accept = CommandConverter.Accept(context.Request.Headers);
			var requestPdf = accept == "application/pdf";
			return Converter.PassThrough<TemplaterProcessDocument, TemplaterProcessDocument.Argument<object>>(
				new TemplaterProcessDocument.Argument<object>
				{
					File = Path.GetFileName(file),
					SearchSources =
						new[] {
							new SearchDomainObject.Argument<object>
							{
								SpecificationName = null,
								Specification = spec.Result,
								Name = domainObject
							}},
					ToPdf = requestPdf
				},
				"application/octet-stream",
				context,
				EmptyCommands,
				requestPdf ? "application/pdf" : null);
		}

		public Task SearchTemplaterWithSpecification(string file, string domainObject, string specification, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var type = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, type, specification, response);
			if (specType.IsFailure) return Task.CompletedTask;
			if (specType.Result == null)
				return Utility.WriteError(response, "Specification must be specified", HttpStatusCode.BadRequest);
			var spec = Utility.ParseObject(Serialization, specType, request.Body, false, Locator, context);
			return SearchTemplater(file, domainObject, spec, context);
		}

		public Task SearchTemplater(string file, string domainObject, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var type = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, type, specification, response);
			var spec = Utility.ObjectFromQuery(specType, context);
			if (spec.IsFailure) return Task.CompletedTask;
			return SearchTemplater(file, domainObject, spec, context);
		}

		public Task SearchTemplaterWithGenericSpecification(string file, string domainObject, HttpContext context)
		{
			var type = Utility.CheckDomainObject(DomainModel, domainObject, context.Response);
			var spec = Serialization.ParseGenericSpecification(type, context);
			return SearchTemplater(file, domainObject, spec, context);
		}

		public Task EvaluateQueryWithoutArguments(string query, HttpContext context)
		{
			var type = Utility.CheckDomainObject(DomainModel, query, context.Response);
			if (type.IsFailure) return Task.CompletedTask;
			return Converter.PassThrough<EvaluateQuery, EvaluateQuery.Argument<object>>(
				context,
				new EvaluateQuery.Argument<object> { QueryName = type.Result.FullName });
		}

		private Task Evaluate<TFormat>(HttpContext context, Type query, TFormat data)
		{
			return Converter.ConvertStream<EvaluateQuery, EvaluateQuery.Argument<TFormat>>(
				context,
				new EvaluateQuery.Argument<TFormat>
				{
					QueryName = query.FullName,
					Data = data
				});
		}

		public Task EvaluateQuery(string query, HttpContext context)
		{
			var type = Utility.CheckDomainObject(DomainModel, query, context.Response);
			if (type.IsFailure) return Task.CompletedTask;
			var request = context.Request;
			switch (Utility.GetIncomingFormat(request))
			{
				//TODO: maybe it's ok to use stream reader now!?
				case MessageFormat.Json:
					return Evaluate(context, type.Result, new StreamReader(request.Body, Encoding.UTF8).ReadToEnd());
				case MessageFormat.ProtoBuf:
					return Evaluate<Stream>(context, type.Result, request.Body);
				default:
					var xml = Utility.ParseXml(request.Body, context.Response);
					if (xml.IsFailure) return Task.CompletedTask;
					return Evaluate(context, type.Result, xml.Result);
			}
		}
	}
}
