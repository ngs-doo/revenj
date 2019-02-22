using Microsoft.AspNetCore.Http;
using System.Linq;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Serialization;
using Revenj.Utility;
using System;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Net;
using System.Runtime.Serialization.Formatters;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using System.Xml.Linq;
using Microsoft.Extensions.Primitives;
using System.Security.Principal;
using System.Threading.Tasks;

namespace Revenj.AspNetCore
{
	public class RestApplication
	{
		private readonly IProcessingEngine ProcessingEngine;
		private readonly IObjectFactory ObjectFactory;
		private readonly IWireSerialization Serialization;

		public RestApplication(
			IProcessingEngine processingEngine,
			IObjectFactory objectFactory,
			IWireSerialization serialization)
		{
			this.ProcessingEngine = processingEngine;
			this.ObjectFactory = objectFactory;
			this.Serialization = serialization;
		}

		class ExecuteResult
		{
			public string Error;
			public object Result;
		}

		private static async Task<ExecuteResult> Execute<TInput>(
			IProcessingEngine engine,
			IServerCommandDescription<TInput>[] commands,
			IPrincipal principal,
			HttpResponse response)
		{
			//TODO: make it truly async. as a temporary workaroung just call engine in a thread
			var result = await Task.Run(() => engine.Execute<TInput, object>(commands, principal));
			var first = result.ExecutedCommandResults != null ? result.ExecutedCommandResults.FirstOrDefault() : null;
			response.StatusCode = (int)(first != null && first.Result != null
				? first.Result.Status
				: result.Status);

			var noResult = first == null || first.Result == null || first.Result.Data == null;

			if ((int)result.Status >= 300 && noResult)
				return new ExecuteResult { Error = result.Message };
			if (first == null)
				return new ExecuteResult { Error = "Missing result" };
			if ((int)first.Result.Status >= 300 && noResult)
				return new ExecuteResult { Error = first.Result.Message };

			foreach (var ar in result.ExecutedCommandResults.Skip(1))
				response.Headers.Add(ar.RequestID, ar.Result.Data.ToString());

			return new ExecuteResult { Result = first.Result.Data };
		}

		public Task Execute(Type commandType, Stream message, HttpContext context)
		{
			var request = context.Request;
			var response = context.Response;
			StringValues headers;
			string accept;
			if (request.Headers.TryGetValue("accept", out headers) && headers.Count > 0)
				accept = headers[0];
			else
				accept = "application/json";
			var start = Stopwatch.GetTimestamp();

			var engine = ProcessingEngine;
			if (request.Headers.TryGetValue("x-revenj-session-id", out headers) && headers.Count > 0)
			{
				var sessionID = headers[0];
				var scope = ObjectFactory.FindScope(sessionID);
				if (scope == null)
					return context.Response.WriteError("Unknown session: " + sessionID, HttpStatusCode.BadRequest);
				engine = scope.Resolve<IProcessingEngine>();
			}

			Task<Stream> task;
			switch (request.ContentType)
			{
				case "application/json":
					task =
						ExecuteCommands(
							engine,
							Serialization,
							new[] { new JsonCommandDescription(request.Query, message, commandType) },
							context,
							accept);
					break;
				case "application/x-protobuf":
					task =
						ExecuteCommands(
							engine,
							Serialization,
							new[] { new ProtobufCommandDescription(request.Query, message, commandType) },
							context,
							accept);
					break;
				default:
					if (message != null)
					{
						XElement el;
						try { el = XElement.Load(message); }
						catch (Exception ex)
						{
							return response.WriteError(
								"Error parsing request body as XML. " + ex.Message,
								request.ContentType == null ? HttpStatusCode.UnsupportedMediaType : HttpStatusCode.BadRequest);
						}
						task =
							ExecuteCommands(
								engine,
								Serialization,
								new[] { new XmlCommandDescription(el, commandType) },
								context,
								accept);
					}
					else
					{
						task =
							ExecuteCommands(
								engine,
								Serialization,
								new[] { new XmlCommandDescription(request.Query, commandType) },
								context,
								accept);
					}
					break;
			}
			return task.ContinueWith(s =>
			{
				if (s.IsCompletedSuccessfully)
				{
					var elapsed = (decimal)(Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
					response.Headers.Add("X-Duration", elapsed.ToString(CultureInfo.InvariantCulture));
					var stream = s.Result;
					var cms = stream as ChunkedMemoryStream;
					if (cms != null)
					{
						cms.CopyTo(response.Body);
						cms.Dispose();
					}
					else if (task != null)
					{
						stream.CopyTo(response.Body);
						stream.Dispose();
					}
				}
			});
		}

		internal static async Task<Stream> ExecuteCommands<TFormat>(
			IProcessingEngine engine,
			IWireSerialization serialization,
			IServerCommandDescription<TFormat>[] commands,
			HttpContext context,
			string accept)
		{
			var response = context.Response;
			var result = await Execute(engine, commands, context.User, response);
			if (result.Error != null)
			{
				context.Response.ContentType = "text/plain; charset=UTF-8";
				return new MemoryStream(Encoding.UTF8.GetBytes(result.Error));
			}
			if (result.Result == null)
			{
				response.ContentType = accept;
				return default(Stream);
			}
			if (accept == "application/octet-stream")
			{
				response.ContentType = "application/octet-stream";
				if (result.Result is Stream)
					return result.Result as Stream;
				else if (result.Result is StreamReader)
					return (result.Result as StreamReader).BaseStream;
				else if (result.Result is StringBuilder)
				{
					var sb = result.Result as StringBuilder;
					var cms = ChunkedMemoryStream.Create();
					var sw = cms.GetWriter();
					for (int i = 0; i < sb.Length;)
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
				response.StatusCode = (int)HttpStatusCode.UnsupportedMediaType;
				var message = "Unexpected command result. Can't convert "
					+ result.Result.GetType().FullName + " to octet-stream. Use application/x-dotnet mime type for .NET binary serialization";
				return new MemoryStream(Encoding.UTF8.GetBytes(message));
			}
			if (accept == "application/base64")
			{
				response.ContentType = "application/base64";
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
						for (int i = 0; i < sb.Length;)
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
				response.StatusCode = (int)HttpStatusCode.UnsupportedMediaType;
				return Base64Error;
			}
			if (accept == "application/x-dotnet")
			{
				response.ContentType = "application/x-dotnet";
				var bf = new BinaryFormatter();
				bf.AssemblyFormat = FormatterAssemblyStyle.Simple;
				var cms = ChunkedMemoryStream.Create();
				bf.Serialize(cms, result.Result);
				cms.Position = 0;
				return cms;
			}
			var ms = ChunkedMemoryStream.Create();
			response.ContentType = serialization.Serialize(result.Result, accept, ms);
			ms.Position = 0;
			return ms;
		}

		private static readonly MemoryStream Base64Error = new MemoryStream(Encoding.UTF8.GetBytes("Unexpected command result. Can't convert to base64."));
	}
}
