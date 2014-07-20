using System;
using System.Collections.Concurrent;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Net;
using System.Runtime.Serialization;
using System.Security;
using NGS;
using NGS.Common;
using NGS.DomainPatterns;
using NGS.Extensibility;
using NGS.Security;
using NGS.Serialization;
using NGS.Utility;
using Revenj.Processing;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(SubmitEvent))]
	public class SubmitEvent : IServerCommand
	{
		private readonly IDomainEventStore EventStore;
		private readonly IServiceLocator Locator;
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public SubmitEvent(
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
			public bool? ReturnInstance;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument<TFormat> { Name = "Module.Event" });
		}

		private static ConcurrentDictionary<Type, ISubmitCommand> Cache = new ConcurrentDictionary<Type, ISubmitCommand>();

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
			if (!typeof(IDomainEvent).IsAssignableFrom(eventType))
				return CommandResult<TOutput>.Fail(@"Specified type ({0}) is not a domain event. 
Please check your arguments.".With(argument.Name), null);

			if (!Permissions.CanAccess(eventType))
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0}.",
						argument.Name);

			try
			{
				ISubmitCommand command;
				if (!Cache.TryGetValue(eventType, out command))
				{
					var commandType = typeof(SubmitEventCommand<>).MakeGenericType(eventType);
					command = Activator.CreateInstance(commandType) as ISubmitCommand;
					Cache.TryAdd(eventType, command);
				}
				var result = command.Submit(input, output, Locator, EventStore, argument.ReturnInstance ?? false, argument.Data);

				return CommandResult<TOutput>.Return(HttpStatusCode.Created, result, "Event stored");
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
				IServiceLocator locator,
				IDomainEventStore domainStore,
				bool returnInstance,
				TInput data);
		}

		private class SubmitEventCommand<TEvent> : ISubmitCommand
			where TEvent : IDomainEvent
		{
			public TOutput Submit<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceLocator locator,
				IDomainEventStore domainStore,
				bool returnInstance,
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
				string uri;
				try
				{
					uri = domainStore.Submit(domainEvent);
				}
				catch (SecurityException) { throw; }
				catch (Exception ex)
				{
					throw new ArgumentException(
						ex.Message,
						data == null
							? new FrameworkException("Error while submitting event: {0}. Data not sent.".With(ex.Message), ex)
							: new FrameworkException(@"Error while submitting event: {0}. Sent data: 
{1}".With(ex.Message, input.Serialize(domainEvent)), ex));
				}
				try
				{
					if (returnInstance)
						return output.Serialize(domainEvent);
					else
						return output.Serialize(uri);
				}
				catch (Exception ex)
				{
					throw new ArgumentException(
						ex.Message,
						new FrameworkException(@"Error serializing result: " + ex.Message, ex));
				}
			}
		}
	}
}
