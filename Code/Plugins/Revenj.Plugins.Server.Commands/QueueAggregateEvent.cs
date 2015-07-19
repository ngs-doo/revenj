using System;
using System.Collections.Concurrent;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Net;
using System.Runtime.Serialization;
using System.Security;
using Revenj.Common;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(QueueAggregateEvent))]
	public class QueueAggregateEvent : IServerCommand
	{
		private static ConcurrentDictionary<Type, IQueueCommand> Cache = new ConcurrentDictionary<Type, IQueueCommand>(1, 127);

		private readonly IDomainEventStore EventStore;
		private readonly IServiceLocator Locator;
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public QueueAggregateEvent(
			IDomainEventStore eventStore,
			IServiceLocator locator,
			IDomainModel domainModel,
			IPermissionManager permissions)
		{
			Contract.Requires(eventStore != null);
			Contract.Requires(locator != null);
			Contract.Requires(domainModel != null);
			Contract.Requires(permissions != null);

			this.EventStore = eventStore;
			this.Locator = locator;
			this.DomainModel = domainModel;
			this.Permissions = permissions;
		}

		[DataContract(Namespace = "")]
		public class Argument<TFormat>
		{
			[DataMember]
			public string Name;
			[DataMember]
			public TFormat Data;
			[DataMember]
			public string Uri;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument<TFormat> { Name = "Module.Aggregate.Event", Uri = "1002" });
		}

		public ICommandResult<TOutput> Execute<TInput, TOutput>(ISerialization<TInput> input, ISerialization<TOutput> output, TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument<TInput>, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;
			var argument = either.Argument;

			var eventType = DomainModel.Find(argument.Name);
			if (eventType == null)
				return
					CommandResult<TOutput>.Fail(
						"Couldn't find event type {0}.".With(argument.Name),
						@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

			var aggregateInterface = eventType.GetInterfaces().FirstOrDefault(it => it.IsGenericType && it.GetGenericTypeDefinition() == typeof(IDomainEvent<>));
			var aggregateType = aggregateInterface != null ? aggregateInterface.GetGenericArguments()[0] : null;
			if (aggregateType == null)
				return
					CommandResult<TOutput>.Fail(
						"{0} does not implement IDomainEvent<TAggregate>.".With(eventType.FullName),
						@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

			if (!Permissions.CanAccess(eventType))
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0}.",
						eventType.FullName);

			if (!Permissions.CanAccess(aggregateType))
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0}.",
						aggregateType.FullName);

			try
			{
				IQueueCommand command;
				if (!Cache.TryGetValue(eventType, out command))
				{
					var commandType = typeof(QueueEventCommand<,>).MakeGenericType(eventType, aggregateType);
					command = Activator.CreateInstance(commandType) as IQueueCommand;
					Cache.TryAdd(eventType, command);
				}
				var result = command.Queue(input, output, Locator, EventStore, argument.Uri, argument.Data);

				return CommandResult<TOutput>.Return(HttpStatusCode.Accepted, result, "Event queued");
			}
			catch (ArgumentException ex)
			{
				return CommandResult<TOutput>.Fail(
					ex.Message,
					ex.GetDetailedExplanation() + @"
Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));
			}
		}

		private interface IQueueCommand
		{
			TOutput Queue<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceLocator locator,
				IDomainEventStore domainStore,
				string uri,
				TInput data);
		}

		private class QueueEventCommand<TEvent, TAggregate> : IQueueCommand
			where TEvent : IDomainEvent<TAggregate>
			where TAggregate : class, IAggregateRoot
		{
			public TOutput Queue<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceLocator locator,
				IDomainEventStore domainStore,
				string uri,
				TInput data)
			{
				TEvent domainEvent;
				try
				{
					domainEvent = data != null ? input.Deserialize<TInput, TEvent>(data, locator) : Activator.CreateInstance<TEvent>();
				}
				catch (Exception ex)
				{
					throw new ArgumentException("Error deserializing domain event.", ex);
				}
				var repository = locator.Resolve<IRepository<TAggregate>>();
				var aggregate = repository.Find(uri);
				if (aggregate == null)
					throw new ArgumentException("Can't find aggregate with Uri: {0}".With(uri));
				try { domainEvent.Apply(aggregate); }
				catch (SecurityException) { throw; }
				catch (Exception ex)
				{
					throw new ArgumentException(ex.Message, ex);
				}
				try { domainStore.Queue(domainEvent); }
				catch (SecurityException) { throw; }
				catch (Exception ex)
				{
					throw new ArgumentException(
						ex.Message,
						data == null
							? new FrameworkException("Error while Queueting event: {0}. Data not sent.".With(ex.Message), ex)
							: new FrameworkException(@"Error while Queueting event: {0}. Sent data: 
{1}".With(ex.Message, output.Serialize(domainEvent)), ex));
				}
				return output.Serialize(aggregate);
			}
		}
	}
}