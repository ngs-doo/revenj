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
	public class CrudMiddleware
	{
		private readonly IServiceProvider Locator;
		private readonly CommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public CrudMiddleware(
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
				return context.Response.WriteError("Domain object not specified", HttpStatusCode.BadRequest);
			var name = path.Substring(prefixLength + 1);
			var type = DomainModel.Find(name);
			if (type == null)
				return Utility.WriteError(context.Response, $"Can't find domain object: {name}", HttpStatusCode.BadRequest);
			if (!typeof(IIdentifiable).IsAssignableFrom(type))
				return Utility.WriteError(context.Response, $"Invalid domain object: {name}", HttpStatusCode.BadRequest);
			switch (context.Request.Method)
			{
				case "POST":
					return Create(type, context);
				case "GET":
					return Read(name, context);
				case "PUT":
					return Update(type, context);
				case "DELETE":
					return Delete(type, context);
				default:
					return Utility.WriteError(context.Response, "Unsuported method type", HttpStatusCode.MethodNotAllowed);
			}
		}

		public Task Create(Type rootType, HttpContext context)
		{
			if (!typeof(IAggregateRoot).IsAssignableFrom(rootType))
				return Utility.WriteError(context.Response, $"{rootType.FullName} is not an aggregate root", HttpStatusCode.BadRequest);
			var request = context.Request;
			var result = request.Query.ContainsKey("result") ? request.Query["result"][0] : null;
			var validatedData = Utility.ParseObject(Serialization, rootType, request.Body, false, Locator, context);
			if (validatedData.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<Create, Create.Argument<object>>(
				context,
				new Create.Argument<object>
				{
					Name = rootType.FullName,
					Data = validatedData.Result,
					ReturnInstance = Utility.ReturnInstance(result, request)
				});
		}

		public Task Read(string domainObject, HttpContext context)
		{
			if (!context.Request.Query.ContainsKey("uri"))
				return Utility.WriteError(context.Response, "Query parameter missing: uri", HttpStatusCode.BadRequest);
			var uri = context.Request.Query["uri"][0];

			return Converter.PassThrough<Read, Read.Argument>(
				context,
				new Read.Argument
				{
					Name = domainObject,
					Uri = uri
				});
		}

		public Task Update(Type rootType, HttpContext context)
		{
			if (!typeof(IAggregateRoot).IsAssignableFrom(rootType))
				return Utility.WriteError(context.Response, $"{rootType.FullName} is not an aggregate root", HttpStatusCode.BadRequest);

			var request = context.Request;
			var response = context.Response;
			if (!context.Request.Query.ContainsKey("uri"))
				return Utility.WriteError(context.Response, "Query parameter missing: uri", HttpStatusCode.BadRequest);
			var uri = context.Request.Query["uri"][0];
			var result = request.Query.ContainsKey("result") ? request.Query["result"][0] : null;

			var validatedData = Utility.ParseObject(Serialization, rootType, request.Body, false, Locator, context);
			if (validatedData.IsFailure) return Task.CompletedTask;

			return Converter.PassThrough<Update, Update.Argument<object>>(
				context,
				new Update.Argument<object>
				{
					Name = rootType.FullName,
					Uri = uri,
					Data = validatedData.Result,
					ReturnInstance = Utility.ReturnInstance(result, request)
				});
		}

		public Task Delete(Type rootType, HttpContext context)
		{
			if (!typeof(IAggregateRoot).IsAssignableFrom(rootType))
				return Utility.WriteError(context.Response, $"{rootType.FullName} is not an aggregate root", HttpStatusCode.BadRequest);
			if (!context.Request.Query.ContainsKey("uri"))
				return Utility.WriteError(context.Response, "Query parameter missing: uri", HttpStatusCode.BadRequest);
			var uri = context.Request.Query["uri"][0];

			return Converter.PassThrough<Delete, Delete.Argument>(
				context,
				new Delete.Argument
				{
					Name = rootType.FullName,
					Uri = uri
				});
		}

	}
}
