using System;
using System.IO;
using System.ServiceModel;
using Revenj.DomainPatterns;
using Revenj.Plugins.Rest.Commands;

namespace Revenj.Features.RestCache
{
	[ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
	public class CachingCrudCommands : ICrudCommands
	{
		private readonly IDomainModel DomainModel;
		private readonly CrudCommands CrudComands;
		private readonly IServiceProvider Locator;

		public CachingCrudCommands(
			IDomainModel domainModel,
			CrudCommands crudCommands,
			IServiceProvider locator)
		{
			this.DomainModel = domainModel;
			this.CrudComands = crudCommands;
			this.Locator = locator;
		}

		public Stream Create(string root, string result, Stream body)
		{
			return CrudComands.Create(root, result, body);
		}

		public Stream Read(string domainObject, string uri)
		{
			var type = DomainModel.Find(domainObject);
			if (type != null && typeof(IAggregateRoot).IsAssignableFrom(type))
				return CachingService.ReadFromCache(type, uri, Locator);
			return CrudComands.Read(domainObject, uri);
		}

		public Stream ReadQuery(string domainObject, string uri)
		{
			var type = DomainModel.Find(domainObject);
			if (type != null && typeof(IAggregateRoot).IsAssignableFrom(type))
				return CachingService.ReadFromCache(type, uri, Locator);
			return CrudComands.ReadQuery(domainObject, uri);
		}

		public Stream Update(string root, string uri, Stream body)
		{
			return CrudComands.Update(root, uri, body);
		}

		public Stream UpdateQuery(string root, string uri, string result, Stream body)
		{
			return CrudComands.UpdateQuery(root, uri, result, body);
		}

		public Stream Delete(string root, string uri)
		{
			return CrudComands.Delete(root, uri);
		}

		public Stream DeleteQuery(string root, string uri)
		{
			return CrudComands.DeleteQuery(root, uri);
		}
	}
}
