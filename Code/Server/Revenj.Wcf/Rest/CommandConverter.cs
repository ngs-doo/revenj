using System;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Globalization;
using System.IO;
using System.Net;
using System.Xml.Linq;
using Revenj.Api;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Wcf
{
	public class CommandConverter : ICommandConverter
	{
		private readonly IRestApplication Application;
		private readonly IObjectFactory ObjectFactory;
		private readonly IProcessingEngine ProcessingEngine;
		private readonly IWireSerialization Serialization;
		private readonly ISerialization<Stream> Protobuf;
		private readonly ISerialization<XElement> Xml;
		private readonly ISerialization<StreamReader> Json;

		public CommandConverter(
			IRestApplication application,
			IObjectFactory objectFactory,
			IProcessingEngine processingEngine,
			IWireSerialization serialization,
			ISerialization<Stream> protobuf,
			ISerialization<XElement> xml,
			ISerialization<StreamReader> json)
		{
			Contract.Requires(application != null);
			Contract.Requires(objectFactory != null);
			Contract.Requires(processingEngine != null);
			Contract.Requires(serialization != null);
			Contract.Requires(protobuf != null);
			Contract.Requires(xml != null);
			Contract.Requires(json != null);

			this.Application = application;
			this.ObjectFactory = objectFactory;
			this.ProcessingEngine = processingEngine;
			this.Serialization = serialization;
			this.Protobuf = protobuf;
			this.Xml = xml;
			this.Json = json;
		}

		public Stream PassThrough<TCommand, TArgument>(TArgument argument, string accept)
		{
			var start = Stopwatch.GetTimestamp();
			var engine = ProcessingEngine;
			var sessionID = ThreadContext.Request.GetHeader("X-Revenj-Session-ID");
			if (sessionID != null)
			{
				var scope = ObjectFactory.FindScope(sessionID);
				if (scope == null)
					return Utility.ReturnError("Unknown session: " + sessionID, HttpStatusCode.BadRequest);
				engine = scope.Resolve<IProcessingEngine>();
			}
			var stream =
				RestApplication.ExecuteCommand<object>(
					engine,
					Serialization,
					new ObjectCommandDescription { Data = argument, CommandType = typeof(TCommand) },
					accept);
			var elapsed = (decimal)(Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
			ThreadContext.Response.Headers.Add("X-Duration", elapsed.ToString(CultureInfo.InvariantCulture));
			return stream;
		}

		public Stream ConvertStream<TCommand, TArgument>(TArgument argument)
		{
			var match = new UriTemplateMatch();
			match.RelativePathSegments.Add(typeof(TCommand).FullName);
			ThreadContext.Request.UriTemplateMatch = match;
			if (argument == null)
				return Application.Get();
			switch (ThreadContext.Request.ContentType)
			{
				case "application/x-protobuf":
					return Application.Post(Protobuf.Serialize(argument));
				case "application/json":
					return Application.Post(Json.Serialize(argument).BaseStream);
			}
			using (var ms = ChunkedMemoryStream.Create())
			{
				var sw = ms.GetWriter();
				sw.Write(Xml.Serialize(argument));
				sw.Flush();
				ms.Position = 0;
				return Application.Post(ms);
			}
		}
	}
}
