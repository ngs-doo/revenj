using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Net;
using System.Security;
using System.Threading;
using NGS;
using NGS.Common;
using NGS.DatabasePersistence;
using NGS.Extensibility;
using NGS.Logging;
using NGS.Security;
using NGS.Serialization;
using NGS.Utility;

namespace Revenj.Processing
{
	public class ProcessingEngine : IProcessingEngine
	{
		private readonly IObjectFactory ObjectFactory;
		private readonly IDatabaseQueryManager TransactionManager;
		private readonly IPermissionManager Permissions;
		private readonly ILogger Logger;
		private readonly Dictionary<Type, Type> ActualCommands = new Dictionary<Type, Type>();
		private readonly ConcurrentDictionary<Type, object> Serializators = new ConcurrentDictionary<Type, object>(1, 7);

		public ProcessingEngine(
			IObjectFactory objectFactory,
			IDatabaseQueryManager transactionManager,
			IPermissionManager permissions,
			ILogFactory logFactory,
			IExtensibilityProvider extensibilityProvider)
		{
			Contract.Requires(objectFactory != null);
			Contract.Requires(transactionManager != null);
			Contract.Requires(permissions != null);
			Contract.Requires(logFactory != null);
			Contract.Requires(extensibilityProvider != null);

			this.ObjectFactory = objectFactory.CreateInnerFactory();
			this.TransactionManager = transactionManager;
			this.Permissions = permissions;
			this.Logger = logFactory.Create("Processing engine");
			var commandTypes = extensibilityProvider.FindPlugins<IServerCommand>();

			ObjectFactory.RegisterTypes(commandTypes, InstanceScope.Transient);
			foreach (var ct in commandTypes)
				ActualCommands[ct] = ct;
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
						ActualCommands[type] = ct;
					}
				}
			}
		}

		private ISerialization<TFormat> GetSerializer<TFormat>()
		{
			object serializer;
			if (!Serializators.TryGetValue(typeof(TFormat), out serializer))
			{
				serializer = ObjectFactory.Resolve<ISerialization<TFormat>>();
				Serializators.TryAdd(typeof(TFormat), serializer);
			}
			return (ISerialization<TFormat>)serializer;
		}

		class CommandInfo<TFormat>
		{
			public readonly IServerCommandDescription<TFormat> Description;
			public readonly Type Target;
			public readonly bool IsReadOnly;

			public CommandInfo(
				IServerCommandDescription<TFormat> description,
				Dictionary<Type, Type> allowedCommands)
			{
				this.Description = description;
				if (allowedCommands.TryGetValue(description.CommandType, out Target))
					this.IsReadOnly = typeof(IReadOnlyServerCommand).IsAssignableFrom(Target);
			}

			public IServerCommand GetCommand(IObjectFactory factory)
			{
				return factory.Resolve<IServerCommand>(Target);
			}
		}

		public IProcessingResult<TOutput> Execute<TInput, TOutput>(IServerCommandDescription<TInput>[] commandDescriptions)
		{
			if (commandDescriptions == null || commandDescriptions.Length == 0)
				return
					ProcessingResult<TOutput>.Create(
						"There are no commands to execute.",
						HttpStatusCode.BadRequest,
						null);

			var stopwatch = Stopwatch.StartNew();

			foreach (var c in commandDescriptions)
				if (!Permissions.CanAccess(c.CommandType))
				{
					Logger.Trace(
						() => "Access denied. User: {0}. Target: {1}.".With(
								Thread.CurrentPrincipal.Identity.Name,
								c.CommandType.FullName));
					return
						ProcessingResult<TOutput>.Create(
							"You don't have permission to execute command: " + c.CommandType.FullName,
							HttpStatusCode.Forbidden,
							null);
				}

			var inputSerializer = GetSerializer<TInput>();
			var outputSerializer = GetSerializer<TOutput>();

			var commands = new CommandInfo<TInput>[commandDescriptions.Length];
			var useTransaction = false;
			for (int i = 0; i < commands.Length; i++)
			{
				var c = commands[i] = new CommandInfo<TInput>(commandDescriptions[i], ActualCommands);
				useTransaction = useTransaction || !c.IsReadOnly;
				if (c.Target == null)
				{
					Logger.Trace(
						() => "Unknown target. User: {0}. Target: {1}.".With(
								Thread.CurrentPrincipal.Identity.Name,
								commandDescriptions[i].CommandType.FullName));
					return
						ProcessingResult<TOutput>.Create(
							"Unknown command: {0}. Check if requested command is registered in the system".With(
								c.Description.CommandType),
							HttpStatusCode.BadRequest,
							null);
				}
			}

			var scopeID = useTransaction ? Guid.NewGuid().ToString() : null;
			using (var scope = ObjectFactory.CreateScope(scopeID))
			{
				var executedCommands = new List<ICommandResultDescription<TOutput>>(commandDescriptions.Length);
				IDatabaseQuery query = null;
				try
				{
					try
					{
						query = TransactionManager.StartQuery(useTransaction);
					}
					catch (Exception ex)
					{
						Logger.Error("Can't start query. Error: " + ex.ToString());
						return Exceptions.DebugMode
							? ProcessingResult<TOutput>.Create(ex.ToString(), HttpStatusCode.ServiceUnavailable, null)
							: ProcessingResult<TOutput>.Create("Unable to create database connection", HttpStatusCode.ServiceUnavailable, null);
					}
					scope.RegisterInstance(query);
					if (useTransaction)
					{
						scope.RegisterInstance<IEnumerable<IServerCommandDescription<TInput>>>(commandDescriptions);
						scope.RegisterInstance<IEnumerable<ICommandResultDescription<TOutput>>>(executedCommands);
						scope.RegisterType(typeof(ProcessingContext), typeof(IProcessingEngine), InstanceScope.Singleton);
					}
					foreach (var cmd in commands)
					{
						var result = cmd.GetCommand(scope).Execute(inputSerializer, outputSerializer, cmd.Description.Data);
						if (result == null)
							throw new FrameworkException("Result returned null for " + cmd.Target.FullName);
						executedCommands.Add(CommandResultDescription<TOutput>.Create(cmd.Description.RequestID, result));
						if ((int)result.Status >= 400)
						{
							TransactionManager.EndQuery(query, false);
							return ProcessingResult<TOutput>.Create(result.Message, result.Status, executedCommands);
						}
					}

					TransactionManager.EndQuery(query, true);
					return
						ProcessingResult<TOutput>.Create(
							"Commands executed in: " + stopwatch.ElapsedMilliseconds.ToString() + " ms.",
							HttpStatusCode.OK,
							executedCommands);
				}
				catch (SecurityException ex)
				{
					Logger.Trace(
						() => "Security error. User: {0}. Error: {1}.".With(
								Thread.CurrentPrincipal.Identity.Name,
								ex.ToString()));
					TransactionManager.EndQuery(query, false);
					return
						ProcessingResult<TOutput>.Create(
							"You don't have authorization to perform requested action: " + ex.Message,
							HttpStatusCode.Forbidden,
							executedCommands);
				}
				catch (AggregateException ex)
				{
					Logger.Trace(
						() => "Multiple errors. User: {0}. Error: {1}.".With(
								Thread.CurrentPrincipal.Identity.Name,
								ex.GetDetailedExplanation()));
					TransactionManager.EndQuery(query, false);
					return Exceptions.DebugMode
						? ProcessingResult<TOutput>.Create(ex.GetDetailedExplanation(), HttpStatusCode.InternalServerError, executedCommands)
						: ProcessingResult<TOutput>.Create(
							string.Join(Environment.NewLine, ex.InnerExceptions.Select(it => it.Message)),
							HttpStatusCode.InternalServerError,
							executedCommands);
				}
				catch (OutOfMemoryException ex)
				{
					Logger.Error("Out of memory error. Error: " + ex.GetDetailedExplanation());
					TransactionManager.EndQuery(query, false);
					return Exceptions.DebugMode
						? ProcessingResult<TOutput>.Create(ex.GetDetailedExplanation(), HttpStatusCode.ServiceUnavailable, executedCommands)
						: ProcessingResult<TOutput>.Create(ex.Message, HttpStatusCode.ServiceUnavailable, executedCommands);
				}
				catch (Exception ex)
				{
					Logger.Trace(
						() => "Unexpected error. User: {0}. Error: {1}.".With(
								Thread.CurrentPrincipal.Identity.Name,
								ex.GetDetailedExplanation()));
					TransactionManager.EndQuery(query, false);
					return Exceptions.DebugMode
						? ProcessingResult<TOutput>.Create(ex.GetDetailedExplanation(), HttpStatusCode.InternalServerError, executedCommands)
						: ProcessingResult<TOutput>.Create(ex.Message, HttpStatusCode.InternalServerError, executedCommands);
				}
			}
		}
	}
}
