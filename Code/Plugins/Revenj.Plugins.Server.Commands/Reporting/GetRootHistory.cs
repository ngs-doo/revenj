using System;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
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
	[ExportMetadata(Metadata.ClassType, typeof(GetRootHistory))]
	public class GetRootHistory : IReadOnlyServerCommand
	{
		private readonly IServiceLocator Locator;
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public GetRootHistory(
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
			var argument = either.Argument;

			var rootType = DomainModel.Find(argument.Name);
			if (rootType == null)
				return CommandResult<TOutput>.Fail(
					"Couldn't find domain object type {0}.".With(argument.Name),
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

			if (!typeof(IObjectHistory).IsAssignableFrom(rootType))
				return CommandResult<TOutput>.Fail(@"Specified type ({0}) does not support history tracking. 
Please check your arguments.".With(argument.Name), null);

			if (!Permissions.CanAccess(rootType))
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0}.",
						argument.Name);

			if (argument.Uri == null || argument.Uri.Length == 0)
				return CommandResult<TOutput>.Fail(
					"Uri not specified.",
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

			try
			{
				var commandType = typeof(FindCommand<>).MakeGenericType(rootType);
				var command = Activator.CreateInstance(commandType) as IFindCommand;
				var result = command.Find(output, Locator, argument.Uri);

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

		private class FindResult<TFormat>
		{
			public TFormat Result;
			public int Count;
		}

		private interface IFindCommand
		{
			FindResult<TOutput> Find<TOutput>(ISerialization<TOutput> output, IServiceLocator locator, string[] uris);
		}

		private class FindCommand<TRoot> : IFindCommand
			where TRoot : IObjectHistory
		{
			public FindResult<TOutput> Find<TOutput>(ISerialization<TOutput> output, IServiceLocator locator, string[] uris)
			{
				IRepository<IHistory<TRoot>> repository;
				try
				{
					repository = locator.Resolve<IRepository<IHistory<TRoot>>>();
				}
				catch (Exception ex)
				{
					throw new ArgumentException(
						"Can't create instance of history repository. Is history concept declared for {0}?".With(
							typeof(TRoot).FullName),
						ex);
				}
				var found = repository.Find(uris);
				return new FindResult<TOutput> { Result = output.Serialize(found), Count = found.Length };
			}
		}
	}
}
