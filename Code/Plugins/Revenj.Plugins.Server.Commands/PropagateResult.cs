using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Runtime.Serialization;
using NGS;
using NGS.DomainPatterns;
using NGS.Extensibility;
using NGS.Serialization;
using Revenj.Processing;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(PropagateResult))]
	public class PropagateResult : IReadOnlyServerCommand
	{
		private readonly IServiceLocator Locator;

		public PropagateResult(IServiceLocator locator)
		{
			Contract.Requires(locator != null);

			this.Locator = locator;
		}

		[DataContract(Namespace = "")]
		public class Argument<TFormat>
		{
			[DataMember]
			public string ResultID;
			[DataMember]
			public string InputID;
			[DataMember]
			public TFormat Transformation;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument<TFormat> { ResultID = "request-1", InputID = "input-1" });
		}

		public ICommandResult<TOutput> Execute<TInput, TOutput>(ISerialization<TInput> input, ISerialization<TOutput> output, TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument<TInput>, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;
			var argument = either.Argument;

			var commands = Locator.Resolve<IEnumerable<IServerCommandDescription<TInput>>>();
			var results = Locator.Resolve<IEnumerable<ICommandResultDescription<TOutput>>>();

			var resultCommand = results.FirstOrDefault(it => it.RequestID == argument.ResultID);
			if (resultCommand == null)
				return CommandResult<TOutput>.Fail("Couldn't find executed command {0}".With(argument.ResultID), null);

			var inputCommand = commands.FirstOrDefault(it => it.RequestID == argument.InputID);
			if (inputCommand == null)
				return CommandResult<TOutput>.Fail("Couldn't find command description {0}".With(argument.InputID), null);

			var transformation = input.Deserialize<TInput, ITransformation<TOutput, TInput>>(argument.Transformation);

			inputCommand.Data = transformation.Transform(output, input, resultCommand.Result.Data);

			return CommandResult<TOutput>.Success(default(TOutput), "Data propagated");
		}
	}
}
