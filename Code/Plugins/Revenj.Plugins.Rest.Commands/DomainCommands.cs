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
	public class DomainCommands : IDomainCommands
	{
		private readonly IServiceLocator Locator;
		private readonly ICommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public DomainCommands(
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

		public Stream Find(string domainObject, string uris)
		{
			Utility.CheckIdentifiable(DomainModel, domainObject);
			return
				Converter.PassThrough<GetDomainObject, GetDomainObject.Argument>(
					new GetDomainObject.Argument
					{
						Name = domainObject,
						Uri = (uris ?? string.Empty).Split(',')
					});
		}

		public Stream FindQuery(string domainObject, string uris)
		{
			return Find(domainObject, uris);
		}

		public Stream FindFrom(string domainObject, Stream body)
		{
			Utility.CheckIdentifiable(DomainModel, domainObject);
			var uris = Serialization.Deserialize<string[]>(body, ThreadContext.Request.ContentType);
			return
				Converter.PassThrough<GetDomainObject, GetDomainObject.Argument>(
					new GetDomainObject.Argument
					{
						Name = domainObject,
						Uri = uris
					});
		}

		private Stream Search(
			string domainObject,
			Stream data,
			Type specType,
			string limit,
			string offset,
			string order)
		{
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);
			var ordDict = Utility.ParseOrder(order);
			var spec = specType != null ? Utility.ParseObject(Serialization, specType, data, true, Locator) : null;
			return
				Converter.PassThrough<SearchDomainObject, SearchDomainObject.Argument<object>>(
					new SearchDomainObject.Argument<object>
					{
						Name = domainObject,
						SpecificationName = specType != null ? specType.FullName : null,
						Specification = spec,
						Limit = intLimit,
						Offset = intOffset,
						Order = ordDict
					});
		}

		public Stream SearchWithSpecification(string domainObject, string specification, string limit, string offset, string order, Stream body)
		{
			return SearchWithSpecificationQuery(domainObject, specification, limit, offset, order, body);
		}

		public Stream SearchWithSpecificationQuery(string domainObject, string specification, string limit, string offset, string order, Stream body)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			if (string.IsNullOrEmpty(specification))
				Utility.ThrowError("Specification must be specified", HttpStatusCode.BadRequest);
			var specType =
				Utility.CheckDomainObject(
					DomainModel,
					(specification.Contains("+") ? string.Empty : doType.FullName + "+") + specification);

			return Search(domainObject, body, specType, limit, offset, order);
		}

		private Stream Search(
			Type domainType,
			string limit,
			string offset,
			string order,
			object specification)
		{
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);
			var ordDict = Utility.ParseOrder(order);

			return
				Converter.PassThrough<SearchDomainObject, SearchDomainObject.Argument<object>>(
					new SearchDomainObject.Argument<object>
					{
						Name = domainType.FullName,
						SpecificationName = null,
						Specification = specification,
						Limit = intLimit,
						Offset = intOffset,
						Order = ordDict
					});
		}

		public Stream SearchQuery(string domainObject, string specification, string limit, string offset, string order)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			var specType = string.IsNullOrEmpty(specification)
				? null
				: Utility.CheckDomainObject(
					DomainModel,
					(specification.Contains("+") ? string.Empty : doType.FullName + "+") + specification);

			var spec = specType != null ? Utility.SpecificationFromQuery(specType) : null;
			return Search(doType, limit, offset, order, spec);
		}

		public Stream SearchWithGenericSpecification(string domainObject, string limit, string offset, string order, Stream body)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			object spec;
			switch (Utility.GetIncomingFormat())
			{
				case MessageFormat.Json:
					spec = Utility.ParseGenericSpecification<string>(Serialization, doType, body);
					break;
				case MessageFormat.ProtoBuf:
					spec = Utility.ParseGenericSpecification<MemoryStream>(Serialization, doType, body);
					break;
				default:
					spec = Utility.ParseGenericSpecification<XElement>(Serialization, doType, body);
					break;
			}
			return Search(doType, limit, offset, order, spec);
		}

		public Stream SearchWithGenericSpecificationQuery(string domainObject, string limit, string offset, string order)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			var spec = Utility.GenericSpecificationFromQuery(doType);
			return Search(doType, limit, offset, order, spec);
		}

		public Stream SearchWithExpression(string domainObject, string limit, string offset, string order, Stream body)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			var spec = Utility.ParseExpressionSpecification(Serialization, doType, body);
			return Search(doType, limit, offset, order, spec);
		}

		private Stream Count(
			string domainObject,
			Stream data,
			Type specType)
		{
			var spec = specType != null ? Utility.ParseObject(Serialization, specType, data, true, Locator) : null;
			return
				Converter.PassThrough<CountDomainObject, CountDomainObject.Argument<object>>(
					new CountDomainObject.Argument<object>
					{
						Name = domainObject,
						SpecificationName = specType != null ? specType.FullName : null,
						Specification = spec
					});
		}

		public Stream CountWithSpecification(string domainObject, string specification, Stream body)
		{
			return CountWithSpecificationQuery(domainObject, specification, body);
		}

		public Stream CountWithSpecificationQuery(string domainObject, string specification, Stream body)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			var specType = string.IsNullOrEmpty(specification)
				? null
				: Utility.CheckDomainObject(
					DomainModel,
					(specification.Contains("+") ? string.Empty : doType.FullName + "+") + specification);

			return Count(domainObject, body, specType);
		}

		private Stream Count(Type domainType, object specification)
		{
			return
				Converter.PassThrough<CountDomainObject, CountDomainObject.Argument<object>>(
					new CountDomainObject.Argument<object>
					{
						Name = domainType.FullName,
						SpecificationName = null,
						Specification = specification
					});
		}

		public Stream CountQuery(string domainObject, string specification)
		{
			var domainType = Utility.CheckDomainObject(DomainModel, domainObject);
			var specType = string.IsNullOrEmpty(specification)
				? null
				: Utility.CheckDomainObject(
					DomainModel,
					(specification.Contains("+") ? string.Empty : domainType.FullName + "+") + specification);

			var spec = specType != null ? Utility.SpecificationFromQuery(specType) : null;
			return Count(domainType, spec);
		}

		public Stream CountWithGenericSpecification(string domainObject, Stream body)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			object spec;
			switch (Utility.GetIncomingFormat())
			{
				case MessageFormat.Json:
					spec = Utility.ParseGenericSpecification<string>(Serialization, doType, body);
					return Count(doType, spec);
				case MessageFormat.ProtoBuf:
					spec = Utility.ParseGenericSpecification<MemoryStream>(Serialization, doType, body);
					return Count(doType, spec);
				default:
					spec = Utility.ParseGenericSpecification<XElement>(Serialization, doType, body);
					return Count(doType, spec);
			}
		}

		public Stream CountWithGenericSpecificationQuery(string domainObject)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			var spec = Utility.GenericSpecificationFromQuery(doType);
			return Count(doType, spec);
		}

		public Stream CountWithExpression(string domainObject, Stream body)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			var spec = Utility.ParseExpressionSpecification(Serialization, doType, body);
			return Count(doType, spec);
		}

		public Stream SubmitEvent(string domainEvent, string result, Stream body)
		{
			var domainType = Utility.CheckDomainEvent(DomainModel, domainEvent);
			bool? returnInstance = result == "instance" ? true : result == "uri" ? (bool?)false : null;
			var validatedObject = Utility.ParseObject(Serialization, domainType, body, true, Locator);
			return
				Converter.PassThrough<SubmitEvent, SubmitEvent.Argument<object>>(
					new SubmitEvent.Argument<object>
					{
						Name = domainType.FullName,
						Data = validatedObject,
						ReturnInstance = returnInstance
					});
		}

		public Stream SubmitAggregateEvent(string aggregate, string domainEvent, string uri, Stream body)
		{
			var aggregateType = Utility.CheckAggregateRoot(DomainModel, aggregate);
			var domainType = Utility.CheckDomainEvent(DomainModel, aggregateType.FullName + "+" + domainEvent);
			var validatedObject = Utility.ParseObject(Serialization, domainType, body, true, Locator);
			return
				Converter.PassThrough<SubmitAggregateEvent, SubmitAggregateEvent.Argument<object>>(
					new SubmitAggregateEvent.Argument<object>
					{
						Name = domainType.FullName,
						Uri = uri,
						Data = domainEvent
					});
		}

		public Stream SubmitAggregateEventQuery(string aggregate, string domainEvent, string uri, Stream body)
		{
			return SubmitAggregateEvent(aggregate, domainEvent, uri, body);
		}
	}
}
