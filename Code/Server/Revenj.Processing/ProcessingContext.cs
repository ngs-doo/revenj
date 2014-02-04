using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Net;
using System.Security;
using System.Threading;
using NGS;
using NGS.Common;
using NGS.Extensibility;
using NGS.Logging;
using NGS.Security;
using NGS.Serialization;
using NGS.Utility;

namespace Revenj.Processing
{
	public class ProcessingContext : IProcessingEngine
	{
		private readonly IObjectFactory Scope;
		private readonly IPermissionManager Permissions;
		private readonly ILogFactory LogFactory;

		public ProcessingContext(
			IObjectFactory scope,
			IPermissionManager permissions,
			ILogFactory logFactory)
		{
			Contract.Requires(scope != null);
			Contract.Requires(permissions != null);
			Contract.Requires(logFactory != null);

			this.Scope = scope;
			this.Permissions = permissions;
			this.LogFactory = logFactory;
		}

		public IProcessingResult<TOutput> Execute<TInput, TOutput>(IServerCommandDescription<TInput>[] commandDescriptions)
		{
			var stopwatch = Stopwatch.StartNew();

			if (commandDescriptions == null || commandDescriptions.Length == 0)
				return
					ProcessingResult<TOutput>.Create(
						"There are no commands to execute.",
						HttpStatusCode.BadRequest,
						null);

			var inputSerializer = Scope.Resolve<ISerialization<TInput>>();
			var outputSerializer = Scope.Resolve<ISerialization<TOutput>>();

			var executedCommands = new List<ICommandResultDescription<TOutput>>();
			try
			{
				foreach (var c in commandDescriptions)
					if (!Permissions.CanAccess(c.CommandType))
					{
						LogFactory.Create("Processing context").Trace(
							() => "Access denied. User: {0}. Target: {1}.".With(
									Thread.CurrentPrincipal.Identity.Name,
									c.CommandType.FullName));
						return
							ProcessingResult<TOutput>.Create(
								"You don't have permission to execute command: " + c.CommandType.FullName,
								HttpStatusCode.Forbidden,
								executedCommands);
					}

				foreach (var cd in commandDescriptions)
				{
					var command = Scope.Resolve<IServerCommand>(cd.CommandType);
					var result = command.Execute(inputSerializer, outputSerializer, cd.Data);
					if (result == null)
						throw new FrameworkException("Result returned null for " + cd.CommandType);
					executedCommands.Add(CommandResultDescription<TOutput>.Create(cd.RequestID, result));
					if ((int)result.Status >= 400)
						return ProcessingResult<TOutput>.Create(result.Message, result.Status, executedCommands);
				}

				return
					ProcessingResult<TOutput>.Create(
						"Commands executed in: {0} ms.".With(stopwatch.ElapsedMilliseconds),
						HttpStatusCode.OK,
						executedCommands);
			}
			catch (SecurityException ex)
			{
				LogFactory.Create("Processing context").Trace(
					() => "Security error. User: {0}. Error: {1}.".With(
							Thread.CurrentPrincipal.Identity.Name,
							ex.ToString()));
				return
					ProcessingResult<TOutput>.Create(
						"You don't have authorization to perform requested action: {0}".With(ex.Message),
						HttpStatusCode.Forbidden,
						executedCommands);
			}
			catch (AggregateException ex)
			{
				LogFactory.Create("Processing context").Trace(
					() => "Multiple errors. User: {0}. Error: {1}.".With(
							Thread.CurrentPrincipal.Identity.Name,
							ex.GetDetailedExplanation()));
				return Exceptions.DebugMode
					? ProcessingResult<TOutput>.Create(ex.GetDetailedExplanation(), HttpStatusCode.InternalServerError, executedCommands)
					: ProcessingResult<TOutput>.Create(
						string.Join(Environment.NewLine, ex.InnerExceptions.Select(it => it.Message)),
						HttpStatusCode.InternalServerError,
						executedCommands);
			}
			catch (OutOfMemoryException ex)
			{
				LogFactory.Create("Processing context").Error("Out of memory error. Error: " + ex.GetDetailedExplanation());
				return Exceptions.DebugMode
					? ProcessingResult<TOutput>.Create(ex.GetDetailedExplanation(), HttpStatusCode.ServiceUnavailable, executedCommands)
					: ProcessingResult<TOutput>.Create(ex.Message, HttpStatusCode.ServiceUnavailable, executedCommands);
			}
			catch (Exception ex)
			{
				LogFactory.Create("Processing context").Trace(
					() => "Unexpected error. User: {0}. Error: {1}.".With(
							Thread.CurrentPrincipal.Identity.Name,
							ex.GetDetailedExplanation()));
				return Exceptions.DebugMode
					? ProcessingResult<TOutput>.Create(ex.GetDetailedExplanation(), HttpStatusCode.InternalServerError, executedCommands)
					: ProcessingResult<TOutput>.Create(ex.Message, HttpStatusCode.InternalServerError, executedCommands);
			}
		}
	}
}
