using System;
using System.IO;
using System.ServiceModel;
using Revenj.Api;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;

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
			var type = Utility.CheckIdentifiable(DomainModel, domainObject);
			var uris = Serialization.TryDeserialize<string[]>(body);
			if (type.IsFailure || uris.IsFailure) return type.Error ?? uris.Error;
			return
				Converter.PassThrough<GetDomainObject, GetDomainObject.Argument>(
					new GetDomainObject.Argument
					{
						Name = domainObject,
						Uri = uris.Result
					});
		}

		private Stream Search(
			Either<Type> doType,
			Stream data,
			Either<Type> specType,
			string limit,
			string offset,
			string order)
		{
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);
			var ordDict = Utility.ParseOrder(order);
			var spec = Utility.ParseObject(Serialization, specType, data, true, Locator);
			if (doType.IsFailure || specType.IsFailure || spec.IsFailure) return doType.Error ?? specType.Error ?? spec.Error;
			return
				Converter.PassThrough<SearchDomainObject, SearchDomainObject.Argument<object>>(
					new SearchDomainObject.Argument<object>
					{
						Name = doType.Result.FullName,
						SpecificationName = specType.Result != null ? specType.Result.FullName : null,
						Specification = spec.Result,
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
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification);
			return Search(doType, body, specType, limit, offset, order);
		}

		private Stream Search(
			Either<Type> domainType,
			string limit,
			string offset,
			string order,
			Either<object> specification)
		{
			if (domainType.IsFailure || specification.IsFailure) return domainType.Error ?? specification.Error;
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);
			var ordDict = Utility.ParseOrder(order);
			return
				Converter.PassThrough<SearchDomainObject, SearchDomainObject.Argument<object>>(
					new SearchDomainObject.Argument<object>
					{
						Name = domainType.Result.FullName,
						SpecificationName = null,
						Specification = specification.Result,
						Limit = intLimit,
						Offset = intOffset,
						Order = ordDict
					});
		}

		public Stream SearchQuery(string domainObject, string specification, string limit, string offset, string order)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification);
			var spec = Utility.SpecificationFromQuery(specType);
			return Search(doType, limit, offset, order, spec);
		}

		public Stream SearchWithGenericSpecification(string domainObject, string limit, string offset, string order, Stream body)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			var spec = Serialization.ParseGenericSpecification(doType, body);
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
			Either<Type> specType)
		{
			var spec = Utility.ParseObject(Serialization, specType, data, true, Locator);
			if (spec.IsFailure) return spec.Error;
			return
				Converter.PassThrough<CountDomainObject, CountDomainObject.Argument<object>>(
					new CountDomainObject.Argument<object>
					{
						Name = domainObject,
						SpecificationName = specType.Result != null ? specType.Result.FullName : null,
						Specification = spec.Result
					});
		}

		public Stream CountWithSpecification(string domainObject, string specification, Stream body)
		{
			return CountWithSpecificationQuery(domainObject, specification, body);
		}

		public Stream CountWithSpecificationQuery(string domainObject, string specification, Stream body)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification);
			return Count(domainObject, body, specType);
		}

		private Stream Count(Either<Type> domainType, Either<object> specification)
		{
			if (domainType.IsFailure || specification.IsFailure) return domainType.Error ?? specification.Error;
			return
				Converter.PassThrough<CountDomainObject, CountDomainObject.Argument<object>>(
					new CountDomainObject.Argument<object>
					{
						Name = domainType.Result.FullName,
						SpecificationName = null,
						Specification = specification.Result
					});
		}

		public Stream CountQuery(string domainObject, string specification)
		{
			var domainType = Utility.CheckDomainObject(DomainModel, domainObject);
			var specType = Utility.CheckDomainObject(DomainModel, domainType, specification);
			var spec = Utility.SpecificationFromQuery(specType);
			return Count(domainType, spec);
		}

		public Stream CountWithGenericSpecification(string domainObject, Stream body)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject);
			var spec = Serialization.ParseGenericSpecification(doType, body);
			return Count(doType, spec);
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
			if (validatedObject.IsFailure) return validatedObject.Error;
			return
				Converter.PassThrough<SubmitEvent, SubmitEvent.Argument<object>>(
					new SubmitEvent.Argument<object>
					{
						Name = domainType.Result.FullName,
						Data = validatedObject.Result,
						ReturnInstance = returnInstance
					});
		}

		public Stream SubmitAggregateEvent(string aggregate, string domainEvent, string uri, Stream body)
		{
			var aggregateType = Utility.CheckAggregateRoot(DomainModel, aggregate);
			if (aggregateType.IsFailure) return aggregateType.Error;
			var domainType = Utility.CheckDomainEvent(DomainModel, aggregateType.Result.FullName + "+" + domainEvent);
			var validatedObject = Utility.ParseObject(Serialization, domainType, body, true, Locator);
			if (validatedObject.IsFailure) return validatedObject.Error;
			return
				Converter.PassThrough<SubmitAggregateEvent, SubmitAggregateEvent.Argument<object>>(
					new SubmitAggregateEvent.Argument<object>
					{
						Name = domainType.Result.FullName,
						Uri = uri,
						Data = validatedObject.Result
					});
		}

		public Stream SubmitAggregateEventQuery(string aggregate, string domainEvent, string uri, Stream body)
		{
			return SubmitAggregateEvent(aggregate, domainEvent, uri, body);
		}
	}
}
