using System.IO;
using System.Runtime.Serialization;
using System.Xml.Linq;
using Revenj.Extensibility;

namespace Revenj.Serialization
{
	public static class Setup
	{
		public static void ConfigureSerialization(this IObjectFactoryBuilder builder)
		{
			builder.RegisterType<GenericDataContractResolver>(InstanceScope.Singleton);
			builder.RegisterType(typeof(XmlSerialization), InstanceScope.Singleton, false, typeof(ISerialization<XElement>));
			builder.RegisterType<GenericDeserializationBinder, GenericDeserializationBinder, SerializationBinder>(InstanceScope.Singleton);
			builder.RegisterType(typeof(BinarySerialization), InstanceScope.Singleton, false, typeof(ISerialization<byte[]>));
			builder.RegisterType(typeof(JsonSerialization), InstanceScope.Singleton, false, typeof(ISerialization<string>), typeof(ISerialization<TextReader>));
			builder.RegisterType(typeof(ProtobufSerialization), InstanceScope.Singleton, false, typeof(ISerialization<MemoryStream>), typeof(ISerialization<Stream>));
			builder.RegisterType(typeof(PassThroughSerialization), InstanceScope.Singleton, false, typeof(ISerialization<object>));
			builder.RegisterType<WireSerialization, IWireSerialization>(InstanceScope.Singleton);
		}
	}
}
