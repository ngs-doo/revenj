using System;
using System.IO;
using System.ServiceModel;
using System.Text;
using Revenj.Api;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;

namespace Revenj.Plugins.Rest.Commands
{
	[ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
	public class ReportingCommands : IReportingCommands
	{
		private readonly IServiceProvider Locator;
		private readonly ICommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public ReportingCommands(
			IServiceProvider locator,
			ICommandConverter converter,
			IDomainModel domainModel,
			IWireSerialization serialization)
		{
			this.Locator = locator;
			this.Converter = converter;
			this.DomainModel = domainModel;
			this.Serialization = serialization;
		}

		public Stream PopulateReportQuery(string report)
		{
			var reportType = Utility.CheckDomainObject(DomainModel, report);
			var reportData = Utility.ObjectFromQuery(reportType);
			if (reportData.IsFailure) return reportData.Error;
			return Converter.PassThrough<PopulateReport, PopulateReport.Argument<object>>(
				new PopulateReport.Argument<object>
				{
					ReportName = reportType.Result.FullName,
					Data = reportData.Result
				});
		}

		public Stream PopulateReport(string report, Stream body)
		{
			var reportType = Utility.CheckDomainObject(DomainModel, report);
			var validatedData = Utility.ParseObject(Serialization, reportType, body, true, Locator);
			if (validatedData.IsFailure) return validatedData.Error;
			return Converter.PassThrough<PopulateReport, PopulateReport.Argument<object>>(
				new PopulateReport.Argument<object>
				{
					ReportName = reportType.Result.FullName,
					Data = validatedData.Result
				});
		}

		public Stream CreateReport(string report, string templater, Stream body)
		{
			var reportType = Utility.CheckDomainObject(DomainModel, report);
			var templaterType = Utility.CheckDomainObject(DomainModel, reportType, templater);
			var validatedData = Utility.ParseObject(Serialization, reportType, body, true, Locator);
			if (templaterType.IsFailure || validatedData.IsFailure) return templaterType.Error ?? validatedData.Error;
			return Converter.PassThrough<CreateReport, CreateReport.Argument<object>>(
				new CreateReport.Argument<object>
				{
					ReportName = reportType.Result.FullName,
					TemplaterName = templater,
					Data = validatedData.Result
				});
		}

		private Stream OlapCube(
			Either<Type> cubeType,
			Either<Type> specType,
			string templater,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream data)
		{
			var spec = Utility.ParseObject(Serialization, specType, data, true, Locator);
			if (cubeType.IsFailure || specType.IsFailure || spec.IsFailure)
				return cubeType.Error ?? specType.Error ?? spec.Error;
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
				return Either<object>.BadRequest("At least one dimension or fact must be specified").Error;
			var ordDict = Utility.ParseOrder(order);
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);

			return Converter.PassThrough<OlapCubeReport, OlapCubeReport.Argument<object>>(
				new OlapCubeReport.Argument<object>
				{
					CubeName = cubeType.Result.FullName,
					TemplaterName = templater,
					SpecificationName = specType.Result != null ? specType.Result.FullName : null,
					Specification = spec.Result,
					Dimensions = dimArr,
					Facts = factArr,
					Order = ordDict,
					Limit = intLimit,
					Offset = intOffset
				});
		}

		public Stream OlapCubeWithSpecification(
			string cube,
			string templater,
			string specification,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream body)
		{
			return OlapCubeWithSpecificationQuery(cube, templater, specification, dimensions, facts, order, limit, offset, body);
		}

		public Stream OlapCubeWithSpecificationQuery(
			string cube,
			string templater,
			string specification,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream body)
		{
			if (string.IsNullOrEmpty(specification))
				return Either<object>.BadRequest("Specification must be specified").Error;
			var cubeType = Utility.CheckDomainObject(DomainModel, cube);
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification);
			return OlapCube(cubeType, specType, templater, dimensions, facts, order, limit, offset, body);
		}

		private Stream OlapCube(
			Either<Type> cubeType,
			string templater,
			string dimensions,
			string facts,
			string order,
			Either<object> spec,
			string limit,
			string offset)
		{
			var templaterType = Utility.CheckDomainObject(DomainModel, cubeType, templater);
			if (templaterType.IsFailure || spec.IsFailure) return templaterType.Error ?? spec.Error;
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
				return Either<object>.BadRequest("At least one dimension or fact must be specified").Error;
			var ordDict = Utility.ParseOrder(order);
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);
			return
				Converter.PassThrough<OlapCubeReport, OlapCubeReport.Argument<object>>(
					new OlapCubeReport.Argument<object>
					{
						CubeName = cubeType.Result.FullName,
						TemplaterName = templater,
						SpecificationName = null,
						Specification = spec.Result,
						Dimensions = dimArr,
						Facts = factArr,
						Order = ordDict,
						Limit = intLimit,
						Offset = intOffset
					});
		}

		public Stream OlapCubeQuery(
			string cube,
			string templater,
			string specification,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset)
		{
			var cubeType = Utility.CheckDomainObject(DomainModel, cube);
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification);
			var spec = Utility.ObjectFromQuery(specType);
			return OlapCube(cubeType, templater, dimensions, facts, order, spec, limit, offset);
		}

		public Stream GetHistory(string root, string uris)
		{
			var type = Utility.CheckAggregateRoot(DomainModel, root);
			if (type.IsFailure) return type.Error;
			return
				Converter.PassThrough<GetRootHistory, GetRootHistory.Argument>(
					new GetRootHistory.Argument
					{
						Name = root,
						Uri = (uris ?? string.Empty).Split(',')
					});
		}

		public Stream GetHistoryFrom(string root, Stream body)
		{
			var type = Utility.CheckAggregateRoot(DomainModel, root);
			var uris = Serialization.TryDeserialize<string[]>(body);
			if (type.IsFailure || uris.IsFailure) return type.Error ?? uris.Error;
			return
				Converter.PassThrough<GetRootHistory, GetRootHistory.Argument>(
					new GetRootHistory.Argument
					{
						Name = root,
						Uri = uris.Result
					});
		}

		public Stream FindTemplater(string file, string domainObject, string uri)
		{
			var type = Utility.CheckIdentifiable(DomainModel, domainObject);
			if (type.IsFailure) return type.Error;
			var requestPdf = ThreadContext.Request.Accept == "application/pdf";
			var result =
				Converter.PassThrough<TemplaterProcessDocument, TemplaterProcessDocument.Argument<object>>(
					new TemplaterProcessDocument.Argument<object>
					{
						File = Path.GetFileName(file),
						GetSources = new[] { new GetDomainObject.Argument { Name = domainObject, Uri = new[] { uri } } },
						ToPdf = requestPdf
					},
					"application/octet-stream",
					null);
			ThreadContext.Response.ContentType = requestPdf
				? "application/pdf"
				: "application/octet-stream";
			return result;
		}

		public Stream FindTemplaterQuery(string file, string domainObject, string uri)
		{
			return FindTemplater(file, domainObject, uri);
		}

		private Stream SearchTemplater(
			string file,
			string domainObject,
			Either<object> spec)
		{
			if (spec.IsFailure) return spec.Error;
			var requestPdf = ThreadContext.Request.Accept == "application/pdf";
			var result =
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
					null);

			ThreadContext.Response.ContentType = requestPdf
				? "application/pdf"
				: "application/octet-stream";
			return result;
		}

		public Stream SearchTemplaterWithSpecification(string file, string domainObject, string specification, Stream body)
		{
			return SearchTemplaterWithSpecificationQuery(file, domainObject, specification, body);
		}

		public Stream SearchTemplaterWithSpecificationQuery(string file, string domainObject, string specification, Stream body)
		{
			var type = Utility.CheckDomainObject(DomainModel, domainObject);
			var specType = Utility.CheckDomainObject(DomainModel, type, specification);
			if (specType.IsFailure || specType.Result == null)
				return specType.Error ?? Either<object>.BadRequest("Specification must be specified").Error;
			var spec = Utility.ParseObject(Serialization, specType, body, false, Locator);
			return SearchTemplater(file, domainObject, spec);
		}

		public Stream SearchTemplaterQuery(string file, string domainObject, string specification)
		{
			var type = Utility.CheckDomainObject(DomainModel, domainObject);
			var specType = Utility.CheckDomainObject(DomainModel, type, specification);
			var spec = Utility.ObjectFromQuery(specType);
			if (specType.IsFailure || spec.IsFailure) return specType.Error ?? spec.Error;
			return SearchTemplater(file, domainObject, spec);
		}

		public Stream SearchTemplaterWithGenericSpecification(string file, string domainObject, Stream body)
		{
			var type = Utility.CheckDomainObject(DomainModel, domainObject);
			var spec = Serialization.ParseGenericSpecification(type, body);
			return SearchTemplater(file, domainObject, spec);
		}

		public Stream SearchTemplaterWithGenericSpecificationQuery(string file, string domainObject)
		{
			var type = Utility.CheckDomainObject(DomainModel, domainObject);
			var spec = Utility.GenericSpecificationFromQuery(type);
			return SearchTemplater(file, domainObject, spec);
		}

		public Stream SearchTemplaterWithExpression(string file, string domainObject, Stream body)
		{
			var type = Utility.CheckDomainObject(DomainModel, domainObject);
			var spec = Utility.ParseExpressionSpecification(Serialization, type, body);
			return SearchTemplater(file, domainObject, spec);
		}

		public Stream EvaluateQuery(string domainObject)
		{
			var type = Utility.CheckDomainObject(DomainModel, domainObject);
			if (type.IsFailure) return type.Error;
			return
				Converter.PassThrough<EvaluateQuery, EvaluateQuery.Argument<object>>(
					new EvaluateQuery.Argument<object> { QueryName = type.Result.FullName });
		}

		private Stream Evaluate<TFormat>(Type query, TFormat data)
		{
			return Converter.ConvertStream<EvaluateQuery, EvaluateQuery.Argument<TFormat>>(
				new EvaluateQuery.Argument<TFormat>
				{
					QueryName = query.FullName,
					Data = data
				});
		}

		public Stream EvaluateQuery(string domainObject, Stream body)
		{
			var type = Utility.CheckDomainObject(DomainModel, domainObject);
			if (type.IsFailure) return type.Error;
			switch (Utility.GetIncomingFormat())
			{
				//TODO: maybe it's ok to use stream reader now!?
				case MessageFormat.Json:
					return Evaluate(type.Result, new StreamReader(body, Encoding.UTF8).ReadToEnd());
				case MessageFormat.ProtoBuf:
					return Evaluate<Stream>(type.Result, body);
				default:
					var xml = Utility.ParseXml(body);
					if (xml.IsFailure) return xml.Error;
					return Evaluate(type.Result, xml.Result);
			}
		}
	}
}
