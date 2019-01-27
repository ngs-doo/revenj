using Microsoft.AspNetCore.Mvc;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;
using System;

namespace Revenj.Plugins.AspNetCore.Commands
{
	[Route("Crud.svc")]
	public class CrudController : ControllerBase
	{
		private readonly IServiceProvider Locator;
		private readonly CommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public CrudController(
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

		[HttpPost("{root}")]
		public void Create(string root, [FromQuery(Name = "result")] string result = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var rootType = Utility.CheckAggregateRoot(DomainModel, root, response);
			var validatedData = Utility.ParseObject(Serialization, rootType, request.Body, false, Locator, request, response);
			if (validatedData.IsFailure) return;

			Converter.PassThrough<Create, Create.Argument<object>>(
				HttpContext,
				new Create.Argument<object>
				{
					Name = rootType.Result.FullName,
					Data = validatedData.Result,
					ReturnInstance = Utility.ReturnInstance(result, request)
				});
		}

		[HttpGet("{domainObject}")]
		public void Read(string domainObject, [FromQuery(Name = "uri")] string uri)
		{
			var type = Utility.CheckIdentifiable(DomainModel, domainObject, HttpContext.Response);
			if (type.IsFailure) return;

			Converter.PassThrough<Read, Read.Argument>(
				HttpContext,
				new Read.Argument
				{
					Name = domainObject,
					Uri = uri
				});
		}

		[HttpPut("{root}")]
		public void Update(string root, [FromQuery(Name = "uri")] string uri, [FromQuery(Name = "result")] string result = null)
		{
			var request = HttpContext.Request;
			var response = HttpContext.Response;
			var rootType = Utility.CheckAggregateRoot(DomainModel, root, response);
			var validatedData = Utility.ParseObject(Serialization, rootType, request.Body, false, Locator, request, response);
			if (validatedData.IsFailure) return;

			Converter.PassThrough<Update, Update.Argument<object>>(
				HttpContext,
				new Update.Argument<object>
				{
					Name = rootType.Result.FullName,
					Uri = uri,
					Data = validatedData.Result,
					ReturnInstance = Utility.ReturnInstance(result, request)
				});
		}

		[HttpDelete("{root}")]
		public void Delete(string root, [FromQuery(Name = "uri")] string uri)
		{
			var rootType = Utility.CheckAggregateRoot(DomainModel, root, HttpContext.Response);
			if (rootType.IsFailure) return;

			Converter.PassThrough<Delete, Delete.Argument>(
				HttpContext,
				new Delete.Argument
				{
					Name = rootType.Result.FullName,
					Uri = uri
				});
		}

	}
}
