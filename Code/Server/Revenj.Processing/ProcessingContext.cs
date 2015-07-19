using System;
using System.Collections.Generic;
using System.Data.Common;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Globalization;
using System.Linq;
using System.Net;
using System.Security;
using System.Security.Principal;
using Revenj.Common;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Processing
{
	internal class ProcessingContext : IProcessingEngine
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Server");

		private readonly IServiceProvider Scope;
		private readonly IPermissionManager Permissions;

		public ProcessingContext(
			IServiceProvider scope,
			IPermissionManager permissions)
		{
			Contract.Requires(scope != null);
			Contract.Requires(permissions != null);

			this.Scope = scope;
			this.Permissions = permissions;
		}

		public IProcessingResult<TOutput> Execute<TInput, TOutput>(
			IServerCommandDescription<TInput>[] commandDescriptions, 
			IPrincipal principal)
		{
			var start = Stopwatch.GetTimestamp();

			if (commandDescriptions == null || commandDescriptions.Length == 0)
			{
				TraceSource.TraceEvent(TraceEventType.Warning, 5310);
				return
					ProcessingResult<TOutput>.Create(
						"There are no commands to execute.",
						HttpStatusCode.BadRequest,
						null,
						start);
			}

			var inputSerializer = (ISerialization<TInput>)Scope.GetService(typeof(ISerialization<TInput>));
			var outputSerializer = (ISerialization<TOutput>)Scope.GetService(typeof(ISerialization<TOutput>));

			var executedCommands = new List<ICommandResultDescription<TOutput>>();
			try
			{
				foreach (var c in commandDescriptions)
					if (!Permissions.CanAccess(c.CommandType.FullName, principal))
					{
						TraceSource.TraceEvent(
							TraceEventType.Warning,
							5311,
							"Access denied. User: {0}. Target: {1}",
							principal.Identity.Name,
							c.CommandType.FullName);
						return
							ProcessingResult<TOutput>.Create(
								"You don't have permission to execute command: " + c.CommandType.FullName,
								HttpStatusCode.Forbidden,
								executedCommands,
								start);
					}

				foreach (var cd in commandDescriptions)
				{
					var startCommand = Stopwatch.GetTimestamp();
					var command = (IServerCommand)Scope.GetService(cd.CommandType);
					var result = command.Execute(Scope, inputSerializer, outputSerializer, principal, cd.Data);
					if (result == null)
						throw new FrameworkException("Result returned null for " + cd.CommandType);
					executedCommands.Add(CommandResultDescription<TOutput>.Create(cd.RequestID, result, startCommand));
					if ((int)result.Status >= 400)
					{
						return ProcessingResult<TOutput>.Create(
							result.Message,
							result.Status,
							executedCommands,
							start);
					}
				}

				var duration = (decimal)(Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
				return
					ProcessingResult<TOutput>.Create(
						"Commands executed in: " + duration.ToString(CultureInfo.InvariantCulture) + " ms",
						HttpStatusCode.OK,
						executedCommands,
						start);
			}
			catch (SecurityException ex)
			{
				TraceSource.TraceEvent(
					TraceEventType.Warning,
					5312,
					"Security error. User: {0}. Error: {1}.",
					principal.Identity.Name,
					ex);
				return
					ProcessingResult<TOutput>.Create(
						"You don't have authorization to perform requested action: {0}".With(ex.Message),
						HttpStatusCode.Forbidden,
						executedCommands,
						start);
			}
			catch (AggregateException ex)
			{
				TraceSource.TraceEvent(
					TraceEventType.Error,
					5313,
					"Multiple errors. User: {0}. Error: {1}.",
					principal.Identity.Name,
					ex.GetDetailedExplanation());
				return Exceptions.DebugMode
					? ProcessingResult<TOutput>.Create(
						ex.GetDetailedExplanation(),
						HttpStatusCode.InternalServerError,
						executedCommands,
						start)
					: ProcessingResult<TOutput>.Create(
						string.Join(Environment.NewLine, ex.InnerExceptions.Select(it => it.Message)),
						HttpStatusCode.InternalServerError,
						executedCommands,
						start);
			}
			catch (OutOfMemoryException ex)
			{
				TraceSource.TraceEvent(TraceEventType.Critical, 5315, ex.GetDetailedExplanation());
				return Exceptions.DebugMode
					? ProcessingResult<TOutput>.Create(
						ex.GetDetailedExplanation(),
						HttpStatusCode.ServiceUnavailable,
						executedCommands,
						start)
					: ProcessingResult<TOutput>.Create(
						ex.Message,
						HttpStatusCode.ServiceUnavailable,
						executedCommands,
						start);
			}
			catch (DbException ex)
			{
				TraceSource.TraceEvent(TraceEventType.Warning, 5316, ex.GetDetailedExplanation());
				return Exceptions.DebugMode
					? ProcessingResult<TOutput>.Create(
						ex.GetDetailedExplanation(),
						HttpStatusCode.Conflict,
						executedCommands,
						start)
					: ProcessingResult<TOutput>.Create(
						ex.Message,
						HttpStatusCode.Conflict,
						executedCommands,
						start);
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(
					TraceEventType.Error,
					5317,
					"Unexpected error. User: {0}. Error: {1}",
					principal.Identity.Name,
					ex.GetDetailedExplanation());
				return Exceptions.DebugMode
					? ProcessingResult<TOutput>.Create(
						ex.GetDetailedExplanation(),
						HttpStatusCode.InternalServerError,
						executedCommands,
						start)
					: ProcessingResult<TOutput>.Create(
						ex.Message,
						HttpStatusCode.InternalServerError,
						executedCommands,
						start);
			}
		}
	}
}
