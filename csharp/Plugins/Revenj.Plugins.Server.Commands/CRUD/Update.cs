using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Runtime.Serialization;
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
	[ExportMetadata(Metadata.ClassType, typeof(Update))]
	public class Update : IServerCommand
	{
		private static Dictionary<Type, IUpdateCommand> Cache = new Dictionary<Type, IUpdateCommand>();

		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public Update(
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
			public string Uri;
			[DataMember]
			public TFormat Data;
			[DataMember]
			public bool? ReturnInstance;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument<TFormat> { Name = "Module.AggregateRoot", Uri = "1001" });
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer, Type rootType)
		{
			try
			{
				return
					serializer.Serialize(
						new Argument<TFormat>
						{
							Name = rootType.FullName,
							Uri = "1001",
							Data = serializer.Serialize((dynamic)TemporaryResources.CreateRandomObject(rootType))
						});
			}
			catch
			{
				//fallback to simple example since sometimes calculated properties will throw exception during serialization
				return CreateExampleArgument(serializer);
			}
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

			var rootType = DomainModel.Find(argument.Name);
			if (rootType == null)
				return CommandResult<TOutput>.Fail(
					"Couldn't find aggregate root type {0}.".With(argument.Name),
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

			if (!typeof(IAggregateRoot).IsAssignableFrom(rootType))
				return CommandResult<TOutput>.Fail(@"Specified type ({0}) is not an aggregate root. 
Please check your arguments.".With(argument.Name), null);

			if (!Permissions.CanAccess(rootType.FullName, principal))
				return CommandResult<TOutput>.Forbidden(argument.Name);
			if (argument.Uri == null)
				return CommandResult<TOutput>.Fail(
					"Uri to update not specified.",
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output, rootType)));

			if (argument.Data == null)
				return CommandResult<TOutput>.Fail(
					"Data to update not specified.",
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output, rootType)));

			try
			{
				IUpdateCommand command;
				if (!Cache.TryGetValue(rootType, out command))
				{
					var commandType = typeof(UpdateCommand<>).MakeGenericType(rootType);
					command = Activator.CreateInstance(commandType) as IUpdateCommand;
					var newCache = new Dictionary<Type, IUpdateCommand>(Cache);
					newCache[rootType] = command;
					Cache = newCache;
				}
				var result = command.Update(input, output, locator, argument.Uri, argument.ReturnInstance ?? true, argument.Data);

				return CommandResult<TOutput>.Success(result, "Object updated");
			}
			catch (ArgumentException ex)
			{
				return CommandResult<TOutput>.Fail(
					ex.Message,
					ex.GetDetailedExplanation() + @"
Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output, rootType)));
			}
		}

		private interface IUpdateCommand
		{
			TOutput Update<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
				string uri,
				bool returnInstance,
				TInput data);
		}

		private class UpdateCommand<TRoot> : IUpdateCommand
			where TRoot : IAggregateRoot
		{
			public TOutput Update<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
				string uri,
				bool returnInstance,
				TInput data)
			{
				TRoot root;
				try
				{
					root = input.Deserialize<TInput, TRoot>(data, locator);
				}
				catch (Exception ex)
				{
					throw new ArgumentException(
						"Error deserializing: {0}".With(ex.Message),
						new FrameworkException(@"Sent data:
{0}".With(data), ex));
				}
				try
				{
					var repository = locator.Resolve<IPersistableRepository<TRoot>>();
					var original = repository.Find(uri);
					if (original == null)
						throw new ArgumentException("Can't find {0} with uri: {1}.".With(typeof(TRoot).FullName, uri));
					repository.Persist(null, new[] { new KeyValuePair<TRoot, TRoot>(original, root) }, null);
					return returnInstance ? output.Serialize(root) : output.Serialize(root.URI);
				}
				catch (Exception ex)
				{
					string additionalInfo;
					try
					{
						additionalInfo = @"Sent data:
" + input.Serialize(root);
					}
					catch (Exception sex)
					{
						additionalInfo = "Error serializing input: " + sex.Message;
					}
					throw new ArgumentException(
						"Error saving: {0}.".With(ex.Message),
						new FrameworkException(additionalInfo, ex));
				}
			}
		}
	}
}
