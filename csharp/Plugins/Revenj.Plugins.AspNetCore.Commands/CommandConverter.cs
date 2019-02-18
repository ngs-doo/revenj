using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Primitives;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Serialization;
using Revenj.Utility;
using System;
using System.Diagnostics;
using System.Globalization;
using System.Net;

namespace Revenj.Plugins.AspNetCore.Commands
{
	public class CommandConverter
	{
		private readonly RestApplication Application;
		private readonly IObjectFactory ObjectFactory;
		private readonly IProcessingEngine ProcessingEngine;
		private readonly IWireSerialization Serialization;

		public CommandConverter(
			RestApplication application,
			IObjectFactory objectFactory,
			IProcessingEngine processingEngine,
			IWireSerialization serialization)
		{
			this.Application = application;
			this.ObjectFactory = objectFactory;
			this.ProcessingEngine = processingEngine;
			this.Serialization = serialization;
		}

		public static string Accept(IHeaderDictionary headers)
		{
			StringValues header;
			if (headers.TryGetValue("accept", out header))
				return header[0];
			return "application/json";
		}

		public void PassThrough<TCommand, TArgument>(HttpContext context, TArgument argument)
		{
			var accept = Accept(context.Request.Headers);
			PassThrough<TCommand, TArgument>(argument, accept, context, NoCommands);
		}

		private static readonly AdditionalCommand[] NoCommands = new AdditionalCommand[0];

		public void PassThrough<TCommand, TArgument>(
			TArgument argument, 
			string accept,
			HttpContext contex,
			AdditionalCommand[] additionalCommands)
		{
			var request = contex.Request;
			var response = contex.Response;
			var start = Stopwatch.GetTimestamp();
			var engine = ProcessingEngine;
			StringValues headers;
			if (request.Headers.TryGetValue("x-revenj-session-id", out headers) && headers.Count == 1)
			{
				var sessionID = headers[0];
				var scope = ObjectFactory.FindScope(sessionID);
				if (scope == null)
				{
					response.WriteError("Unknown session: " + sessionID, HttpStatusCode.BadRequest);
					return;
				}
				engine = scope.Resolve<IProcessingEngine>();
			}
			var commands = new ObjectCommandDescription[1 + (additionalCommands != null ? additionalCommands.Length : 0)];
			commands[0] = new ObjectCommandDescription { Data = argument, CommandType = typeof(TCommand) };
			for (int i = 1; i < commands.Length; i++)
			{
				var ac = additionalCommands[i - 1];
				commands[i] = new ObjectCommandDescription { RequestID = ac.ToHeader, CommandType = ac.CommandType, Data = ac.Argument };
			}
			var stream = RestApplication.ExecuteCommands<object>(engine, Serialization, commands, contex, accept);
			var elapsed = (decimal)(Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
			response.Headers.Add("X-Duration", elapsed.ToString(CultureInfo.InvariantCulture));
			var cms = stream as ChunkedMemoryStream;
			if (cms != null)
				cms.CopyTo(response.Body);
			else if (stream != null)
				stream.CopyTo(response.Body);
		}

		public void ConvertStream<TCommand, TArgument>(HttpContext contex, TArgument argument)
		{
			if (argument == null)
			{
				Application.Execute(typeof(TCommand), null, contex);
				return;
			}
			using (var ms = ChunkedMemoryStream.Create())
			{
				Serialization.Serialize(argument, contex.Request.ContentType, ms);
				ms.Position = 0;
				Application.Execute(typeof(TCommand), ms, contex);
			}
		}
	}

}
