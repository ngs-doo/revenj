using Microsoft.AspNetCore.Http;
using Revenj.AspNetCore;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;
using System;
using System.Net;
using System.Threading.Tasks;

namespace Revenj.Plugins.AspNetCore.Commands
{
	public class DomainMiddleware
	{
		private static readonly AdditionalCommand[] EmptyCommands = new AdditionalCommand[0];

		private readonly IServiceProvider Locator;
		private readonly CommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public DomainMiddleware(
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
					if (name.StartsWith("find/", StringComparison.OrdinalIgnoreCase))
						return Find(name.Substring(5), context);
					if (name.StartsWith("search/", StringComparison.OrdinalIgnoreCase))
						return SearchBody(name.Substring(7), context);
					if (name.StartsWith("search-generic/", StringComparison.OrdinalIgnoreCase))
						return SearchGeneric(name.Substring(15), context);
					if (name.StartsWith("count/", StringComparison.OrdinalIgnoreCase))
						return Count(name.Substring(6), context);
					if (name.StartsWith("count-generic/", StringComparison.OrdinalIgnoreCase))
						return CountGeneric(name.Substring(14), context);
					if (name.StartsWith("exists/", StringComparison.OrdinalIgnoreCase))
						return ExistsFromBody(name.Substring(7), context);
					if (name.StartsWith("exists-generic/", StringComparison.OrdinalIgnoreCase))
						return ExistsGeneric(name.Substring(15), context);

					return Utility.WriteError(context.Response, "Unknown route", HttpStatusCode.NotFound);
				case "GET":
					if (name.StartsWith("search/", StringComparison.OrdinalIgnoreCase))
						return SearchFromGet(name.Substring(7), context);
					if (name.StartsWith("count/", StringComparison.OrdinalIgnoreCase))
						return CountFromGet(name.Substring(6), context);
					if (name.StartsWith("exists/", StringComparison.OrdinalIgnoreCase))
						return ExistsFromGet(name.Substring(6), context);
					if (name.StartsWith("check/", StringComparison.OrdinalIgnoreCase))
						return Check(name.Substring(6), context);

					return Utility.WriteError(context.Response, "Unknown route", HttpStatusCode.NotFound);
				case "POST":
					if (name.StartsWith("submit/", StringComparison.OrdinalIgnoreCase))
					{
						var ind = name.IndexOf('/', 7);
						if (ind == -1)
							return Submit(name.Substring(7), context);
						if (ind != name.Length)
							return SubmitAggregate(name.Substring(7, ind - 7), name.Substring(ind + 1), context);
					}
					if (name.StartsWith("queue/", StringComparison.OrdinalIgnoreCase))
					{
						var ind = name.IndexOf('/', 6);
						if (ind == -1)
							return Queue(name.Substring(6), context);
						if (ind != name.Length)
							return QueueAggregate(name.Substring(6, ind - 6), name.Substring(ind + 1), context);
					}

					return Utility.WriteError(context.Response, "Unknown route", HttpStatusCode.NotFound);
				default:
					return Utility.WriteError(context.Response, "Unsuported method type", HttpStatusCode.MethodNotAllowed);
			}
		}

		public Task Find(string domainObject, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var order = request.Query.ContainsKey("order") ? request.Query["order"][0] : null;

			var type = Utility.CheckIdentifiable(DomainModel, domainObject, response);
			if (type.IsFailure) return Task.CompletedTask;
			var uris = Serialization.TryDeserialize<string[]>(request, response);
			if (uris.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<GetDomainObject, GetDomainObject.Argument>(
				context,
				new GetDomainObject.Argument
				{
					Name = domainObject,
					Uri = uris.Result,
					MatchOrder = order == "match"
				});
		}

		public Task SearchBody(string domainObject, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification, response);
			var spec = Utility.ParseObject(Serialization, specType, request.Body, true, Locator, context);
			return Search(domainObject, doType, specType, spec, context);
		}

		public Task SearchFromGet(string domainObject, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification, response);
			var spec = Utility.ObjectFromQuery(specType, context);
			return Search(domainObject, doType, specType, spec, context);
		}

		private Task Search(string domainObject, Try<Type> doType, Try<Type> specType, Try<object> spec, HttpContext context)
		{
			if (spec.IsFailure) return Task.CompletedTask;
			var request = context.Request;
			var response = context.Response;
			int? limit, offset;
			Utility.ParseLimitOffset(request.Query, out limit, out offset);
			var order = request.Query.ContainsKey("order") ? request.Query["order"][0] : null;
			var count = request.Query.ContainsKey("count") ? request.Query["count"][0] : null;
			var ordDict = Utility.ParseOrder(order);

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
			return Converter.PassThrough<SearchDomainObject, SearchDomainObject.Argument<object>>(
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
				context,
				additionalCommands);
		}

		public Task SearchGeneric(string domainObject, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var spec = Utility.ParseGenericSpecification(Serialization, doType, context);
			int? limit, offset;
			Utility.ParseLimitOffset(request.Query, out limit, out offset);
			var order = request.Query.ContainsKey("order") ? request.Query["order"][0] : null;
			var count = request.Query.ContainsKey("count") ? request.Query["count"][0] : null;
			var ordDict = Utility.ParseOrder(order);
			if (spec.IsFailure) return Task.CompletedTask;

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
			return Converter.PassThrough<SearchDomainObject, SearchDomainObject.Argument<object>>(
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
				context,
				additionalCommands);
		}

		public Task Count(string domainObject, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification, response);
			var spec = Utility.ParseObject(Serialization, specType, request.Body, true, Locator, context);
			if (spec.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<CountDomainObject, CountDomainObject.Argument<object>>(
				context,
				new CountDomainObject.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = specType.Result != null ? specType.Result.FullName : null,
					Specification = spec.Result,
				});
		}

		public Task CountFromGet(string domainObject, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification, response);
			var spec = Utility.ObjectFromQuery(specType, context);
			if (spec.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<CountDomainObject, CountDomainObject.Argument<object>>(
				context,
				new CountDomainObject.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = null,
					Specification = spec.Result,
				});
		}

		public Task CountGeneric(string domainObject, HttpContext context)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, context.Response);
			var spec = Utility.ParseGenericSpecification(Serialization, doType, context);
			if (spec.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<CountDomainObject, CountDomainObject.Argument<object>>(
				context,
				new CountDomainObject.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = null,
					Specification = spec.Result,
				});
		}

		public Task ExistsFromBody(string domainObject, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, response);
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification, response);
			var spec = Utility.ParseObject(Serialization, specType, request.Body, true, Locator, context);
			if (spec.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<DomainObjectExists, DomainObjectExists.Argument<object>>(
				context,
				new DomainObjectExists.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = specType.Result != null ? specType.Result.FullName : null,
					Specification = spec.Result,
				});
		}

		public Task ExistsFromGet(string domainObject, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var specification = request.Query.ContainsKey("specification") ? request.Query["specification"][0] : null;
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, context.Response);
			var specType = Utility.CheckDomainObject(DomainModel, doType, specification, response);
			var spec = Utility.ObjectFromQuery(specType, context);
			if (spec.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<DomainObjectExists, DomainObjectExists.Argument<object>>(
				context,
				new DomainObjectExists.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = null,
					Specification = spec.Result,
				});
		}

		public Task ExistsGeneric(string domainObject, HttpContext context)
		{
			var doType = Utility.CheckDomainObject(DomainModel, domainObject, context.Response);
			var spec = Serialization.ParseGenericSpecification(doType, context);
			if (spec.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<DomainObjectExists, DomainObjectExists.Argument<object>>(
				context,
				new DomainObjectExists.Argument<object>
				{
					Name = doType.Result.FullName,
					SpecificationName = null,
					Specification = spec.Result,
				});
		}

		public Task Check(string domainObject, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			if (!request.Query.ContainsKey("uri"))
				return Utility.WriteError(response, "Query parameter missing: uri", HttpStatusCode.BadRequest);
			var uri = request.Query["uri"][0];

			var doType = Utility.CheckIdentifiable(DomainModel, domainObject, response);
			if (doType.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<CheckDomainObject, CheckDomainObject.Argument>(
				context,
				new CheckDomainObject.Argument
				{
					Name = doType.Result.FullName,
					Uri = uri
				});
		}

		public Task Submit(string @event, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var domainType = Utility.CheckEvent(DomainModel, @event, response);
			var validatedData = Utility.ParseObject(Serialization, domainType, request.Body, true, Locator, context);
			if (validatedData.IsFailure) return Task.CompletedTask;
			var result = request.Query.ContainsKey("result") ? request.Query["result"][0] : null;

			return Converter.PassThrough<SubmitEvent, SubmitEvent.Argument<object>>(
				context,
				new SubmitEvent.Argument<object>
				{
					Name = domainType.Result.FullName,
					Data = validatedData.Result,
					ReturnInstance = Utility.ReturnInstance(result, request)
				});
		}

		public Task Queue(string @event, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			var domainType = Utility.CheckEvent(DomainModel, @event, response);
			var validatedData = Utility.ParseObject(Serialization, domainType, request.Body, true, Locator, context);
			if (validatedData.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<QueueEvent, QueueEvent.Argument<object>>(
				context,
				new QueueEvent.Argument<object>
				{
					Name = domainType.Result.FullName,
					Data = validatedData.Result
				});
		}

		public Task SubmitAggregate(string aggregate, string @event, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			if (!request.Query.ContainsKey("uri"))
				return Utility.WriteError(response, "Query parameter missing: uri", HttpStatusCode.BadRequest);
			var uri = request.Query["uri"][0];

			var aggregateType = Utility.CheckAggregateRoot(DomainModel, aggregate, response);
			if (aggregateType.IsFailure) return Task.CompletedTask;
			var domainType = Utility.CheckDomainEvent(DomainModel, aggregateType.Result.FullName + "+" + @event, response);
			var validatedObject = Utility.ParseObject(Serialization, domainType, request.Body, true, Locator, context);
			if (validatedObject.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<SubmitAggregateEvent, SubmitAggregateEvent.Argument<object>>(
				context,
				new SubmitAggregateEvent.Argument<object>
				{
					Name = domainType.Result.FullName,
					Uri = uri,
					Data = validatedObject.Result
				});
		}

		public Task QueueAggregate(string aggregate, string @event, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			if (!request.Query.ContainsKey("uri"))
				return Utility.WriteError(response, "Query parameter missing: uri", HttpStatusCode.BadRequest);
			var uri = request.Query["uri"][0];
			var aggregateType = Utility.CheckAggregateRoot(DomainModel, aggregate, response);
			if (aggregateType.IsFailure) return Task.CompletedTask;
			var domainType = Utility.CheckDomainEvent(DomainModel, aggregateType.Result.FullName + "+" + @event, response);
			var validatedObject = Utility.ParseObject(Serialization, domainType, request.Body, true, Locator, context);
			if (validatedObject.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<QueueAggregateEvent, QueueAggregateEvent.Argument<object>>(
				context,
				new QueueAggregateEvent.Argument<object>
				{
					Name = domainType.Result.FullName,
					Uri = uri,
					Data = validatedObject.Result
				});
		}
	}
}
