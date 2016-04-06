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
	public class CrudCommands : ICrudCommands
	{
		private readonly IServiceProvider Locator;
		private readonly ICommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public CrudCommands(
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

		public Stream Create(string root, string result, Stream body)
		{
			var rootType = Utility.CheckAggregateRoot(DomainModel, root);
			var validatedData = Utility.ParseObject(Serialization, rootType, body, false, Locator);
			if (validatedData.IsFailure) return validatedData.Error;
			return
				Converter.PassThrough<Create, Create.Argument<object>>(
					new Create.Argument<object>
					{
						Name = rootType.Result.FullName,
						Data = validatedData.Result,
						ReturnInstance = Utility.ReturnInstance(result, ThreadContext.Request)
					});
		}

		public Stream Read(string domainObject, string uri)
		{
			var type = Utility.CheckIdentifiable(DomainModel, domainObject);
			if (type.IsFailure) return type.Error;
			return
				Converter.PassThrough<Read, Read.Argument>(
					new Read.Argument
					{
						Name = domainObject,
						Uri = uri
					});
		}

		public Stream ReadQuery(string domainObject, string uri)
		{
			return Read(domainObject, uri);
		}

		public Stream Update(string root, string uri, Stream body)
		{
			return UpdateQuery(root, uri, null, body);
		}

		public Stream UpdateQuery(string root, string uri, string result, Stream body)
		{
			var rootType = Utility.CheckAggregateRoot(DomainModel, root);
			var validatedData = Utility.ParseObject(Serialization, rootType, body, false, Locator);
			if (validatedData.IsFailure) return validatedData.Error;
			return
				Converter.PassThrough<Update, Update.Argument<object>>(
					new Update.Argument<object>
					{
						Name = rootType.Result.FullName,
						Uri = uri,
						Data = validatedData.Result,
						ReturnInstance = Utility.ReturnInstance(result, ThreadContext.Request)
					});
		}

		public Stream Delete(string root, string uri)
		{
			var type = Utility.CheckAggregateRoot(DomainModel, root);
			if (type.IsFailure) return type.Error;
			return
				Converter.PassThrough<Delete, Delete.Argument>(
					new Delete.Argument
					{
						Name = root,
						Uri = uri
					});
		}

		public Stream DeleteQuery(string root, string uri)
		{
			return Delete(root, uri);
		}
	}
}
