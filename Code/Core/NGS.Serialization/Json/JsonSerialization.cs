using System;
using System.Diagnostics.Contracts;
using System.IO;
using System.Reflection;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using NGS.Logging;
using NGS.Utility;

namespace NGS.Serialization
{
	public class JsonSerialization : ISerialization<string>, ISerialization<StringBuilder>, ISerialization<StreamReader>
	{
		private readonly GenericDeserializationBinder Binder;
		private readonly ILogger Logger;
		private readonly JsonSerializer SharedSerializer;

		public JsonSerialization(
			GenericDeserializationBinder binder,
			ILogFactory logFactory)
		{
			Contract.Requires(binder != null);
			Contract.Requires(logFactory != null);

			this.Binder = binder;
			this.Logger = logFactory.Create(GetType().FullName);
			SharedSerializer = new JsonSerializer();
			SharedSerializer.Converters.Add(new StringEnumConverter());
			SharedSerializer.TypeNameHandling = TypeNameHandling.Auto;
			SharedSerializer.TypeNameAssemblyFormat = FormatterAssemblyStyle.Simple;
			SharedSerializer.Binder = binder;
		}

		public string Serialize<T>(T value)
		{
			var declaredType = typeof(T);
			var type = value != null ? value.GetType() : declaredType;
			var settings = new JsonSerializerSettings();
			settings.Converters.Add(new StringEnumConverter());
			settings.TypeNameHandling = type != declaredType || !(declaredType.IsClass || declaredType.IsValueType) ? TypeNameHandling.Objects : TypeNameHandling.Auto;
			settings.Binder = Binder;
			return JsonConvert.SerializeObject(value, settings);
		}

		public T Deserialize<T>(string data, StreamingContext context)
		{
			var settings = new JsonSerializerSettings();
			settings.TypeNameHandling = TypeNameHandling.Auto;
			settings.Context = context;
			settings.Binder = Binder;
			try
			{
				return (T)JsonConvert.DeserializeObject<T>(data, settings);
			}
			catch (TargetInvocationException tex)
			{
				if (tex.InnerException != null)
					throw tex.InnerException;
				throw;
			}
			catch (JsonSerializationException ex)
			{
				Logger.Trace(ex.ToString());
				Logger.Trace(data);
				throw;
			}
		}

		StringBuilder ISerialization<StringBuilder>.Serialize<T>(T value)
		{
			var serializer = new JsonSerializer();
			serializer.Converters.Add(new StringEnumConverter());
			var declaredType = typeof(T);
			var type = value != null ? value.GetType() : declaredType;
			serializer.TypeNameHandling = type != declaredType || !(declaredType.IsClass || declaredType.IsValueType) ? TypeNameHandling.Objects : TypeNameHandling.Auto;
			serializer.Binder = Binder;
			var sb = new StringBuilder();
			using (var sw = new StringWriter(sb))
			{
				serializer.Serialize(sw, value);
			}
			return sb;
		}

		public T Deserialize<T>(StringBuilder data, StreamingContext context)
		{
			var serializer = new JsonSerializer();
			serializer.TypeNameHandling = TypeNameHandling.Auto;
			serializer.Context = context;
			serializer.Binder = Binder;
			var sbr = new StringBuilderReader(data);
			try
			{
				return (T)serializer.Deserialize(sbr, typeof(T));
			}
			catch (TargetInvocationException tex)
			{
				if (tex.InnerException != null)
					throw tex.InnerException;
				throw;
			}
			catch (JsonSerializationException ex)
			{
				Logger.Trace(ex.ToString());
				Logger.Trace(data.ToString());
				throw;
			}
		}

		StreamReader ISerialization<StreamReader>.Serialize<T>(T value)
		{
			var serializer = new JsonSerializer();
			serializer.Converters.Add(new StringEnumConverter());
			var declaredType = typeof(T);
			var type = value != null ? value.GetType() : declaredType;
			serializer.TypeNameHandling = type != declaredType || !(declaredType.IsClass || declaredType.IsValueType) ? TypeNameHandling.Objects : TypeNameHandling.Auto;
			serializer.Binder = Binder;
			var cms = ChunkedMemoryStream.Create();
			var sw = cms.GetWriter();
			serializer.Serialize(sw, value);
			sw.Flush();
			cms.Position = 0;
			//TODO: GetReader !?
			return new StreamReader(cms, Encoding.UTF8);
		}

		public T Deserialize<T>(StreamReader data, StreamingContext context)
		{
			var serializer = new JsonSerializer();
			serializer.TypeNameHandling = TypeNameHandling.Auto;
			serializer.Context = context;
			serializer.Binder = Binder;
			try
			{
				return (T)serializer.Deserialize(data, typeof(T));
			}
			catch (TargetInvocationException tex)
			{
				if (tex.InnerException != null)
					throw tex.InnerException;
				throw;
			}
			catch (JsonSerializationException ex)
			{
				if (!data.BaseStream.CanSeek)
					throw;
				data.BaseStream.Position = 0;
				data.DiscardBufferedData();
				Logger.Trace(ex.ToString());
				Logger.Trace(data.ReadToEnd());
				throw;
			}
		}

		public void Serialize(object value, Stream s)
		{
			StreamWriter sw;
			var cms = s as ChunkedMemoryStream;
			if (cms != null)
				sw = cms.GetWriter();
			else
				sw = new StreamWriter(s);
			SharedSerializer.Serialize(sw, value);
			sw.Flush();
		}

		public void SerializeJsonObject(object value, Stream s)
		{
			StreamWriter sw;
			var cms = s as ChunkedMemoryStream;
			if (cms != null)
				sw = cms.GetWriter();
			else
				sw = new StreamWriter(s);
			var jo = value as IJsonObject;
			if (jo != null)
				jo.Serialize(sw, SharedSerializer.Serialize);
			else
			{
				var type = value.GetType();
				if (type.IsArray)
				{
					var array = (object[])value;
					if (array.Length == 0 || array[0] is IJsonObject)
					{
						sw.Write('[');
						for (int i = 0; i < array.Length - 1; i++)
						{
							(array[i] as IJsonObject).Serialize(sw, SharedSerializer.Serialize);
							sw.Write(',');
						}
						if (array.Length > 0)
							(array[array.Length - 1] as IJsonObject).Serialize(sw, SharedSerializer.Serialize);
						sw.Write(']');
					}
				}
				else SharedSerializer.Serialize(sw, value);
			}
			sw.Flush();
		}

		public object Deserialize(Stream s, Type target, StreamingContext context)
		{
			var serializer = new JsonSerializer();
			serializer.Context = context;
			serializer.Binder = Binder;
			serializer.TypeNameHandling = TypeNameHandling.Auto;
			StreamReader sr;
			var cms = s as ChunkedMemoryStream;
			if (cms != null)
				sr = cms.GetReader();
			else
				sr = new StreamReader(s, Encoding.UTF8);
			return serializer.Deserialize(sr, target);
		}
	}
}
