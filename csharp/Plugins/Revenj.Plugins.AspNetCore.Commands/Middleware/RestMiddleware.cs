using Microsoft.AspNetCore.Http;
using Revenj.AspNetCore;
using Revenj.Extensibility;
using Revenj.Processing;
using System.Net;
using System.Threading.Tasks;

namespace Revenj.Plugins.AspNetCore.Commands
{
	public class RestMiddleware
	{
		private readonly IPluginRepository<IServerCommand> CommandsRepository;
		private readonly RestApplication Application;

		public RestMiddleware(
			IPluginRepository<IServerCommand> commandsRepository,
			RestApplication application)
		{
			this.CommandsRepository = commandsRepository;
			this.Application = application;
		}

		public Task Handle(HttpContext context, int prefixLength)
		{
			var path = context.Request.Path.Value;
			if (path.Length == prefixLength)
				return context.Response.WriteError("Command not specified", HttpStatusCode.BadRequest);
			var command = path.Substring(prefixLength + 1);
			if (command.Length == 0)
				return context.Response.WriteError("Command not specified", HttpStatusCode.BadRequest);
			var commandType = CommandsRepository.Find(command);
			if (commandType == null)
				return context.Response.WriteError($"Unknown command: {command}", HttpStatusCode.NotFound);
			switch (context.Request.Method)
			{
				case "POST":
					return Application.Execute(commandType, context.Request.Body, context);
				case "GET":
					return Application.Execute(commandType, null, context);
				default:
					return Utility.WriteError(context.Response, "Unsuported method type", HttpStatusCode.MethodNotAllowed);
			}
		}
	}
}
