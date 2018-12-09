using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Linq;
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
	[ExportMetadata(Metadata.ClassType, typeof(EvaluateQuery))]
	public class EvaluateQuery : IReadOnlyServerCommand
	{
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public EvaluateQuery(
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
			public TFormat Data;
			[DataMember]
			public string QueryName;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument<TFormat> { QueryName = "Module.Query" });
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

			IEvaluateCommand command;
			if (!Cache.TryGetValue(argument.QueryName, out command))
			{
				var queryType = DomainModel.Find(argument.QueryName);
				if (queryType == null)
					return CommandResult<TOutput>.Fail(
						"Couldn't find query type {0}.".With(argument.QueryName),
						@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

				var ri1 = queryType.GetInterfaces().FirstOrDefault(it => it.IsGenericType && it.GetGenericTypeDefinition() == typeof(IQuery<,>));
				var ri2 = queryType.GetInterfaces().FirstOrDefault(it => it.IsGenericType && it.GetGenericTypeDefinition() == typeof(IQuery<>));
				if (ri1 == null && ri2 == null)
					return CommandResult<TOutput>.Fail(@"Specified type ({0}) is not an query. 
Please check your arguments.".With(argument.QueryName), null);

				var commandType = ri1 != null
					? typeof(EvaluateCommand<,>).MakeGenericType(queryType, ri1.GetGenericArguments()[0])
					: typeof(EvaluateCommand<,,>).MakeGenericType(queryType, ri2.GetGenericArguments()[0], ri2.GetGenericArguments()[1]);
				command = Activator.CreateInstance(commandType) as IEvaluateCommand;
				var newCache = new Dictionary<string, IEvaluateCommand>(Cache);
				newCache[argument.QueryName] = command;
				Cache = newCache;
			}

			try
			{
				var result = command.Evaluate(input, output, locator, argument.Data);
				return CommandResult<TOutput>.Success(result, "Evaluated");
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

		private static Dictionary<string, IEvaluateCommand> Cache = new Dictionary<string, IEvaluateCommand>();

		private interface IEvaluateCommand
		{
			TOutput Evaluate<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
				TInput data);
		}

		private class EvaluateCommand<TSignature, TResult> : IEvaluateCommand
			where TSignature : IQuery<TResult>, new()
		{
			public TOutput Evaluate<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
				TInput data)
			{
				IHandler<TSignature> queryEvaluator;
				try
				{
					queryEvaluator = locator.Resolve<IHandler<TSignature>>();
				}
				catch (Exception ex)
				{
					throw new ArgumentException("Can't resolve query handler for {0}".With(typeof(TSignature).FullName), ex);
				}
				var signature = new TSignature();
				queryEvaluator.Handle(signature);
				return output.Serialize(signature.Out);
			}
		}

		private class EvaluateCommand<TSignature, TArgument, TResult> : IEvaluateCommand
			where TSignature : IQuery<TArgument, TResult>, new()
		{
			public TOutput Evaluate<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
				TInput data)
			{
				TArgument argument;
				try
				{
					argument = data != null ? input.Deserialize<TInput, TArgument>(data, locator) : Activator.CreateInstance<TArgument>();
				}
				catch (Exception ex)
				{
					throw new ArgumentException("Error deserializing input arguments", ex);
				}
				IHandler<TSignature> queryEvaluator;
				try
				{
					queryEvaluator = locator.Resolve<IHandler<TSignature>>();
				}
				catch (Exception ex)
				{
					throw new ArgumentException("Can't resolve query handler for {0}".With(typeof(TSignature).FullName), ex);
				}
				var signature = new TSignature();
				signature.In = argument;
				queryEvaluator.Handle(signature);
				return output.Serialize(signature.Out);
			}
		}
	}
}
