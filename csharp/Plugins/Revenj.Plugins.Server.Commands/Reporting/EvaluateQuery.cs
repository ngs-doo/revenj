using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Reflection;
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

				var returnMethods =
					(from m in queryType.GetMethods(BindingFlags.Static | BindingFlags.Public)
					 where m.Name == "Return"
					 let p = m.GetParameters()
					 where p != null && p.Length == 1 && typeof(ICommand).IsAssignableFrom(p[0].ParameterType)
					 select new { command = p[0].ParameterType, output = m.ReturnType, method = m }).ToList();
				if (returnMethods.Count != 1)
					return CommandResult<TOutput>.Fail("Invalid query type {0}.".With(argument.QueryName), null);
				var acceptMethods =
					(from m in queryType.GetMethods(BindingFlags.Static | BindingFlags.Public)
					 where m.Name == "Accept"
					 let p = m.GetParameters()
					 where p != null && p.Length < 2 && typeof(ICommand).IsAssignableFrom(m.ReturnType)
					 select new { method = m, accept = p.Length == 1 ? p[0].ParameterType : null }).ToList();
				if (acceptMethods.Count != 1)
					return CommandResult<TOutput>.Fail("Invalid query type {0}.".With(argument.QueryName), null);
				var commandType = acceptMethods[0].accept == null
					? typeof(EvaluateCommand<,>).MakeGenericType(returnMethods[0].command, returnMethods[0].output)
					: typeof(EvaluateCommand<,,>).MakeGenericType(returnMethods[0].command, acceptMethods[0].accept, returnMethods[0].output);
				command = Activator.CreateInstance(commandType, new object[] { acceptMethods[0].method, returnMethods[0].method }) as IEvaluateCommand;
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

		private class EvaluateCommand<TCommand, TResult> : IEvaluateCommand
			where TCommand : ICommand
		{
			protected readonly MethodInfo InMethod;
			protected readonly MethodInfo OutMethod;

			public EvaluateCommand(MethodInfo inMethod, MethodInfo outMethod)
			{
				this.InMethod = inMethod;
				this.OutMethod = outMethod;
				//TODO: lambda instead of reflection
			}

			public virtual TOutput Evaluate<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
				TInput data)
			{
				var command = (TCommand)InMethod.Invoke(null, new object[0]);
				return ExecuteCommand<TOutput>(output, locator, command);
			}

			protected TOutput ExecuteCommand<TOutput>(ISerialization<TOutput> output, IServiceProvider locator, TCommand command)
			{
				var domainStore = locator.Resolve<IEventStore>();
				try
				{
					domainStore.Submit(command);
				}
				catch (SecurityException) { throw; }
				catch (Exception ex)
				{
					throw new ArgumentException(
						ex.Message,
						new FrameworkException("Error while submitting query: {0}".With(ex.Message), ex));
				}
				try
				{
					return output.Serialize(OutMethod.Invoke(null, new object[] { command }));
				}
				catch (Exception ex)
				{
					throw new ArgumentException(
						ex.Message,
						new FrameworkException(@"Error serializing result: " + ex.Message, ex));
				}
			}
		}

		private class EvaluateCommand<TCommand, TArgument, TResult> : EvaluateCommand<TCommand, TResult>
			where TCommand : ICommand
		{
			public EvaluateCommand(MethodInfo inMethod, MethodInfo outMethod)
				: base(inMethod, outMethod) { }

			public override TOutput Evaluate<TInput, TOutput>(
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
				var command = (TCommand)InMethod.Invoke(null, new object[] { argument });
				return ExecuteCommand<TOutput>(output, locator, command);
			}
		}
	}
}
