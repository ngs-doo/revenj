using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Linq;
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
	[ExportMetadata(Metadata.ClassType, typeof(PersistAggregateRoot))]
	public class PersistAggregateRoot : IServerCommand
	{
		private static Dictionary<Type, IPersistCommand> Cache = new Dictionary<Type, IPersistCommand>();

		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public PersistAggregateRoot(
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
			public string RootName;
			[DataMember]
			public TFormat ToInsert;
			[DataMember]
			public TFormat ToUpdate;
			[DataMember]
			public TFormat ToDelete;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument<TFormat> { RootName = "Module.AggregateRoot" });
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer, Type rootType)
		{
			try
			{
				var array = Array.CreateInstance(rootType, 1);
				var element = TemporaryResources.CreateRandomObject(rootType);
				array.SetValue(element, 0);
				return serializer.Serialize(new Argument<TFormat> { RootName = rootType.FullName, ToInsert = serializer.Serialize((dynamic)array) });
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

			var rootType = DomainModel.Find(argument.RootName);
			if (rootType == null)
				return CommandResult<TOutput>.Fail("Couldn't find root type {0}.".With(argument.RootName), @"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

			if (!typeof(IAggregateRoot).IsAssignableFrom(rootType))
				return CommandResult<TOutput>.Fail(@"Specified type ({0}) is not an aggregate root. 
Please check your arguments.".With(argument.RootName), null);

			if (!Permissions.CanAccess(rootType.FullName, principal))
				return CommandResult<TOutput>.Forbidden(argument.RootName);
			if (argument.ToInsert == null && argument.ToUpdate == null && argument.ToDelete == null)
				return CommandResult<TOutput>.Fail("Data to persist not specified.", @"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output, rootType)));

			try
			{
				IPersistCommand command;
				if (!Cache.TryGetValue(rootType, out command))
				{
					var commandType = typeof(PersistAggregateRootCommand<>).MakeGenericType(rootType);
					command = Activator.CreateInstance(commandType) as IPersistCommand;
					var newCache = new Dictionary<Type, IPersistCommand>(Cache);
					newCache[rootType] = command;
					Cache = newCache;
				}
				var uris = command.Persist(input, locator, argument.ToInsert, argument.ToUpdate, argument.ToDelete);

				return CommandResult<TOutput>.Success(output.Serialize(uris), "Data persisted");
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

		private interface IPersistCommand
		{
			string[] Persist<TFormat>(
				ISerialization<TFormat> serializer,
				IServiceProvider locator,
				TFormat toInsert,
				TFormat toUpdate,
				TFormat toDelete);
		}

		private class PersistAggregateRootCommand<TRoot> : IPersistCommand
			where TRoot : IAggregateRoot, new()
		{
			public string[] Persist<TFormat>(
				ISerialization<TFormat> serializer,
				IServiceProvider locator,
				TFormat toInsert,
				TFormat toUpdate,
				TFormat toDelete)
			{
				var repository = locator.Resolve<IPersistableRepository<TRoot>>();
				var insertData = toInsert != null ? serializer.Deserialize<TFormat, TRoot[]>(toInsert, locator) : null;
				var updateData = toUpdate != null ? serializer.Deserialize<TFormat, KeyValuePair<TRoot, TRoot>[]>(toUpdate, locator) : null;
				//TODO support old update format
				if (toUpdate != null && updateData != null && updateData.Length == 0)
				{
					var updateValues = serializer.Deserialize<TFormat, TRoot[]>(toUpdate, locator);
					if (updateValues != null && updateValues.Length > 0)
						updateData = updateValues.Select(it => new KeyValuePair<TRoot, TRoot>(default(TRoot), it)).ToArray();
				}
				var deleteData = toDelete != null ? serializer.Deserialize<TFormat, TRoot[]>(toDelete, locator) : null;

				if ((insertData == null || insertData.Length == 0)
					&& (updateData == null || updateData.Length == 0)
					&& (deleteData == null || deleteData.Length == 0))
					throw new ArgumentException(
						"Data not sent or deserialized unsuccessfully.",
						new FrameworkException(@"Example:
" + serializer.Serialize(
			new Argument<TFormat>
			{
				RootName = typeof(TRoot).FullName,
				ToInsert = serializer.Serialize(new TRoot[] { new TRoot() })
			})));
				try
				{
					return repository.Persist(insertData, updateData, deleteData);
				}
				catch (FrameworkException ex)
				{
					throw new ArgumentException(ex.Message, ex);
				}
				catch (Exception ex)
				{
					throw new ArgumentException(
						"Error persisting: {0}.".With(ex.Message),
						new FrameworkException(
							@"{0}{1}{2}".With(
								FormatData(serializer, "Insert", insertData),
								FormatData(serializer, "Update", updateData),
								FormatData(serializer, "Delete", deleteData)),
							ex));
				}
			}

			private static string FormatData<T, TFormat>(ISerialization<TFormat> serializer, string text, T[] data)
			{
				return data != null && data.Length > 0
					? "{0}{1} (first two): {2}".With(Environment.NewLine, text, serializer.Serialize(data.Take(2).ToArray()))
					: string.Empty;
			}
		}
	}
}
