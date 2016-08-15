using System;
using System.ComponentModel.Composition;
using System.Runtime.Serialization;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Serialization;
using Revenj.Utility;
using System.Security.Principal;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(CheckDomainObject))]
	public class CheckDomainObject : IReadOnlyServerCommand
	{
		private readonly GetDomainObject GetDomainObject;

		public CheckDomainObject(GetDomainObject getDomainObject)
		{
			this.GetDomainObject = getDomainObject;
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
			if (either.Argument.Uri == null)
				return CommandResult<TOutput>.Fail("Uri not provided.", @"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));
			try
			{
				var arg = new GetDomainObject.Argument { Name = either.Argument.Name, Uri = new[] { either.Argument.Uri } };
				var result = GetDomainObject.GetData(locator, arg, principal);
				return CommandResult<TOutput>.Success(output.Serialize(result.Length == 1), result.Length == 1 ? "Found" : "Not found");
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
	}
}
