using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
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
	[ExportMetadata(Metadata.ClassType, typeof(SubmitEvent))]
	public class SubmitEvent : IServerCommand
	{
		private static Dictionary<Type, ISubmitCommand> Cache = new Dictionary<Type, ISubmitCommand>();

		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public SubmitEvent(
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
			public bool? ReturnInstance;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument<TFormat> { Name = "Module.Event" });
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
						"Couldn't find event type {0}.".With(argument.Name),
						@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));
			}
			if (!typeof(IEvent).IsAssignableFrom(eventType))
			{
				return CommandResult<TOutput>.Fail(@"Specified type ({0}) is not an event. 
Please check your arguments.".With(argument.Name), null);
			}
			if (!Permissions.CanAccess(eventType.FullName, principal))
				return CommandResult<TOutput>.Forbidden(argument.Name);
			try
			{
				ISubmitCommand command;
				if (!Cache.TryGetValue(eventType, out command))
				{
					var commandType = typeof(SubmitEventCommand<>).MakeGenericType(eventType);
					command = Activator.CreateInstance(commandType) as ISubmitCommand;
					var newCache = new Dictionary<Type, ISubmitCommand>(Cache);
					newCache[eventType] = command;
					Cache = newCache;
				}
				return command.Submit(input, output, locator, argument.ReturnInstance ?? false, argument.Data);
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
			CommandResult<TOutput> Submit<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
				bool returnInstance,
				TInput data);
		}

		private class SubmitEventCommand<TEvent> : ISubmitCommand
			where TEvent : IEvent
		{
			public CommandResult<TOutput> Submit<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
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
				var domainStore = locator.Resolve<IEventStore>();
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
					var command = domainEvent as ICommand;
					Dictionary<string, List<string>> errors;
					if (command != null)
					{
						errors = command.GetValidationErrors();
						if (errors.Count != 0)
							return CommandResult<TOutput>.Return(
								HttpStatusCode.BadRequest,
								output.Serialize(errors),
								"Validation errors");
						return CommandResult<TOutput>.Return(
							HttpStatusCode.OK,
							returnInstance ? output.Serialize(domainEvent) : output.Serialize(uri),
							"Command processed");
					}
					return CommandResult<TOutput>.Return(
						HttpStatusCode.Created,
						returnInstance ? output.Serialize(domainEvent) : output.Serialize(uri),
						"Event stored");
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
