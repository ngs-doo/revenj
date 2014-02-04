using System;
using System.IO;
using System.Net;
using System.ServiceModel;
using System.Xml.Linq;
using NGS.DomainPatterns;
using NGS.Serialization;
using Revenj.Api;
using Revenj.Plugins.Server.Commands;

namespace Revenj.Plugins.Rest.Commands
{
	[ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
	public class ReportingCommands : IReportingCommands
	{
		private readonly IServiceLocator Locator;
		private readonly ICommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public ReportingCommands(
			IServiceLocator locator,
			ICommandConverter converter,
			IDomainModel domainModel,
			IWireSerialization serialization)
		{
			this.Locator = locator;
			this.Converter = converter;
			this.DomainModel = domainModel;
			this.Serialization = serialization;
		}

		public Stream PopulateReport(string report, Stream body)
		{
			var reportType = Utility.CheckDomainObject(DomainModel, report);
			var validatedData = Utility.ParseObject(Serialization, reportType, body, true, Locator);
			return Converter.PassThrough<PopulateReport, PopulateReport.Argument<object>>(
				new PopulateReport.Argument<object>
				{
					ReportName = reportType.FullName,
					Data = validatedData
				});
		}

		public Stream CreateReport(string report, string templater, Stream body)
		{
			var reportType = Utility.CheckDomainObject(DomainModel, report);
			Utility.CheckDomainObject(DomainModel, reportType.FullName + "+" + templater);
			var validatedData = Utility.ParseObject(Serialization, reportType, body, true, Locator);
			return Converter.PassThrough<CreateReport, CreateReport.Argument<object>>(
				new CreateReport.Argument<object>
				{
					ReportName = reportType.FullName,
					TemplaterName = templater,
					Data = validatedData
				});
		}

		private Stream OlapCube(
			Type cubeType,
			Type specificationType,
			string templater,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream data)
		{
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
				Utility.ThrowError("At least one dimension or fact must be specified", HttpStatusCode.BadRequest);
			var ordDict = Utility.ParseOrder(order);
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);

			var specification = specificationType != null
				? Utility.ParseObject(Serialization, specificationType, data, true, Locator)
				: null;

			return Converter.PassThrough<OlapCubeReport, OlapCubeReport.Argument<object>>(
				new OlapCubeReport.Argument<object>
				{
					CubeName = cubeType.FullName,
					TemplaterName = templater,
					SpecificationName = specificationType != null ? specificationType.FullName : null,
					Specification = specification,
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
				Utility.ThrowError("Specification must be specified", HttpStatusCode.BadRequest);
			var cubeType = Utility.CheckDomainObject(DomainModel, cube);
			var specType =
				Utility.CheckDomainObject(
					DomainModel,
					(specification.Contains("+") ? string.Empty : cubeType.FullName + "+") + specification);
			Utility.CheckDomainObject(DomainModel, cubeType.FullName + "+" + templater);
			return OlapCube(cubeType, specType, templater, dimensions, facts, order, limit, offset, body);
		}

		private Stream OlapCube(
			Type cubeType,
			string templater,
			string dimensions,
			string facts,
			string order,
			object specification,
			string limit,
			string offset)
		{
			Utility.CheckDomainObject(DomainModel, cubeType.FullName + "+" + templater);
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
				Utility.ThrowError("At least one dimension or fact must be specified", HttpStatusCode.BadRequest);
			var ordDict = Utility.ParseOrder(order);
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);
			return
				Converter.PassThrough<OlapCubeReport, OlapCubeReport.Argument<object>>(
					new OlapCubeReport.Argument<object>
					{
						CubeName = cubeType.FullName,
						TemplaterName = templater,
						SpecificationName = null,
						Specification = specification,
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
			var specType = string.IsNullOrEmpty(specification)
				? null
				: Utility.CheckDomainObject(
					DomainModel,
					(specification.Contains("+") ? string.Empty : cubeType.FullName + "+") + specification);

			var spec = specType != null ? Utility.SpecificationFromQuery(specType) : null;
			return OlapCube(cubeType, templater, dimensions, facts, order, spec, limit, offset);
		}

		public Stream OlapCubeWithGenericSpecification(
			string cube,
			string templater,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream body)
		{
			var ci = Utility.CheckCube(DomainModel, cube);
			if (ci.Value == null)
				Utility.ThrowError("Cube data source not found. Static DataSource property not found.", HttpStatusCode.NotImplemented);

			object spec;
			switch (Utility.GetIncomingFormat())
			{
				case MessageFormat.Json:
					spec = Utility.ParseGenericSpecification<string>(Serialization, ci.Key, body);
					break;
				case MessageFormat.ProtoBuf:
					spec = Utility.ParseGenericSpecification<MemoryStream>(Serialization, ci.Key, body);
					break;
				default:
					spec = Utility.ParseGenericSpecification<XElement>(Serialization, ci.Key, body);
					break;
			}
			return OlapCube(ci.Key, templater, dimensions, facts, order, spec, limit, offset);
		}

		public Stream OlapCubeWithGenericSpecificationQuery(
			string cube,
			string templater,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset)
		{
			var ci = Utility.CheckCube(DomainModel, cube);
			if (ci.Value == null)
				Utility.ThrowError("Cube data source not found. Static DataSource property not found.", HttpStatusCode.NotImplemented);

			var spec = Utility.GenericSpecificationFromQuery(ci.Value);
			return OlapCube(ci.Key, templater, dimensions, facts, order, spec, limit, offset);
		}

		public Stream OlapCubeWithExpression(
			string cube,
			string templater,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream body)
		{
			var ci = Utility.CheckCube(DomainModel, cube);
			Utility.CheckDomainObject(DomainModel, ci.Key.FullName + "+" + templater);
			if (ci.Value == null)
				Utility.ThrowError("Cube data source not found. Static DataSource property not found.", HttpStatusCode.NotImplemented);
			var spec = Utility.ParseExpressionSpecification(Serialization, ci.Key, body);
			return OlapCube(ci.Key, templater, dimensions, facts, order, spec, limit, offset);
		}

		public Stream GetHistory(string root, string uris)
		{
			Utility.CheckAggregateRoot(DomainModel, root);
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
			Utility.CheckAggregateRoot(DomainModel, root);
			var uris = Serialization.Deserialize<string[]>(body, ThreadContext.Request.ContentType);
			return
				Converter.PassThrough<GetRootHistory, GetRootHistory.Argument>(
					new GetRootHistory.Argument
					{
						Name = root,
						Uri = uris
					});
		}

		public Stream FindTemplater(string file, string domainObject, string uri)
		{
			Utility.CheckIdentifiable(DomainModel, domainObject);
			var requestPdf =
				(ThreadContext.Request.Accept ?? string.Empty)
				.Equals("application/pdf", StringComparison.CurrentCultureIgnoreCase);

			var result =
				Converter.PassThrough<TemplaterProcessDocument, TemplaterProcessDocument.Argument<object>>(
					new TemplaterProcessDocument.Argument<object>
					{
						File = Path.GetFileName(file),
						GetSources = new[] { new GetDomainObject.Argument { Name = domainObject, Uri = new[] { uri } } },
						ToPdf = requestPdf
					},
					"application/octet-stream");
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
			string specification,
			Func<Type, object> buildSpecification)
		{
			var type = Utility.CheckDomainObject(DomainModel, domainObject);
			var specType = string.IsNullOrEmpty(specification)
				? type
				: Utility.CheckDomainObject(
					DomainModel,
					(specification.Contains("+") ? string.Empty : type.FullName + "+") + specification);
			var requestPdf =
				(ThreadContext.Request.Accept ?? string.Empty)
				.Equals("application/pdf", StringComparison.CurrentCultureIgnoreCase);

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
									Specification = buildSpecification(specType),
									Name = type.FullName 
								}},
						ToPdf = requestPdf
					},
					"application/octet-stream");

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
			Func<Type, object> getSpec = type => Utility.ParseObject(Serialization, type, body, false, Locator);
			return SearchTemplater(file, domainObject, specification, getSpec);
		}

		public Stream SearchTemplaterQuery(string file, string domainObject, string specification)
		{
			Func<Type, object> getSpec = type => string.IsNullOrEmpty(specification) ? null : Utility.SpecificationFromQuery(type);
			return SearchTemplater(file, domainObject, specification, getSpec);
		}

		public Stream SearchTemplaterWithGenericSpecification(string file, string domainObject, Stream body)
		{
			switch (Utility.GetIncomingFormat())
			{
				case MessageFormat.Json:
					Func<Type, object> getJsonSpec = type => Utility.ParseGenericSpecification<string>(Serialization, type, body);
					return SearchTemplater(file, domainObject, null, getJsonSpec);
				case MessageFormat.ProtoBuf:
					Func<Type, object> getProtobufSpec = type => Utility.ParseGenericSpecification<MemoryStream>(Serialization, type, body);
					return SearchTemplater(file, domainObject, null, getProtobufSpec);
				default:
					Func<Type, object> getXmlSpec = type => Utility.ParseGenericSpecification<XElement>(Serialization, type, body);
					return SearchTemplater(file, domainObject, null, getXmlSpec);
			}
		}

		public Stream SearchTemplaterWithGenericSpecificationQuery(string file, string domainObject)
		{
			Func<Type, object> getSpec = type => Utility.GenericSpecificationFromQuery(type);
			return SearchTemplater(file, domainObject, null, getSpec);
		}

		public Stream SearchTemplaterWithExpression(string file, string domainObject, Stream body)
		{
			Func<Type, object> getSpec = type => Utility.ParseExpressionSpecification(Serialization, type, body);
			return SearchTemplater(file, domainObject, null, getSpec);
		}
	}
}
