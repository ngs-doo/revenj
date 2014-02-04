using System;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Net;
using System.Runtime.Serialization;
using NGS;
using NGS.DomainPatterns;
using NGS.Extensibility;
using NGS.Security;
using NGS.Serialization;
using NGS.Utility;
using Revenj.Processing;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(Read))]
	public class Read : IReadOnlyServerCommand
	{
		private readonly IServiceLocator Locator;
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public Read(
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

		public ICommandResult<TOutput> Execute<TInput, TOutput>(ISerialization<TInput> input, ISerialization<TOutput> output, TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;
			var argument = either.Argument;

			var objectType = DomainModel.Find(argument.Name);
			if (objectType == null)
				return CommandResult<TOutput>.Fail("Couldn't find domain object type {0}.".With(argument.Name), null);

			if (!Permissions.CanAccess(objectType))
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0}.",
						argument.Name);

			if (argument.Uri == null)
				return CommandResult<TOutput>.Fail(
					"Uri to read not specified.",
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output, objectType)));

			try
			{
				var commandType = typeof(FindCommand<>).MakeGenericType(objectType);
				var command = Activator.CreateInstance(commandType) as IFindCommand;
				var result = command.Find(output, Locator, Permissions, argument.Uri);

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
				IServiceLocator locator,
				IPermissionManager permissions,
				string uri);
		}

		private class FindCommand<TDomainObject> : IFindCommand
			where TDomainObject : IIdentifiable
		{
			public TOutput Find<TOutput>(
				ISerialization<TOutput> output,
				IServiceLocator locator,
				IPermissionManager permissions,
				string uri)
			{
				var repository = locator.Resolve<IRepository<TDomainObject>>();
				var data = repository.Find(new[] { uri });
				var filtered = permissions.ApplyFilters(data).FirstOrDefault();
				return filtered != null ? output.Serialize(filtered) : default(TOutput);
			}
		}
	}
}
