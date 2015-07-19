using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Data.Common;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Globalization;
using System.Linq;
using System.Net;
using System.Security;
using System.Security.Principal;
using Revenj.Common;
using Revenj.Extensibility;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Processing
{
	public class ProcessingEngine : IProcessingEngine
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Server");

		private readonly IObjectFactory ObjectFactory;
		private readonly IScopePool ScopePool;
		private readonly IPermissionManager Permissions;
		private readonly Dictionary<Type, IServerCommand> ActualCommands = new Dictionary<Type, IServerCommand>();
		private Dictionary<Type, object> Serializators = new Dictionary<Type, object>(7);

		public ProcessingEngine(
			IObjectFactory objectFactory,
			IScopePool scopePool,
			IPermissionManager permissions,
			IExtensibilityProvider extensibilityProvider)
		{
			Contract.Requires(objectFactory != null);
			Contract.Requires(scopePool != null);
			Contract.Requires(permissions != null);
			Contract.Requires(extensibilityProvider != null);

			this.ObjectFactory = objectFactory.CreateInnerFactory();
			this.ScopePool = scopePool;
			this.Permissions = permissions;
			var commandTypes = extensibilityProvider.FindPlugins<IServerCommand>();

			var commands = new Dictionary<Type, Type>();
			foreach (var ct in commandTypes)
				commands[ct] = ct;
			foreach (var ct in commandTypes)
			{
				var attr = ct.GetCustomAttributes(typeof(ExportMetadataAttribute), false) as ExportMetadataAttribute[];
				if (attr != null)
				{
					var insteadOf = attr.FirstOrDefault(it => it.Name == Metadata.InsteadOf);
					if (insteadOf != null)
					{
						var type = insteadOf.Value as Type;
						if (commandTypes.All(it => it != type))
							throw new FrameworkException("Can't find target {0} for InsteadOf attribute declared on {1}".With(type, ct));
						commands[type] = ct;
					}
				}
			}
			foreach (var ct in commands)
				ActualCommands[ct.Key] = ObjectFactory.Resolve<IServerCommand>(ct.Value);
		}

		private ISerialization<TFormat> GetSerializer<TFormat>()
		{
			object serializer;
			if (!Serializators.TryGetValue(typeof(TFormat), out serializer))
			{
				serializer = ObjectFactory.Resolve<ISerialization<TFormat>>();
				var newDict = new Dictionary<Type, object>(Serializators);
				newDict[typeof(TFormat)] = serializer;
				Serializators = newDict;
			}
			return (ISerialization<TFormat>)serializer;
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

			for (int i = 0; i < commandDescriptions.Length; i++)
			{
				var c = commandDescriptions[i];
				if (!Permissions.CanAccess(c.CommandType))
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
							null,
							start);
				}
			}

			var inputSerializer = GetSerializer<TInput>();
			var outputSerializer = GetSerializer<TOutput>();

			var useTransaction = false;
			for (int i = 0; i < commandDescriptions.Length; i++)
			{
				IServerCommand command;
				var cd = commandDescriptions[i];
				if (!ActualCommands.TryGetValue(cd.CommandType, out command))
				{
					TraceSource.TraceEvent(
						TraceEventType.Warning,
						5321,
						"Unknown target. User: {0}. Target: {1}",
						principal.Identity.Name,
						cd.CommandType.FullName);
					return
						ProcessingResult<TOutput>.Create(
							"Unknown command: {0}. Check if requested command is registered in the system".With(
								cd.CommandType),
							HttpStatusCode.NotImplemented,
							null,
							start);
				}
				useTransaction = useTransaction || (command is IReadOnlyServerCommand == false);
			}

			var executedCommands = new List<ICommandResultDescription<TOutput>>(commandDescriptions.Length);
			Scope scope = null;
			try
			{
				try
				{
					scope = ScopePool.Take(!useTransaction);
				}
				catch (Exception ex)
				{
					TraceSource.TraceEvent(TraceEventType.Critical, 5322, "{0}", ex);
					return Exceptions.DebugMode
						? ProcessingResult<TOutput>.Create(
							ex.ToString(),
							HttpStatusCode.ServiceUnavailable,
							null,
							start)
						: ProcessingResult<TOutput>.Create(
							"Unable to create database connection",
							HttpStatusCode.ServiceUnavailable,
							null,
							start);
				}
				foreach (var cd in commandDescriptions)
				{
					var startCommand = Stopwatch.GetTimestamp();
					var cmd = ActualCommands[cd.CommandType];
					var result = cmd.Execute(scope.Factory, inputSerializer, outputSerializer, cd.Data);
					if (result == null)
						throw new FrameworkException("Result returned null for " + cd.CommandType.FullName);
					executedCommands.Add(CommandResultDescription<TOutput>.Create(cd.RequestID, result, startCommand));
					if ((int)result.Status >= 400)
					{
						ScopePool.Release(scope, false);
						return ProcessingResult<TOutput>.Create(
							result.Message,
							result.Status,
							executedCommands,
							start);
					}
				}

				ScopePool.Release(scope, true);
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
				ScopePool.Release(scope, false);
				return
					ProcessingResult<TOutput>.Create(
						"You don't have authorization to perform requested action: " + ex.Message,
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
				ScopePool.Release(scope, false);
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
				ScopePool.Release(scope, false);
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
				ScopePool.Release(scope, false);
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
				ScopePool.Release(scope, false);
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