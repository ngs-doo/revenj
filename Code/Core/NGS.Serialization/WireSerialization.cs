using System;
using System.IO;
using System.Runtime.Serialization;
using NGS.Logging;

namespace NGS.Serialization
{
	public class WireSerialization : IWireSerialization
	{
		private readonly XmlSerialization Xml;
		private readonly JsonSerialization Json;
		private readonly ProtobufSerialization Protobuf;
		private readonly PassThroughSerialization Pass;

		public WireSerialization(
			ILogFactory logFactory,
			GenericDeserializationBinder binder)
		{
			Xml = new XmlSerialization(null, null, binder, logFactory);
			Json = new JsonSerialization(binder, logFactory);
			Protobuf = new ProtobufSerialization();
			Pass = new PassThroughSerialization();
		}

		public string Serialize(object value, string accept, Stream destination)
		{
			//fast path
			if (accept == "application/json")
			{
				Json.Serialize(value, destination);
				return "application/json";
			}
			if (accept == "application/x-protobuf")
			{
				Protobuf.Serialize(value, destination);
				return "application/x-protobuf";
			}
			if (accept == "application/xml")
			{
				Xml.Serialize(value, destination);
				return "application/xml";
			}
			if (accept == "application/json-experimental")
			{
				Json.SerializeJsonObject(value, destination);
				return "application/json";
			}
			//Slow path
			accept = (accept ?? "application/json").ToLowerInvariant();
			if (accept.Contains("application/json"))
			{
				Json.Serialize(value, destination);
				return "application/json";
			}
			if (accept.Contains("application/x-protobuf"))
			{
				Protobuf.Serialize(value, destination);
				return "application/x-protobuf";
			}
			Xml.Serialize(value, destination);
			return "application/xml";
		}

		public object Deserialize(Stream source, Type target, string contentType, StreamingContext context)
		{
			if (source == null)
				return null;
			//fast path
			if (contentType == "application/json")
				return Json.DeserializeJson(source, target, context);
			if (contentType == "application/json-experimental")
				return Json.Deserialize(source, target, context);
			if (contentType == "application/x-protobuf")
				return Protobuf.Deserialize(source, target, context);
			if (contentType == "application/xml")
				return Xml.Deserialize(source, target, context);
			//slow path
			contentType = (contentType ?? "application/json").ToLowerInvariant().TrimStart();
			if (contentType.StartsWith("application/json", StringComparison.InvariantCulture))
				return Json.Deserialize(source, target, context);
			if (contentType.StartsWith("application/x-protobuf", StringComparison.InvariantCulture))
				return Protobuf.Deserialize(source, target, context);
			return Xml.Deserialize(source, target, context);
		}

		public ISerialization<TFormat> GetSerializer<TFormat>()
		{
			return Xml as ISerialization<TFormat>
				?? Json as ISerialization<TFormat>
				?? Protobuf as ISerialization<TFormat>
				?? Pass as ISerialization<TFormat>;
		}
	}
}
