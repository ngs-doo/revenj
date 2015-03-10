﻿using System;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Runtime.Serialization.Formatters;
using System.Runtime.Serialization.Formatters.Binary;
using System.ServiceModel;
using System.Text;
using System.Web;
using System.Xml.Linq;
using Revenj.Api;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Wcf
{
	[ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
	public class RestApplication : IRestApplication
	{
		private readonly IProcessingEngine ProcessingEngine;
		private readonly IObjectFactory ObjectFactory;
		private readonly IWireSerialization Serialization;
		private readonly IPluginRepository<IServerCommand> CommandsRepository;

		public RestApplication(
			IProcessingEngine processingEngine,
			IObjectFactory objectFactory,
			IWireSerialization serialization,
			IPluginRepository<IServerCommand> commandsRepository)
		{
			Contract.Requires(processingEngine != null);
			Contract.Requires(objectFactory != null);
			Contract.Requires(serialization != null);
			Contract.Requires(commandsRepository != null);

			this.ProcessingEngine = processingEngine;
			this.ObjectFactory = objectFactory;
			this.Serialization = serialization;
			this.CommandsRepository = commandsRepository;
		}

		class ExecuteResult
		{
			public Stream Error;
			public object Result;
		}

		private static ExecuteResult Execute<TInput>(
			IProcessingEngine engine,
			IServerCommandDescription<TInput>[] commands)
		{
			var result = engine.Execute<TInput, object>(commands);
			var first = result.ExecutedCommandResults != null ? result.ExecutedCommandResults.FirstOrDefault() : null;
			ThreadContext.Response.StatusCode = first != null && first.Result != null
				? first.Result.Status
				: result.Status;

			if (result.Status == HttpStatusCode.ServiceUnavailable)
				HttpRuntime.UnloadAppDomain();

			if ((int)result.Status >= 300)
				return new ExecuteResult { Error = Utility.ReturnError(result.Message, result.Status) };

			if (first == null)
				return new ExecuteResult { Error = Utility.ReturnError("Missing result", HttpStatusCode.InternalServerError) };

			if ((int)first.Result.Status >= 300)
				return new ExecuteResult { Error = Utility.ReturnError(first.Result.Message, first.Result.Status) };

			foreach (var ar in result.ExecutedCommandResults.Skip(1))
				ThreadContext.Response.Headers[ar.RequestID] = ar.Result.Data.ToString();

			return new ExecuteResult { Result = first.Result.Data };
		}

		public Stream Get()
		{
			return Post(null);
		}

		public Stream Post(Stream message)
		{
			var request = ThreadContext.Request;
			var template = request.UriTemplateMatch;
			var command = template.RelativePathSegments.Count > 0 ? template.RelativePathSegments[0] : null;

			if (command == null)
				return Utility.ReturnError("Command not specified", HttpStatusCode.BadRequest);

			var commandType = CommandsRepository.Find(command);
			if (commandType == null)
				return Utility.ReturnError("Unknown command " + command, HttpStatusCode.NotFound);

			var start = Stopwatch.GetTimestamp();

			var accept = (request.Accept ?? "application/xml").ToLowerInvariant();

			var engine = ProcessingEngine;
			var sessionID = request.GetHeader("X-Revenj-Session-ID");
			if (sessionID != null)
			{
				var scope = ObjectFactory.FindScope(sessionID);
				if (scope == null)
					return Utility.ReturnError("Unknown session: " + sessionID, HttpStatusCode.BadRequest);
				engine = scope.Resolve<IProcessingEngine>();
			}

			Stream stream;
			switch (request.ContentType)
			{
				case "application/json":
					stream = ExecuteCommands(engine, Serialization, new[] { new JsonCommandDescription(template.QueryParameters, message, commandType) }, accept);
					break;
				case "application/x-protobuf":
					stream = ExecuteCommands(engine, Serialization, new[] { new ProtobufCommandDescription(template.QueryParameters, message, commandType) }, accept);
					break;
				default:
					if (message != null)
					{
						XElement el;
						try { el = XElement.Load(message); }
						catch (Exception ex) { return Utility.ReturnError("Error parsing request body. " + ex.Message, HttpStatusCode.BadRequest); }
						stream = ExecuteCommands(engine, Serialization, new[] { new XmlCommandDescription(el, commandType) }, accept);
					}
					else stream = ExecuteCommands(engine, Serialization, new[] { new XmlCommandDescription(template.QueryParameters, commandType) }, accept);
					break;
			}
			var elapsed = (decimal)(Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
			ThreadContext.Response.Headers.Add("X-Duration", elapsed.ToString(CultureInfo.InvariantCulture));
			return stream;
		}

		internal static Stream ExecuteCommands<TFormat>(
			IProcessingEngine engine,
			IWireSerialization serialization,
			IServerCommandDescription<TFormat>[] commands,
			string accept)
		{
			var result = Execute(engine, commands);
			if (result.Error != null)
				return result.Error;
			if (result.Result == null)
			{
				ThreadContext.Response.ContentType = accept;
				return null;
			}
			if (accept == "application/octet-stream")
			{
				ThreadContext.Response.ContentType = "application/octet-stream";
				if (result.Result is Stream)
					return result.Result as Stream;
				else if (result.Result is StreamReader)
					return (result.Result as StreamReader).BaseStream;
				else if (result.Result is StringBuilder)
				{
					var sb = result.Result as StringBuilder;
					var cms = ChunkedMemoryStream.Create();
					var sw = cms.GetWriter();
					for (int i = 0; i < sb.Length; )
					{
						var min = Math.Min(sb.Length - i, cms.CharBuffer.Length);
						sb.CopyTo(i, cms.CharBuffer, 0, min);
						i += min;
						sw.Write(cms.CharBuffer, 0, min);
					}
					sw.Flush();
					cms.Position = 0;
					return cms;
				}
				else if (result.Result is byte[])
					return new MemoryStream(result.Result as byte[]);
				else if (result.Result is string)
				{
					var cms = ChunkedMemoryStream.Create();
					var sw = cms.GetWriter();
					sw.Write(result.Result as string);
					sw.Flush();
					cms.Position = 0;
					return cms;
				}
				else if (result.Result is char[])
				{
					var cms = ChunkedMemoryStream.Create();
					var sw = cms.GetWriter();
					sw.Write(result.Result as char[]);
					sw.Flush();
					cms.Position = 0;
					return cms;
				}
				return Utility.ReturnError(
					"Unexpected command result. Can't convert "
					+ result.Result.GetType().FullName + " to octet-stream. Use application/x-dotnet mime type for .NET binary serialization",
					HttpStatusCode.BadRequest);
			}
			if (accept == "application/base64")
			{
				ThreadContext.Response.ContentType = "application/base64";
				if (result.Result is Stream)
				{
					var stream = result.Result as Stream;
					try
					{
						var cms = stream as ChunkedMemoryStream;
						if (cms != null)
							return cms.ToBase64Stream();
						else
						{
							cms = new ChunkedMemoryStream(stream);
							try { return cms.ToBase64Stream(); }
							finally { cms.Dispose(); }
						}
					}
					finally { stream.Dispose(); }
				}
				else if (result.Result is StreamReader)
				{
					var sr = result.Result as StreamReader;
					try
					{
						var cms = sr.BaseStream as ChunkedMemoryStream;
						if (cms != null)
							return cms.ToBase64Stream();
						else
						{
							cms = new ChunkedMemoryStream(sr.BaseStream);
							try { return cms.ToBase64Stream(); }
							finally { cms.Dispose(); }
						}
					}
					finally { sr.Dispose(); }
				}
				else if (result.Result is StringBuilder)
				{
					var sb = result.Result as StringBuilder;
					using (var cms = ChunkedMemoryStream.Create())
					{
						var sw = cms.GetWriter();
						for (int i = 0; i < sb.Length; )
						{
							var min = Math.Min(sb.Length - i, cms.CharBuffer.Length);
							sb.CopyTo(i, cms.CharBuffer, 0, min);
							i += min;
							sw.Write(cms.CharBuffer, 0, min);
						}
						sw.Flush();
						cms.Position = 0;
						return cms.ToBase64Stream();
					}
				}
				else if (result.Result is byte[])
				{
					var bytes = result.Result as byte[];
					using (var cms = ChunkedMemoryStream.Create())
					{
						cms.Write(bytes, 0, bytes.Length);
						cms.Position = 0;
						return cms.ToBase64Stream();
					}
				}
				else if (result.Result is string)
				{
					using (var cms = ChunkedMemoryStream.Create())
					{
						var sw = cms.GetWriter();
						sw.Write(result.Result as string);
						sw.Flush();
						cms.Position = 0;
						return cms.ToBase64Stream();
					}
				}
				else if (result.Result is char[])
				{
					using (var cms = ChunkedMemoryStream.Create())
					{
						var sw = cms.GetWriter();
						sw.Write(result.Result as char[]);
						sw.Flush();
						cms.Position = 0;
						return cms.ToBase64Stream();
					}
				}
				return Utility.ReturnError("Unexpected command result. Can't convert to base64.", HttpStatusCode.BadRequest);
			}
			if (accept == "application/x-dotnet")
			{
				ThreadContext.Response.ContentType = "application/x-dotnet";
				var bf = new BinaryFormatter();
				bf.AssemblyFormat = FormatterAssemblyStyle.Simple;
				var cms = ChunkedMemoryStream.Create();
				bf.Serialize(cms, result.Result);
				cms.Position = 0;
				return cms;
			}
			var ms = ChunkedMemoryStream.Create();
			ThreadContext.Response.ContentType = serialization.Serialize(result.Result, accept, ms);
			ms.Position = 0;
			return ms;
		}
	}
}
