using System;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.IO;
using System.Runtime.Serialization;
using System.Xml;
using System.Xml.Linq;
using Revenj.Common;
using Revenj.Extensibility;
using Revenj.Utility;

namespace Revenj.Serialization
{
	internal class XmlSerialization : ISerialization<XElement>
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Serialization");

		private readonly DataContractResolver GenericResolver;
		private readonly ITypeResolver TypeResolver;
		private readonly GenericDeserializationBinder GenericBinder;

		public XmlSerialization(
			ITypeResolver typeResolver,
			GenericDataContractResolver genericResolver,
			GenericDeserializationBinder genericBinder)
		{
			Contract.Requires(typeResolver != null);
			Contract.Requires(genericResolver != null);
			Contract.Requires(genericBinder != null);

			this.TypeResolver = typeResolver;
			this.GenericResolver = genericResolver;
			this.GenericBinder = genericBinder;
		}

		public XElement Serialize<T>(T value)
		{
			var declaredType = typeof(T);
			var type = value != null ? value.GetType() : declaredType;
			var settings = new XmlWriterSettings();
			settings.CheckCharacters = false;
			settings.NewLineHandling = NewLineHandling.Entitize;
			using (var cms = ChunkedMemoryStream.Create())
			using (var xw = XmlWriter.Create(cms, settings))
			using (var dw = XmlDictionaryWriter.CreateDictionaryWriter(xw))
			{
				var serializer = new DataContractSerializer(type);
				serializer.WriteObject(dw, value, GenericResolver);
				dw.Flush();
				cms.Position = 0;
				using (var sr = new StreamReader(cms))
				{
					var doc = XElement.Load(sr);
					if (type != declaredType || !(declaredType.IsClass || declaredType.IsValueType))
						doc.Add(new XAttribute("type", type.FullName));
					return doc;
				}
			}
		}

		public T Deserialize<T>(XElement data, StreamingContext context)
		{
			var declaredType = typeof(T);
			var atr = data.Attribute("type");
			var type = atr != null ? TypeResolver.Resolve(atr.Value) ?? declaredType : declaredType;
			if (!declaredType.IsAssignableFrom(type) || atr == null)
			{
				if (declaredType.IsClass || declaredType.IsValueType)
					type = declaredType;
				else if (atr == null)
				{
					TraceSource.TraceEvent(TraceEventType.Verbose, 5201, "{0}", data);
					throw new FrameworkException(@"Couldn't resolve type from provided Xml. 
Root element should embed type attribute with class name or you should provide appropriate type T to Deserialize<T> method.
Trying to deserialize {0}.".With(declaredType.FullName));
				}
				else
				{
					TraceSource.TraceEvent(TraceEventType.Verbose, 5201, "{0}", data);
					throw new FrameworkException(@"Can't deserialize provided Xml to {0}. 
Type detected for Xml is {1}. Can't deserialize Xml to instance of {1}.".With(declaredType.FullName, type.FullName));
				}
			}
			var document = new XmlDocument();
			using (var newReader = data.CreateReader())
			{
				document.AppendChild(document.ReadNode(newReader));
				using (var oldReader = new XmlNodeReader(document.DocumentElement))
					return (T)Deserialize(type, oldReader, context);
			}
		}

		private object Deserialize(Type type, XmlReader reader, StreamingContext context)
		{
			using (var dict = XmlDictionaryReader.CreateDictionaryReader(reader))
			{
				var serializer = new DataContractSerializer(type);
				var result = serializer.ReadObject(dict, false, GenericResolver);
				if (context.Context == null)
					return result;
				//TODO NO need for actual xml serializer now
				//implement recursive descent and provide context to all objects
				using (var cms = ChunkedMemoryStream.Create())
				{
					var ns = new NetDataContractSerializer(context);
					try
					{
						ns.Serialize(cms, result);
						cms.Position = 0;
						ns.Binder = GenericBinder;
						return ns.Deserialize(cms);
					}
					catch (Exception ex)
					{
						TraceSource.TraceEvent(TraceEventType.Error, 5202, "{0}", ex);
						TraceSource.TraceEvent(TraceEventType.Verbose, 5202, "{0}", cms);
						throw;
					}
				}
			}
		}

		public void Serialize(object value, Stream s)
		{
			var serializer = new DataContractSerializer(value.GetType());
			serializer.WriteObject(s, value);
		}

		public object Deserialize(Stream s, Type target, StreamingContext context)
		{
			var serializer = new DataContractSerializer(target);
			var result = serializer.ReadObject(s);
			if (context.Context == null)
				return result;
			//TODO fix double serialization because of context
			using (var cms = ChunkedMemoryStream.Create())
			{
				var ns = new NetDataContractSerializer(context);
				ns.Serialize(cms, result);
				ns.Binder = GenericBinder;
				cms.Position = 0;
				return ns.Deserialize(cms);
			}
		}
	}
}
