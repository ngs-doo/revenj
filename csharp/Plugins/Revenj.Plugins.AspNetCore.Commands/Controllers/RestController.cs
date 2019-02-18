using Microsoft.AspNetCore.Mvc;
using Revenj.Extensibility;
using Revenj.Processing;
using System.Net;

namespace Revenj.Plugins.AspNetCore.Commands
{
	[Route("RestApplication.svc")]
	public class RestController : ControllerBase
	{
		private readonly IPluginRepository<IServerCommand> CommandsRepository;
		private readonly RestApplication Application;

		public RestController(
			IPluginRepository<IServerCommand> commandsRepository,
			RestApplication application)
		{
			this.CommandsRepository = commandsRepository;
			this.Application = application;
		}

		[HttpGet("{command}")]
		public void Get(string command)
		{
			Application.Execute(command, null, HttpContext);
		}

		[HttpPost("{command}")]
		public void Post(string command)
		{
			Application.Execute(command, Request.Body, HttpContext);
		}
	}
}
