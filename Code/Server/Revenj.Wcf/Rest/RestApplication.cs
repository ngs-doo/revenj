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
using NGS.Extensibility;
using NGS.Serialization;
using NGS.Utility;
using Revenj.Api;
using Revenj.Processing;

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

		class ExecuteResult<TFormat>
		{
			public Stream Error;
			public TFormat Result;
		}

		private static ExecuteResult<TOutput> Execute<TInput, TOutput>(IProcessingEngine engine, IServerCommandDescription<TInput> command)
		{
			var result = engine.Execute<TInput, TOutput>(new[] { command });
			var first = result.ExecutedCommandResults != null ? result.ExecutedCommandResults.FirstOrDefault() : null;
			ThreadContext.Response.StatusCode = first != null && first.Result != null
				? first.Result.Status
				: result.Status;

			if (result.Status == HttpStatusCode.ServiceUnavailable)
				HttpRuntime.UnloadAppDomain();

			if ((int)result.Status >= 300)
				return new ExecuteResult<TOutput> { Error = Utility.ReturnError(result.Message, result.Status) };

			if (first == null)
				return new ExecuteResult<TOutput> { Error = Utility.ReturnError("Missing result", HttpStatusCode.InternalServerError) };

			if ((int)first.Result.Status >= 300)
				return new ExecuteResult<TOutput> { Error = Utility.ReturnError(first.Result.Message, first.Result.Status) };

			return new ExecuteResult<TOutput> { Result = first.Result.Data };
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
			var sessionID = request.GetHeader("X-NGS-Session-ID");
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
					stream = ExecuteCommand(engine, Serialization, new JsonCommandDescription(template.QueryParameters, message, commandType), accept);
					break;
				case "application/x-protobuf":
					stream = ExecuteCommand(engine, Serialization, new ProtobufCommandDescription(template.QueryParameters, message, commandType), accept);
					break;
				default:
					if (message != null)
					{
						XElement el;
						try { el = XElement.Load(message); }
						catch (Exception ex) { return Utility.ReturnError("Error parsing request body. " + ex.Message, HttpStatusCode.BadRequest); }
						stream = ExecuteCommand(engine, Serialization, new XmlCommandDescription(el, commandType), accept);
					}
					else stream = ExecuteCommand(engine, Serialization, new XmlCommandDescription(template.QueryParameters, commandType), accept);
					break;
			}
			var elapsed = (decimal)(Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
			ThreadContext.Response.Headers.Add("X-Duration", elapsed.ToString(CultureInfo.InvariantCulture));
			return stream;
		}

		internal static Stream ExecuteCommand<TFormat>(
			IProcessingEngine engine,
			IWireSerialization serialization,
			IServerCommandDescription<TFormat> command,
			string accept)
		{
			if (accept == "application/json-experimental")
			{
				var instance = Execute<TFormat, object>(engine, command);
				if (instance.Error != null)
					return instance.Error;
				if (instance.Result == null)
					return null;
				var cms = ChunkedMemoryStream.Create();
				var ct = serialization.Serialize(instance.Result, accept, cms);
				ThreadContext.Response.ContentType = ct;
				cms.Position = 0;
				return cms;
			}
			if (accept.Contains("application/json"))
			{
				var json = Execute<TFormat, StreamReader>(engine, command);
				if (json.Error != null)
					return json.Error;
				ThreadContext.Response.ContentType = "application/json";
				if (json.Result == null)
					return null;
				return json.Result.BaseStream;
			}
			if (accept.Contains("application/octet-stream"))
			{
				var native = Execute<TFormat, object>(engine, command);
				if (native.Error != null)
					return native.Error;
				ThreadContext.Response.ContentType = "application/octet-stream";
				if (native.Result == null)
					return null;
				if (native.Result is Stream)
					return native.Result as Stream;
				else if (native.Result is StreamReader)
					return (native.Result as StreamReader).BaseStream;
				//Warning LOH leak
				else if (native.Result is StringBuilder)
					return (native.Result as StringBuilder).ToStream();
				//Warning LOH leak
				else if (native.Result is byte[])
					return new MemoryStream(native.Result as byte[]);
				//Warning LOH leak
				else if (native.Result is string)
					return new StringBuilder(native.Result as string).ToStream();
				//Warning LOH leak
				else if (native.Result is char[])
				{
					var ch = native.Result as char[];
					var sb = new StringBuilder(ch.Length);
					sb.Append(ch);
					return sb.ToStream();
				}
				return Utility.ReturnError(
					"Unexpected command result. Can't convert "
					+ native.Result.GetType().FullName + " to octet-stream. Use application/x-dotnet mime type for .NET binary serialization",
					HttpStatusCode.BadRequest);
			}
			if (accept.Contains("application/base64"))
			{
				var native = Execute<TFormat, object>(engine, command);
				if (native.Error != null)
					return native.Error;
				ThreadContext.Response.ContentType = "application/base64";
				if (native.Result == null)
					return null;
				if (native.Result is Stream)
				{
					var stream = native.Result as Stream;
					try { return stream.ToBase64Stream(); }
					finally { stream.Dispose(); }
				}
				else if (native.Result is StreamReader)
				{
					var sr = native.Result as StreamReader;
					try { return sr.BaseStream.ToBase64Stream(); }
					finally { sr.Dispose(); }
				}
				//Warning LOH leak
				else if (native.Result is StringBuilder)
				{
					var sb = native.Result as StringBuilder;
					return sb.ToBase64Stream();
				}
				//Warning LOH leak
				else if (native.Result is byte[])
				{
					var bytes = native.Result as byte[];
					using (var cms = ChunkedMemoryStream.Create())
					{
						cms.Write(bytes, 0, bytes.Length);
						cms.Position = 0;
						return cms.ToBase64Stream();
					}
				}
				//Warning LOH leak
				else if (native.Result is string)
				{
					var sb = new StringBuilder(native.Result as string);
					return sb.ToBase64Stream();
				}
				//Warning LOH leak
				else if (native.Result is char[])
				{
					var ch = native.Result as char[];
					var sb = new StringBuilder(ch.Length);
					sb.Append(ch);
					return sb.ToBase64Stream();
				}
				return Utility.ReturnError("Unexpected command result. Cant convert to base64.", HttpStatusCode.BadRequest);
			}
			if (accept.Contains("application/x-protobuf"))
			{
				var proto = Execute<TFormat, Stream>(engine, command);
				if (proto.Error != null)
					return proto.Error;
				ThreadContext.Response.ContentType = "application/x-protobuf";
				return proto.Result;
			}
			if (accept.Contains("application/x-dotnet"))
			{
				var native = Execute<TFormat, object>(engine, command);
				if (native.Error != null)
					return native.Error;
				ThreadContext.Response.ContentType = "application/x-dotnet";
				if (native.Result == null)
					return null;
				var bf = new BinaryFormatter();
				bf.AssemblyFormat = FormatterAssemblyStyle.Simple;
				var cms = ChunkedMemoryStream.Create();
				bf.Serialize(cms, native.Result);
				cms.Position = 0;
				return cms;
			}
			var xml = Execute<TFormat, XElement>(engine, command);
			if (xml.Error != null)
				return xml.Error;
			ThreadContext.Response.ContentType = "application/xml";
			if (xml.Result == null)
				return null;
			var ms = ChunkedMemoryStream.Create();
			xml.Result.Save(ms);
			ms.Position = 0;
			return ms;
		}
	}
}
