using Microsoft.AspNetCore.Mvc;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;
using System;

namespace Revenj.Plugins.AspNetCore.Commands
{
	[Route("Domain.svc")]
	public class DomainController : ControllerBase
	{
		private static readonly AdditionalCommand[] EmptyCommands = new AdditionalCommand[0];

		private readonly IServiceProvider Locator;
		private readonly CommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public DomainController(
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

		[HttpPut("find/{domainObject}")]
		public void Find(string domainObject, [FromQuery(Name = "order")] string order = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var type = Utility.CheckIdentifiable(DomainModel, domainObject, response);
			if (type.IsFailure) return;
			var uris = Serialization.TryDeserialize<string[]>(request, response);
			if (uris.IsFailure) return;

			Converter.PassThrough<GetDomainObject, GetDomainObject.Argument>(
				HttpContext,
				new GetDomainObject.Argument
				{
					Name = domainObject,
					Uri = uris.Result,
					MatchOrder = order == "match"
				});
		}

		[HttpPut("/search/{domainObject}")]
		public void Search(
			string domainObject,
			[FromQuery(Name = "specification")] string specification = null,
			[FromQuery(Name = "limit")] int? limit = null,
			[FromQuery(Name = "offset")] int? offset = null,
			[FromQuery(Name = "order")] string order = null,
			[FromQuery(Name = "count")] string count = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification, response);
			var spec = Utility.ParseObject(Serialization, specType, request.Body, true, Locator, request, response);
			var ordDict = Utility.ParseOrder(order);
			if (spec.IsFailure) return;

			var additionalCommands = EmptyCommands;
			if (limit != null && Utility.IncludeCount(count, request))
			{
				additionalCommands = new[]
				{
					new AdditionalCommand
					{
						Argument = new CountDomainObject.Argument<object>
						{
							Name = doType.Result.FullName,
							SpecificationName = specType.Result != null ? specType.Result.FullName : null,
							Specification = spec.Result
						},
						CommandType = typeof(CountDomainObject),
						ToHeader = "Total-Results"
					}
				};
			}
			Converter.PassThrough<SearchDomainObject, SearchDomainObject.Argument<object>>(
				new SearchDomainObject.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = specType.Result != null ? specType.Result.FullName : null,
					Specification = spec.Result,
					Limit = limit,
					Offset = offset,
					Order = ordDict
				},
				CommandConverter.Accept(request.Headers),
				HttpContext,
				additionalCommands);
		}

		[HttpPut("/search-generic/{domainObject}")]
		public void SearchGeneric(
			string domainObject,
			[FromQuery(Name = "limit")] int? limit = null,
			[FromQuery(Name = "offset")] int? offset = null,
			[FromQuery(Name = "order")] string order = null,
			[FromQuery(Name = "count")] string count = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var spec = Utility.ParseGenericSpecification(Serialization, doType, request.Body, request, response);
			var ordDict = Utility.ParseOrder(order);
			if (spec.IsFailure) return;

			var additionalCommands = EmptyCommands;
			if (limit != null && Utility.IncludeCount(count, request))
			{
				additionalCommands = new[]
				{
					new AdditionalCommand
					{
						Argument = new CountDomainObject.Argument<object>
						{
							Name = doType.Result.FullName,
							SpecificationName = null,
							Specification = spec.Result
						},
						CommandType = typeof(CountDomainObject),
						ToHeader = "Total-Results"
					}
				};
			}
			Converter.PassThrough<SearchDomainObject, SearchDomainObject.Argument<object>>(
				new SearchDomainObject.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = null,
					Specification = spec.Result,
					Limit = limit,
					Offset = offset,
					Order = ordDict
				},
				CommandConverter.Accept(request.Headers),
				HttpContext,
				additionalCommands);
		}

		[HttpPut("/count/{domainObject}")]
		public void Count(
			string domainObject,
			[FromQuery(Name = "specification")] string specification = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification, response);
			var spec = Utility.ParseObject(Serialization, specType, request.Body, true, Locator, request, response);
			if (spec.IsFailure) return;

			Converter.PassThrough<CountDomainObject, CountDomainObject.Argument<object>>(
				HttpContext,
				new CountDomainObject.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = specType.Result != null ? specType.Result.FullName : null,
					Specification = spec.Result,
				});
		}

		[HttpPut("/count-generic/{domainObject}")]
		public void CountGeneric(string domainObject)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var spec = Utility.ParseGenericSpecification(Serialization, doType, request.Body, request, response);
			if (spec.IsFailure) return;

			Converter.PassThrough<CountDomainObject, CountDomainObject.Argument<object>>(
				HttpContext,
				new CountDomainObject.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = null,
					Specification = spec.Result,
				});
		}

		[HttpPut("/exists/{domainObject}")]
		public void Exists(
			string domainObject,
			[FromQuery(Name = "specification")] string specification = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification, response);
			var spec = Utility.ParseObject(Serialization, specType, request.Body, true, Locator, request, response);
			if (spec.IsFailure) return;

			Converter.PassThrough<DomainObjectExists, DomainObjectExists.Argument<object>>(
				HttpContext,
				new DomainObjectExists.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = specType.Result != null ? specType.Result.FullName : null,
					Specification = spec.Result,
				});
		}

		[HttpPut("/exists-generic/{domainObject}")]
		public void ExistsGeneric(string domainObject)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var spec = Utility.ParseGenericSpecification(Serialization, doType, request.Body, request, response);
			if (spec.IsFailure) return;

			Converter.PassThrough<DomainObjectExists, DomainObjectExists.Argument<object>>(
				HttpContext,
				new DomainObjectExists.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = null,
					Specification = spec.Result,
				});
		}

		[HttpGet("/check/{domainObject}")]
		public void Check(string domainObject, [FromQuery(Name = "uri")] string uri)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var doType = Utility.CheckIdentifiable(DomainModel, domainObject, response);
			if (doType.IsFailure) return;

			Converter.PassThrough<CheckDomainObject, CheckDomainObject.Argument>(
				HttpContext,
				new CheckDomainObject.Argument
				{
					Name = doType.Result.FullName,
					Uri = uri
				});
		}

		[HttpPost("submit/{event}")]
		public void Submit(string @event, [FromQuery(Name = "result")] string result = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var domainType = Utility.CheckEvent(DomainModel, @event, response);
			var validatedData = Utility.ParseObject(Serialization, domainType, request.Body, true, Locator, request, response);
			if (validatedData.IsFailure) return;

			Converter.PassThrough<SubmitEvent, SubmitEvent.Argument<object>>(
				HttpContext,
				new SubmitEvent.Argument<object>
				{
					Name = domainType.Result.FullName,
					Data = validatedData.Result,
					ReturnInstance = Utility.ReturnInstance(result, request)
				});
		}

		[HttpPost("queue/{event}")]
		public void Queue(string @event, [FromQuery(Name = "result")] string result = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var domainType = Utility.CheckEvent(DomainModel, @event, response);
			var validatedData = Utility.ParseObject(Serialization, domainType, request.Body, true, Locator, request, response);
			if (validatedData.IsFailure) return;

			Converter.PassThrough<QueueEvent, QueueEvent.Argument<object>>(
				HttpContext,
				new QueueEvent.Argument<object>
				{
					Name = domainType.Result.FullName,
					Data = validatedData.Result
				});
		}

		[HttpPost("submit/{aggregate}/{event}")]
		public void SubmitAggregate(string aggregate, string @event, [FromQuery(Name = "uri")] string uri)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var aggregateType = Utility.CheckAggregateRoot(DomainModel, aggregate, response);
			if (aggregateType.IsFailure) return;
			var domainType = Utility.CheckDomainEvent(DomainModel, aggregateType.Result.FullName + "+" + @event, response);
			var validatedObject = Utility.ParseObject(Serialization, domainType, request.Body, true, Locator, request, response);
			if (validatedObject.IsFailure) return;

			Converter.PassThrough<SubmitAggregateEvent, SubmitAggregateEvent.Argument<object>>(
				HttpContext,
				new SubmitAggregateEvent.Argument<object>
				{
					Name = domainType.Result.FullName,
					Uri = uri,
					Data = validatedObject.Result
				});
		}

		[HttpPost("queue/{aggregate}/{event}")]
		public void QueueAggregate(string aggregate, string @event, [FromQuery(Name = "uri")] string uri)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var aggregateType = Utility.CheckAggregateRoot(DomainModel, aggregate, response);
			if (aggregateType.IsFailure) return;
			var domainType = Utility.CheckDomainEvent(DomainModel, aggregateType.Result.FullName + "+" + @event, response);
			var validatedObject = Utility.ParseObject(Serialization, domainType, request.Body, true, Locator, request, response);
			if (validatedObject.IsFailure) return;

			Converter.PassThrough<QueueAggregateEvent, QueueAggregateEvent.Argument<object>>(
				HttpContext,
				new QueueAggregateEvent.Argument<object>
				{
					Name = domainType.Result.FullName,
					Uri = uri,
					Data = validatedObject.Result
				});
		}
	}
}
