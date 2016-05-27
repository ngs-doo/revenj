using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
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
	[ExportMetadata(Metadata.ClassType, typeof(QueueEvent))]
	public class QueueEvent : IReadOnlyServerCommand
	{
		private static Dictionary<Type, IQueueCommand> Cache = new Dictionary<Type, IQueueCommand>();

		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;
		private readonly IDataContext DataContext;

		public QueueEvent(
			IDomainModel domainModel,
			IPermissionManager permissions,
			IDataContext dataContext)
		{
			this.DomainModel = domainModel;
			this.Permissions = permissions;
			this.DataContext = dataContext;
		}

		[DataContract(Namespace = "")]
		public class Argument<TFormat>
		{
			[DataMember]
			public string Name;
			[DataMember]
			public TFormat Data;
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
			if (!typeof(IDomainEvent).IsAssignableFrom(eventType))
			{
				return CommandResult<TOutput>.Fail(@"Specified type ({0}) is not a domain event. 
Please check your arguments.".With(argument.Name), null);
			}
			if (!Permissions.CanAccess(eventType.FullName, principal))
				return CommandResult<TOutput>.Forbidden(argument.Name);
			try
			{
				IQueueCommand command;
				if (!Cache.TryGetValue(eventType, out command))
				{
					var commandType = typeof(QueueEventCommand<>).MakeGenericType(eventType);
					command = Activator.CreateInstance(commandType) as IQueueCommand;
					var newCache = new Dictionary<Type, IQueueCommand>(Cache);
					newCache[eventType] = command;
					Cache = newCache;
				}
				command.Queue(input, locator, DataContext, argument.Data);

				return CommandResult<TOutput>.Return(HttpStatusCode.Accepted, default(TOutput), "Event queued");
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
			void Queue<TInput>(
				ISerialization<TInput> input,
				IServiceProvider locator,
				IDataContext context,
				TInput data);
		}

		private class QueueEventCommand<TEvent> : IQueueCommand
			where TEvent : IDomainEvent
		{
			public void Queue<TInput>(
				ISerialization<TInput> input,
				IServiceProvider locator,
				IDataContext context,
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
				try
				{
					context.Queue(domainEvent);
				}
				catch (SecurityException) { throw; }
				catch (Exception ex)
				{
					throw new ArgumentException(
						ex.Message,
						data == null
							? new FrameworkException("Error while queuing event: {0}. Data not sent.".With(ex.Message), ex)
							: new FrameworkException(@"Error while queuing event: {0}. Sent data: 
{1}".With(ex.Message, input.Serialize(domainEvent)), ex));
				}
			}
		}
	}
}
