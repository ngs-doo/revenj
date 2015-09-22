using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Net;
using System.Runtime.Serialization;
using System.Security.Principal;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(Read))]
	public class Read : IReadOnlyServerCommand
	{
		private static Dictionary<Type, IFindCommand> Cache = new Dictionary<Type, IFindCommand>();

		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public Read(
			IDomainModel domainModel,
			IPermissionManager permissions)
		{
			Contract.Requires(domainModel != null);
			Contract.Requires(permissions != null);

			this.DomainModel = domainModel;
			this.Permissions = permissions;
		}

		[DataContract(Namespace = "")]
		public class Argument
		{
			[DataMember]
			public string Name;
			[DataMember]
			public string Uri;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument { Name = "Module.Entity", Uri = "1001" });
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer, Type objectType)
		{
			return serializer.Serialize(new Argument { Name = objectType.FullName, Uri = "1001" });
		}

		public ICommandResult<TOutput> Execute<TInput, TOutput>(
			IServiceProvider locator,
			ISerialization<TInput> input,
			ISerialization<TOutput> output,
			IPrincipal principal,
			TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;
			var argument = either.Argument;

			var objectType = DomainModel.Find(argument.Name);
			if (objectType == null)
				return CommandResult<TOutput>.Fail("Couldn't find domain object type {0}.".With(argument.Name), null);

			if (!Permissions.CanAccess(objectType.FullName, principal))
				return CommandResult<TOutput>.Forbidden(argument.Name);
			if (argument.Uri == null)
				return CommandResult<TOutput>.Fail(
					"Uri to read not specified.",
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output, objectType)));

			try
			{
				IFindCommand command;
				if (!Cache.TryGetValue(objectType, out command))
				{
					var commandType = typeof(FindCommand<>).MakeGenericType(objectType);
					command = Activator.CreateInstance(commandType) as IFindCommand;
					var newCache = new Dictionary<Type, IFindCommand>(Cache);
					newCache[objectType] = command;
					Cache = newCache;
				}
				var result = command.Find(output, locator, Permissions, principal, argument.Uri);

				return result != null
					? CommandResult<TOutput>.Success(result, "Object found")
					: CommandResult<TOutput>.Return(
							HttpStatusCode.NotFound,
							result,
							"Can't find {0} with Uri: {1}.".With(objectType.FullName, argument.Uri));
			}
			catch (ArgumentException ex)
			{
				return CommandResult<TOutput>.Fail(
					ex.Message,
					ex.GetDetailedExplanation() + @"
Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output, objectType)));
			}
		}

		private interface IFindCommand
		{
			TOutput Find<TOutput>(
				ISerialization<TOutput> output,
				IServiceProvider locator,
				IPermissionManager permissions,
				IPrincipal principal,
				string uri);
		}

		private class FindCommand<TDomainObject> : IFindCommand
			where TDomainObject : IIdentifiable
		{
			public TOutput Find<TOutput>(
				ISerialization<TOutput> output,
				IServiceProvider locator,
				IPermissionManager permissions,
				IPrincipal principal,
				string uri)
			{
				var repository = locator.Resolve<IRepository<TDomainObject>>();
				var data = repository.Find(new[] { uri });
				var filtered = permissions.ApplyFilters(principal, data);
				if (filtered.Length == 1)
					return output.Serialize(filtered[0]);
				return default(TOutput);
			}
		}
	}
}
