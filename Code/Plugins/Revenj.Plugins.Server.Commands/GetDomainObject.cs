using System;
using System.Collections.Concurrent;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Runtime.Serialization;
using System.Security;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(GetDomainObject))]
	public class GetDomainObject : IReadOnlyServerCommand
	{
		private static ConcurrentDictionary<Type, IFindCommand> Cache = new ConcurrentDictionary<Type, IFindCommand>(1, 127);

		private readonly IServiceLocator Locator;
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public GetDomainObject(
			IServiceLocator locator,
			IDomainModel domainModel,
			IPermissionManager permissions)
		{
			Contract.Requires(locator != null);
			Contract.Requires(domainModel != null);
			Contract.Requires(permissions != null);

			this.Locator = locator;
			this.DomainModel = domainModel;
			this.Permissions = permissions;
		}

		[DataContract(Namespace = "")]
		public class Argument
		{
			[DataMember]
			public string Name;
			[DataMember]
			public string[] Uri;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument { Name = "Module.Entity", Uri = new[] { "1001", "1002" } });
		}

		public ICommandResult<TOutput> Execute<TInput, TOutput>(ISerialization<TInput> input, ISerialization<TOutput> output, TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;

			try
			{
				var result = GetAndReturn(output, either.Argument.Name, either.Argument.Uri);
				return CommandResult<TOutput>.Success(result.Result, "Found {0} item(s)", result.Count);
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

		public TFormat Execute<TFormat>(ISerialization<TFormat> serialization, string name, string[] uri)
		{
			return GetAndReturn(serialization, name, uri).Result;
		}

		private Type ValidateArgument(string name, string[] uri)
		{
			var objectType = DomainModel.Find(name);
			if (objectType == null)
				throw new ArgumentException("Couldn't find domain object type {0}.".With(name));
			if (uri == null || uri.Length == 0)
				throw new ArgumentException("Uri not specified.");
			if (!typeof(IIdentifiable).IsAssignableFrom(objectType))
				throw new ArgumentException(@"Specified type ({0}) is not an identifiable object. 
Please check your arguments.", name);
			if (!Permissions.CanAccess(objectType))
				throw new SecurityException("You don't have permission to access: {0}.".With(name));
			return objectType;
		}

		public IIdentifiable[] GetData(Argument argument)
		{
			var objectType = ValidateArgument(argument.Name, argument.Uri);
			var repository = Locator.Resolve<IRepository<IIdentifiable>>(typeof(IRepository<>).MakeGenericType(objectType));
			return repository.Find(argument.Uri);
		}

		private class FindResult<TFormat>
		{
			public TFormat Result;
			public int Count;
		}

		private FindResult<TOutput> GetAndReturn<TOutput>(
			ISerialization<TOutput> output,
			string name,
			string[] uri)
		{
			var objectType = ValidateArgument(name, uri);
			IFindCommand command;
			if (!Cache.TryGetValue(objectType, out command))
			{
				var commandType = typeof(FindCommand<>).MakeGenericType(objectType);
				command = Activator.CreateInstance(commandType) as IFindCommand;
				Cache.TryAdd(objectType, command);
			}
			return command.Find(output, Locator, Permissions, uri);
		}

		private interface IFindCommand
		{
			FindResult<TOutput> Find<TOutput>(
				ISerialization<TOutput> output,
				IServiceLocator locator,
				IPermissionManager permissions,
				string[] uris);
		}

		private class FindCommand<TResult> : IFindCommand
			where TResult : IIdentifiable
		{
			public FindResult<TOutput> Find<TOutput>(
				ISerialization<TOutput> output,
				IServiceLocator locator,
				IPermissionManager permissions,
				string[] uris)
			{
				var repository = locator.Resolve<IRepository<TResult>>();
				var found = repository.Find(uris);
				var filtered = permissions.ApplyFilters(found);
				return new FindResult<TOutput> { Result = output.Serialize(filtered), Count = filtered.Length };
			}
		}
	}
}
