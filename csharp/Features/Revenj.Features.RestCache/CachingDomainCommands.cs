using System;
using System.IO;
using System.ServiceModel;
using Revenj.Api;
using Revenj.DomainPatterns;
using Revenj.Plugins.Rest.Commands;
using Revenj.Serialization;

namespace Revenj.Features.RestCache
{
	[ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
	public class CachingDomainCommands : IDomainCommands
	{
		private readonly IDomainModel DomainModel;
		private readonly DomainCommands DomainCommands;
		private readonly IServiceProvider Locator;
		private readonly IWireSerialization Serialization;

		public CachingDomainCommands(
			IDomainModel domainModel,
			DomainCommands domainCommands,
			IServiceProvider locator,
			IWireSerialization serialization)
		{
			this.DomainModel = domainModel;
			this.DomainCommands = domainCommands;
			this.Locator = locator;
			this.Serialization = serialization;
		}

		public Stream Find(string domainObject, string uris)
		{
			var type = DomainModel.Find(domainObject);
			if (type != null && typeof(IAggregateRoot).IsAssignableFrom(type))
				return CachingService.ReadFromCache(type, (uris ?? string.Empty).Split(','), false, Locator);
			return DomainCommands.Find(domainObject, uris);
		}

		public Stream FindQuery(string domainObject, string uris, string order)
		{
			var type = DomainModel.Find(domainObject);
			if (type != null && typeof(IAggregateRoot).IsAssignableFrom(type))
				return CachingService.ReadFromCache(type, (uris ?? string.Empty).Split(','), order == "match", Locator);
			return DomainCommands.FindQuery(domainObject, uris, order);
		}

		public Stream FindFrom(string domainObject, string order, Stream body)
		{
			var type = DomainModel.Find(domainObject);
			if (type != null && typeof(IAggregateRoot).IsAssignableFrom(type))
			{
				var uris = Serialization.Deserialize<string[]>(body, ThreadContext.Request.ContentType);
				return CachingService.ReadFromCache(type, uris, order == "match", Locator);
			}
			return DomainCommands.FindFrom(domainObject, order, body);
		}

		public Stream SearchWithSpecification(string domainObject, string specification, string limit, string offset, string order, string count, Stream body)
		{
			return DomainCommands.SearchWithSpecification(domainObject, specification, limit, offset, order, count, body);
		}

		public Stream SearchWithSpecificationQuery(string domainObject, string specification, string limit, string offset, string order, string count, Stream body)
		{
			return DomainCommands.SearchWithSpecificationQuery(domainObject, specification, limit, offset, order, count, body);
		}

		public Stream SearchQuery(string domainObject, string specification, string limit, string offset, string order, string count)
		{
			return DomainCommands.SearchQuery(domainObject, specification, limit, offset, order, count);
		}

		public Stream SearchWithGenericSpecification(string domainObject, string limit, string offset, string order, string count, Stream body)
		{
			return DomainCommands.SearchWithGenericSpecification(domainObject, limit, offset, order, count, body);
		}

		public Stream SearchWithGenericSpecificationQuery(string domainObject, string limit, string offset, string order, string count)
		{
			return DomainCommands.SearchWithGenericSpecificationQuery(domainObject, limit, offset, order, count);
		}

		public Stream SearchWithExpression(string domainObject, string limit, string offset, string order, string count, Stream body)
		{
			return DomainCommands.SearchWithExpression(domainObject, limit, offset, order, count, body);
		}

		public Stream CountWithSpecification(string domainObject, string specification, Stream body)
		{
			return DomainCommands.CountWithSpecification(domainObject, specification, body);
		}

		public Stream CountWithSpecificationQuery(string domainObject, string specification, Stream body)
		{
			return DomainCommands.CountWithSpecificationQuery(domainObject, specification, body);
		}

		public Stream CountQuery(string domainObject, string specification)
		{
			return DomainCommands.CountQuery(domainObject, specification);
		}

		public Stream CountWithGenericSpecification(string domainObject, Stream body)
		{
			return DomainCommands.CountWithGenericSpecification(domainObject, body);
		}

		public Stream CountWithGenericSpecificationQuery(string domainObject)
		{
			return DomainCommands.CountWithGenericSpecificationQuery(domainObject);
		}

		public Stream CountWithExpression(string domainObject, Stream body)
		{
			return DomainCommands.CountWithExpression(domainObject, body);
		}

		public Stream SubmitEvent(string domainEvent, string result, Stream body)
		{
			return DomainCommands.SubmitEvent(domainEvent, result, body);
		}

		public Stream QueueEvent(string domainEvent, Stream body)
		{
			return DomainCommands.QueueEvent(domainEvent, body);
		}

		public Stream SubmitAggregateEvent(string aggregate, string domainEvent, string uri, Stream body)
		{
			return DomainCommands.SubmitAggregateEvent(aggregate, domainEvent, uri, body);
		}

		public Stream SubmitAggregateEventQuery(string aggregate, string domainEvent, string uri, Stream body)
		{
			return DomainCommands.SubmitAggregateEventQuery(aggregate, domainEvent, uri, body);
		}

		public Stream QueueAggregateEvent(string aggregate, string domainEvent, string uri, Stream body)
		{
			return DomainCommands.QueueAggregateEvent(aggregate, domainEvent, uri, body);
		}

		public Stream QueueAggregateEventQuery(string aggregate, string domainEvent, string uri, Stream body)
		{
			return DomainCommands.QueueAggregateEventQuery(aggregate, domainEvent, uri, body);
		}

		public Stream ExistsWithSpecification(string domainObject, string specification, Stream body)
		{
			return DomainCommands.ExistsWithSpecification(domainObject, specification, body);
		}

		public Stream ExistsWithSpecificationQuery(string domainObject, string specification, Stream body)
		{
			return DomainCommands.ExistsWithSpecificationQuery(domainObject, specification, body);
		}

		public Stream ExistsQuery(string domainObject, string specification)
		{
			return DomainCommands.ExistsQuery(domainObject, specification);
		}

		public Stream ExistsWithGenericSpecification(string domainObject, Stream body)
		{
			return DomainCommands.ExistsWithGenericSpecification(domainObject, body);
		}

		public Stream ExistsWithGenericSpecificationQuery(string domainObject)
		{
			return DomainCommands.ExistsWithGenericSpecificationQuery(domainObject);
		}

		public Stream ExistsWithExpression(string domainObject, Stream body)
		{
			return DomainCommands.ExistsWithExpression(domainObject, body);
		}

		public Stream Check(string domainObject, string uri)
		{
			return DomainCommands.Check(domainObject, uri);
		}
	}
}
