using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Primitives;
using Revenj;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Serialization;
using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Threading.Tasks;

namespace AspNetTutorial
{
	public class RevenjMiddleware
	{
		private readonly RequestDelegate next;
		private readonly IObjectFactory objectFactory;
		private readonly IWireSerialization serialization;
		private readonly IDomainModel domainModel;
		private readonly ILoggerProvider loggerProvider;

		public RevenjMiddleware(
			RequestDelegate next,
			IObjectFactory objectFactory,
			IWireSerialization serialization,
			IDomainModel domainModel,
			ILoggerProvider loggerProvider)
		{
			this.next = next;
			this.objectFactory = objectFactory;
			this.serialization = serialization;
			this.domainModel = domainModel;
			this.loggerProvider = loggerProvider;
		}

		private static readonly PathString Path = new PathString("/revenj");

		public Task Invoke(HttpContext context)
		{
			var request = context.Request;
			if (request.Method == "POST" && request.Path.StartsWithSegments(Path) && request.Path != Path)
				return HandleCommand(request.Path.Value.Substring(Path.Value.Length + 1), context);

			return next(context);
		}

		private static Task ReturnError(HttpResponse response, string error, int code)
		{
			response.ContentType = "text/plain; charset=UTF-8";
			response.StatusCode = code;
			return response.WriteAsync(error);
		}

		private Task HandleCommand(string command, HttpContext context)
		{
			var type = domainModel.Find(command);
			if (type == null || !typeof(ICommand).IsAssignableFrom(type))
				return ReturnError(context.Response, $"Invalid command: {command}", 400);
			IExecuteCommand execute;
			if (!Cache.TryGetValue(type, out execute))
			{
				var newDict = new Dictionary<Type, IExecuteCommand>(Cache);
				var genType = typeof(ExecuteCommand<>).MakeGenericType(type);
				newDict[type] = execute = (IExecuteCommand)Activator.CreateInstance(genType, objectFactory, serialization, loggerProvider);
				Cache = newDict;
			}
			return execute.Submit(context);
		}

		private static Dictionary<Type, IExecuteCommand> Cache = new Dictionary<Type, IExecuteCommand>();
		interface IExecuteCommand
		{
			Task Submit(HttpContext http);
		}

		public class ExecuteCommand<T> : IExecuteCommand
			where T : ICommand
		{
			private readonly IObjectFactory objectFactory;
			private readonly IWireSerialization serialization;
			private readonly ILogger logger;
			private readonly StreamingContext streamingContext;

			public ExecuteCommand(IObjectFactory objectFactory, IWireSerialization serialization, ILoggerProvider loggerProvider)
			{
				this.objectFactory = objectFactory;
				this.serialization = serialization;
				this.streamingContext = new StreamingContext(StreamingContextStates.All, objectFactory);
				this.logger = loggerProvider.CreateLogger(typeof(ExecuteCommand<T>).FullName);
			}

			public Task Submit(HttpContext http)
			{
				string accept = "application/json";
				StringValues headers;
				if (http.Request.Headers.TryGetValue("accept", out headers))
					accept = headers[0];
				T command;
				try
				{
					command = (T)serialization.Deserialize(http.Request.Body, typeof(T), http.Request.ContentType, streamingContext);
				}
				catch (Exception ex)
				{
					logger.LogError(ex.ToString());
					return ReturnError(http.Response, $"Error deserializing command: {ex.Message}", 400);
				}
				try
				{
					Dictionary<string, List<string>> errors;
					using (var context = objectFactory.DoWork())
					{
						context.Submit(command);
						errors = command.GetValidationErrors();
						if (errors.Count == 0)
							context.Commit();
					}
					http.Response.ContentType = accept;
					if (errors.Count == 0)
					{
						http.Response.StatusCode = 200;
						serialization.Serialize(command, accept, http.Response.Body);
					}
					else
					{
						http.Response.StatusCode = 400;
						serialization.Serialize(errors, accept, http.Response.Body);
					}
					//ideally Revenj could provide async DB API :(
					return Task.CompletedTask;
				}
				catch (CustomException ex)
				{
					logger.LogError(ex.ToString());
					return ReturnError(http.Response, ex.Message, 400);
				}
				catch (Exception ex)
				{
					logger.LogError(ex.ToString());
					return ReturnError(http.Response, "Unknown error", 500);
				}
			}
		}
	}
}
