using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;
using System;
using System.IO;
using System.Net;
using System.Text;

namespace Revenj.Plugins.AspNetCore.Commands
{
	[Route("Reporting.svc")]
	public class ReportingController : ControllerBase
	{
		private static readonly AdditionalCommand[] NoCommands = new AdditionalCommand[0];

		private readonly IServiceProvider Locator;
		private readonly CommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public ReportingController(
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

		[HttpPut("report/{report}")]
		public void PopulateReport(string report)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var reportType = Utility.CheckDomainObject(DomainModel, report, response);
			var validatedData = Utility.ParseObject(Serialization, reportType, request.Body, true, Locator, request, response);
			if (validatedData.IsFailure) return;
			Converter.PassThrough<PopulateReport, PopulateReport.Argument<object>>(
				HttpContext,
				new PopulateReport.Argument<object>
				{
					ReportName = reportType.Result.FullName,
					Data = validatedData.Result
				});
		}

		[HttpPut("report/{report}/{templater}")]
		public void CreateReport(string report, string templater)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var reportType = Utility.CheckDomainObject(DomainModel, report, response);
			var templaterType = Utility.CheckDomainObject(DomainModel, reportType, templater, response);
			if (templaterType.IsFailure) return;
			var validatedData = Utility.ParseObject(Serialization, reportType, request.Body, true, Locator, request, response);
			if (validatedData.IsFailure) return;
			Converter.PassThrough<CreateReport, CreateReport.Argument<object>>(
				HttpContext,
				new CreateReport.Argument<object>
				{
					ReportName = reportType.Result.FullName,
					TemplaterName = templater,
					Data = validatedData.Result
				});
		}

		[HttpPut("olap/{cube}/{templater}")]
		public void OlapCubeWithSpecification(
			string cube,
			string templater,
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
			var templaterType = Utility.CheckDomainObject(DomainModel, cubeType, templater, response);
			if (templaterType.IsFailure) return;
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

			Converter.PassThrough<OlapCubeReport, OlapCubeReport.Argument<object>>(
				HttpContext,
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

		private void OlapCube(
			Type cubeType,
			Type templaterType,
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

			Converter.PassThrough<OlapCubeReport, OlapCubeReport.Argument<object>>(
				context,
				new OlapCubeReport.Argument<object>
				{
					CubeName = cubeType.FullName,
					TemplaterName = templaterType.FullName,
					SpecificationName = null,
					Specification = spec.Result,
					Dimensions = dimArr,
					Facts = factArr,
					Order = ordDict,
					Limit = limit,
					Offset = offset
				});
		}

		[HttpGet("olap/{cube}/{templater}")]
		public void OlapCubeWithOptionalSpecification(
			string cube,
			string templater,
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
			if (cubeType.IsFailure) return;
			var templaterType = Utility.CheckDomainObject(DomainModel, cubeType, templater, response);
			if (templaterType.IsFailure) return;
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification, response);
			var spec = Utility.ObjectFromQuery(specType, request, response);
			OlapCube(cubeType.Result, templaterType.Result, dimensions, facts, order, limit, offset, spec, HttpContext);
		}

		[HttpPut("olap-generic/{cube}/{templater}")]
		public void OlapCubeWithGenericSpecification(
			string cube,
			string templater,
			[FromQuery(Name = "dimensions")] string dimensions = null,
			[FromQuery(Name = "facts")] string facts = null,
			[FromQuery(Name = "order")] string order = null,
			[FromQuery(Name = "limit")] int? limit = null,
			[FromQuery(Name = "offset")] int? offset = null)
		{
			var response = HttpContext.Response;
			var cubeType = Utility.CheckDomainObject(DomainModel, cube, response);
			if (cubeType.IsFailure) return;
			var templaterType = Utility.CheckDomainObject(DomainModel, cubeType, templater, response);
			if (templaterType.IsFailure) return;
			var spec = Utility.ParseGenericSpecification(Serialization, cubeType, HttpContext);
			OlapCube(cubeType.Result, templaterType.Result, dimensions, facts, order, limit, offset, spec, HttpContext);
		}

		[HttpPut("history/{root}")]
		public void GetHistory(string root)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var type = Utility.CheckAggregateRoot(DomainModel, root, response);
			if (type.IsFailure) return;
			var uris = Serialization.TryDeserialize<string[]>(request, response);
			if (uris.IsFailure) return;
			Converter.PassThrough<GetRootHistory, GetRootHistory.Argument>(
				HttpContext,
				new GetRootHistory.Argument
				{
					Name = root,
					Uri = uris.Result
				});
		}

		[HttpGet("templater/{file}/{domainObject}/{uri}")]
		public void FindTemplater(string file, string domainObject, string uri)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var type = Utility.CheckIdentifiable(DomainModel, domainObject, response);
			if (type.IsFailure) return;
			var accept = CommandConverter.Accept(request.Headers);
			var requestPdf = accept == "application/pdf";
			Converter.PassThrough<TemplaterProcessDocument, TemplaterProcessDocument.Argument<object>>(
				new TemplaterProcessDocument.Argument<object>
				{
					File = Path.GetFileName(file),
					GetSources = new[] { new GetDomainObject.Argument { Name = domainObject, Uri = new[] { uri } } },
					ToPdf = requestPdf
				},
				"application/octet-stream",
				HttpContext,
				NoCommands);
			response.ContentType = requestPdf ? "application/pdf" : "application/octet-stream";
		}

		private void SearchTemplater(
			string file,
			string domainObject,
			Try<object> spec,
			HttpContext context)
		{
			if (spec.IsFailure) return;
			var accept = CommandConverter.Accept(HttpContext.Request.Headers);
			var requestPdf = accept == "application/pdf";
			Converter.PassThrough<TemplaterProcessDocument, TemplaterProcessDocument.Argument<object>>(
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
				HttpContext,
				NoCommands);
			HttpContext.Response.ContentType = requestPdf ? "application/pdf" : "application/octet-stream";
		}

		[HttpPut("templater/{file}/{domainObject}/{specification}")]
		public void SearchTemplaterWithSpecification(string file, string domainObject, string specification)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var type = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, type, specification, response);
			if (specType.IsFailure) return;
			if (specType.Result == null)
			{
				Utility.WriteError(response, "Specification must be specified", HttpStatusCode.BadRequest);
				return;
			}
			var spec = Utility.ParseObject(Serialization, specType, request.Body, false, Locator, request, response);
			SearchTemplater(file, domainObject, spec, HttpContext);
		}

		[HttpGet("templater/{file}/{domainObject}")]
		public void SearchTemplater(
			string file, 
			string domainObject,
			[FromQuery(Name = "specification")] string specification = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var type = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, type, specification, response);
			var spec = Utility.ObjectFromQuery(specType, request, response);
			if (spec.IsFailure) return;
			SearchTemplater(file, domainObject, spec, HttpContext);
		}

		[HttpPut("templater-generic/{file}/{domainObject}")]
		public void SearchTemplaterWithGenericSpecification(string file, string domainObject)
		{
			var type = Utility.CheckDomainObject(DomainModel, domainObject, HttpContext.Response);
			var spec = Serialization.ParseGenericSpecification(type, HttpContext);
			SearchTemplater(file, domainObject, spec, HttpContext);
		}

		[HttpGet("query/{query}")]
		public void EvaluateQueryWithoutArguments(string query)
		{
			var type = Utility.CheckDomainObject(DomainModel, query, HttpContext.Response);
			if (type.IsFailure) return;
			Converter.PassThrough<EvaluateQuery, EvaluateQuery.Argument<object>>(
				HttpContext,
				new EvaluateQuery.Argument<object> { QueryName = type.Result.FullName });
		}

		private void Evaluate<TFormat>(HttpContext context, Type query, TFormat data)
		{
			Converter.ConvertStream<EvaluateQuery, EvaluateQuery.Argument<TFormat>>(
				context,
				new EvaluateQuery.Argument<TFormat>
				{
					QueryName = query.FullName,
					Data = data
				});
		}
		
		[HttpPut("query/{query}")]
		public void EvaluateQuery(string query)
		{
			var type = Utility.CheckDomainObject(DomainModel, query, HttpContext.Response);
			if (type.IsFailure) return;
			var request = HttpContext.Request;
			switch (Utility.GetIncomingFormat(request))
			{
				//TODO: maybe it's ok to use stream reader now!?
				case MessageFormat.Json:
					Evaluate(HttpContext, type.Result, new StreamReader(request.Body, Encoding.UTF8).ReadToEnd());
					break;
				case MessageFormat.ProtoBuf:
					Evaluate<Stream>(HttpContext, type.Result, request.Body);
					break;
				default:
					var xml = Utility.ParseXml(request.Body, HttpContext.Response);
					if (xml.IsFailure) return;
					Evaluate(HttpContext, type.Result, xml.Result);
					break;
			}
		}
	}
}
