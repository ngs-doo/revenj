using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Net;
using System.Runtime.Serialization;
using System.Security;
using System.Security.Principal;
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
	[ExportMetadata(Metadata.ClassType, typeof(SubmitAggregateEvent))]
	public class SubmitAggregateEvent : IServerCommand
	{
		private static Dictionary<Type, ISubmitCommand> Cache = new Dictionary<Type, ISubmitCommand>();

		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public SubmitAggregateEvent(
			IDomainModel domainModel,
			IPermissionManager permissions)
		{
			Contract.Requires(domainModel != null);
			Contract.Requires(permissions != null);

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

		public ICommandResult<TOutput> Execute<TInput, TOutput>(
			IServiceProvider locator,
			ISerialization<TInput> input,
			ISerialization<TOutput> output,
			IPrincipal principal,
			TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument<TInput>, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;
			var argument = either.Argument;

			var eventType = DomainModel.Find(argument.Name);
			if (eventType == null)
			{
				return
					CommandResult<TOutput>.Fail(
						"Couldn't find event type " + argument.Name,
						@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));
			}
			var aggregateInterface = eventType.GetInterfaces().FirstOrDefault(it => it.IsGenericType && it.GetGenericTypeDefinition() == typeof(IDomainEvent<>));
			var aggregateType = aggregateInterface != null ? aggregateInterface.GetGenericArguments()[0] : null;
			if (aggregateType == null)
			{
				return
					CommandResult<TOutput>.Fail(
						"{0} does not implement IDomainEvent<TAggregate>.".With(eventType.FullName),
						@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));
			}
			if (!Permissions.CanAccess(eventType.FullName, principal))
				return CommandResult<TOutput>.Forbidden(eventType.FullName);
			if (!Permissions.CanAccess(aggregateType.FullName, principal))
				return CommandResult<TOutput>.Forbidden(aggregateType.FullName);
			try
			{
				ISubmitCommand command;
				if (!Cache.TryGetValue(eventType, out command))
				{
					var commandType = typeof(SubmitEventCommand<,>).MakeGenericType(eventType, aggregateType);
					command = Activator.CreateInstance(commandType) as ISubmitCommand;
					var newCache = new Dictionary<Type, ISubmitCommand>(Cache);
					newCache[eventType] = command;
					Cache = newCache;
				}
				var result = command.Submit(input, output, locator, argument.Uri, argument.Data);

				return CommandResult<TOutput>.Return(HttpStatusCode.Created, result, "Event applied");
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

		private interface ISubmitCommand
		{
			TOutput Submit<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
				string uri,
				TInput data);
		}

		private class SubmitEventCommand<TEvent, TAggregate> : ISubmitCommand
			where TEvent : IDomainEvent<TAggregate>
			where TAggregate : class, IAggregateRoot
		{
			public TOutput Submit<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
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
				var domainStore = locator.Resolve<IDomainEventStore>();
				try { domainStore.Submit(domainEvent); }
				catch (SecurityException) { throw; }
				catch (Exception ex)
				{
					throw new ArgumentException(
						ex.Message,
						data == null
							? new FrameworkException("Error while submitting event: {0}. Data not sent.".With(ex.Message), ex)
							: new FrameworkException(@"Error while submitting event: {0}. Sent data: 
{1}".With(ex.Message, output.Serialize(domainEvent)), ex));
				}
				return output.Serialize(aggregate);
			}
		}
	}
}