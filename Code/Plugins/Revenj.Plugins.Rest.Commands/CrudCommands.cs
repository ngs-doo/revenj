using System.IO;
using System.ServiceModel;
using NGS.DomainPatterns;
using NGS.Serialization;
using Revenj.Api;
using Revenj.Plugins.Server.Commands;

namespace Revenj.Plugins.Rest.Commands
{
	[ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
	public class CrudCommands : ICrudCommands
	{
		private readonly IServiceLocator Locator;
		private readonly ICommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public CrudCommands(
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

		public Stream Create(string root, Stream body)
		{
			var rootType = Utility.CheckAggregateRoot(DomainModel, root);
			var validatedData = Utility.ParseObject(Serialization, rootType, body, false, Locator);
			return
				Converter.PassThrough<Create, Create.Argument<object>>(
					new Create.Argument<object>
					{
						Name = rootType.FullName,
						Data = validatedData
					});
		}

		public Stream Read(string domainObject, string uri)
		{
			Utility.CheckIdentifiable(DomainModel, domainObject);
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
			var rootType = Utility.CheckAggregateRoot(DomainModel, root);
			var validatedData = Utility.ParseObject(Serialization, rootType, body, false, Locator);
			return
				Converter.PassThrough<Update, Update.Argument<object>>(
					new Update.Argument<object>
					{
						Name = rootType.FullName,
						Uri = uri,
						Data = validatedData
					});
		}

		public Stream UpdateQuery(string root, string uri, Stream body)
		{
			return Update(root, uri, body);
		}

		public Stream Delete(string root, string uri)
		{
			Utility.CheckAggregateRoot(DomainModel, root);
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
