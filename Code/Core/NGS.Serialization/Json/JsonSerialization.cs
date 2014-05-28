using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Globalization;
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

		public object DeserializeJson(Stream stream, Type type, StreamingContext context)
		{
			var serializer = new JsonSerializer();
			serializer.TypeNameHandling = TypeNameHandling.Auto;
			serializer.Context = context;
			serializer.Binder = Binder;
			return serializer.Deserialize(new StreamReader(stream, Encoding.UTF8), type);
		}

		class InvariantWriter : StreamWriter
		{
			private static readonly CultureInfo Invariant = CultureInfo.InvariantCulture;

			public InvariantWriter(Stream s) : base(s) { }

			public override IFormatProvider FormatProvider { get { return Invariant; } }
		}

		public void Serialize(object value, Stream s)
		{
			StreamWriter sw;
			var cms = s as ChunkedMemoryStream;
			if (cms != null)
				sw = cms.GetWriter();
			else
				sw = new InvariantWriter(s);
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
				sw = new InvariantWriter(s);
			var jo = value as IJsonObject;
			if (jo != null)
			{
				jo.Serialize(sw, SharedSerializer.Serialize);
			}
			else
			{
				var array = value as IJsonObject[];
				if (array != null)
				{
					sw.Write('[');
					if (array.Length > 0)
					{
						for (int i = 0; i < array.Length - 1; i++)
						{
							if (array[i] != null)
								array[i].Serialize(sw, SharedSerializer.Serialize);
							else
								sw.Write("null");
							sw.Write(',');
						}
						if (array[array.Length - 1] != null)
							array[array.Length - 1].Serialize(sw, SharedSerializer.Serialize);
						else
							sw.Write("null");
					}
					sw.Write(']');
				}
				else if (value is IList<IJsonObject>)
				{
					var list = value as IList<IJsonObject>;
					sw.Write('[');
					if (list.Count > 0)
					{
						for (int i = 0; i < list.Count - 1; i++)
						{
							if (list[i] != null)
								list[i].Serialize(sw, SharedSerializer.Serialize);
							else
								sw.Write("null");
							sw.Write(',');
						}
						if (list[list.Count - 1] != null)
							list[list.Count - 1].Serialize(sw, SharedSerializer.Serialize);
						else
							sw.Write("null");
					}
					sw.Write(']');
				}
				else if (value is ICollection<IJsonObject>)
				{
					var col = value as ICollection<IJsonObject>;
					sw.Write('[');
					var total = col.Count - 1;
					if (total > 0)
					{
						var enumerator = col.GetEnumerator();
						IJsonObject item;
						for (var i = 0; enumerator.MoveNext() && i < total; i++)
						{
							item = enumerator.Current;
							if (item != null)
								item.Serialize(sw, SharedSerializer.Serialize);
							else
								sw.Write("null");
							sw.Write(',');
						}
						item = enumerator.Current;
						if (item != null)
							item.Serialize(sw, SharedSerializer.Serialize);
						else
							sw.Write("null");
					}
					sw.Write(']');
				}
				else
				{
					SharedSerializer.Serialize(sw, value);
				}
			}
			sw.Flush();
		}

		private static ConcurrentDictionary<Type, IJsonObject> Cache = new ConcurrentDictionary<Type, IJsonObject>(1, 17);
		private IJsonObject GetSerializer(Type target)
		{
			IJsonObject jo = null;
			if (Cache.TryGetValue(target, out jo))
				return jo;
			if (typeof(IJsonObject).IsAssignableFrom(target))
				jo = (IJsonObject)Activator.CreateInstance(target);
			else if (typeof(IJsonObject[]).IsAssignableFrom(target))
				jo = (IJsonObject)Activator.CreateInstance(target.GetElementType());
			else if (typeof(IList<IJsonObject>).IsAssignableFrom(target))
				jo = (IJsonObject)Activator.CreateInstance(target.GetGenericArguments()[0]);
			else if (typeof(ICollection<IJsonObject>).IsAssignableFrom(target))
				jo = (IJsonObject)Activator.CreateInstance(target.GetGenericArguments()[0]);
			Cache.TryAdd(target, jo);
			return jo;
		}

		public object Deserialize(Stream s, Type target, StreamingContext context)
		{
			var serializer = new JsonSerializer();
			serializer.TypeNameHandling = TypeNameHandling.Auto;
			serializer.Context = context;
			serializer.Binder = Binder;
			StreamReader sr;
			var cms = s as ChunkedMemoryStream;
			if (cms != null)
				sr = cms.GetReader();
			else
				sr = new StreamReader(s, Encoding.UTF8);
			if (sr.Peek() == 'n')
			{
				if (sr.Read() == 'n' && sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid json provided");
			}
			var ser = GetSerializer(target);
			if (ser == null)
				return serializer.Deserialize(sr, target);
			return ser.Deserialize(sr, context, serializer.Deserialize);
		}

		private static bool IsWhiteSpace(int c)
		{
			switch (c)
			{
				case 9:
				case 10:
				case 11:
				case 12:
				case 13:
				case 32:
				case 160:
				case 5760:
				case 8192:
				case 8193:
				case 8194:
				case 8195:
				case 8196:
				case 8197:
				case 8198:
				case 8199:
				case 8200:
				case 8201:
				case 8202:
				case 8232:
				case 8233:
				case 8239:
				case 8287:
				case 12288:
					return true;
				default:
					return false;
			}
		}

		public static int GetNextToken(StreamReader sr)
		{
			int c = sr.Read();
			while (IsWhiteSpace(c))
				c = sr.Read();
			return c;
		}

		public static int MoveToNextToken(StreamReader sr, int nextToken)
		{
			int c = nextToken;
			while (IsWhiteSpace(c))
				c = sr.Read();
			return c;
		}

		public static bool SameName(char[] buffer, string name)
		{
			int i = 0;
			for (; i < name.Length && i < buffer.Length; i++)
				if (buffer[i] != name[i])
					return false;
			return i == buffer.Length || buffer[i] == '\0';
		}

		public static long PositionInStream(StreamReader sr)
		{
			try
			{
				var binding = BindingFlags.DeclaredOnly | BindingFlags.Public | BindingFlags.NonPublic
					| BindingFlags.Instance | BindingFlags.GetField;
				var charpos = (int)sr.GetType().InvokeMember("charPos", binding, null, sr, null);
				var charlen = (int)sr.GetType().InvokeMember("charLen", binding, null, sr, null);

				return sr.BaseStream.Position - charlen + charpos;
			}
			catch
			{
				return -1;
			}
		}

		public static int FillName(StreamReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			char c = (char)nextToken;
			int hash = 23;
			int i = 0;
			for (; i < buffer.Length && c != '"'; i++, c = (char)sr.Read())
			{
				buffer[i] = c;
				hash = hash * 31 + c;
			}
			nextToken = c;
			if (i < buffer.Length) buffer[i] = '\0';
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = GetNextToken(sr);
			if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			return hash;
		}

		public static List<T> DeserializeObjectCollection<T>(StreamReader sr, int nextToken, Func<T> factory)
		{
			var res = new List<T>();
			if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			res.Add(factory());
			while ((nextToken = GetNextToken(sr)) == ',')
			{
				if (GetNextToken(sr) != '{') throw new SerializationException("Expecting '{' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				res.Add(factory());
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}

		public static List<T> DeserializeCollection<T>(StreamReader sr, int nextToken, Func<int, T> factory)
		{
			var res = new List<T>();
			res.Add(factory(nextToken));
			while ((nextToken = GetNextToken(sr)) == ',')
			{
				nextToken = GetNextToken(sr);
				res.Add(factory(nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}

		public static List<T> DeserializeNullableObjectCollection<T>(StreamReader sr, int nextToken, Func<T> factory)
			where T : class
		{
			var res = new List<T>();
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for string value. Expecting '\"' or null");
			}
			else if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			else res.Add(factory());
			while ((nextToken = GetNextToken(sr)) == ',')
			{
				nextToken = GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for string value. Expecting '\"' or null");
				}
				else if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				res.Add(factory());
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}

		public static List<T> DeserializeNullableCollection<T>(StreamReader sr, int nextToken, Func<int, T> factory)
			where T : class
		{
			var res = new List<T>();
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for string value. Expecting '\"' or null");
			}
			else res.Add(factory(nextToken));
			while ((nextToken = GetNextToken(sr)) == ',')
			{
				nextToken = GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for string value. Expecting '\"' or null");
				}
				else res.Add(factory(nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}

		private static int SkipString(StreamReader sr)
		{
			var c = sr.Read();
			while (c != '"' && c != -1)
				c = sr.Read();
			return GetNextToken(sr);
		}

		public static int Skip(StreamReader sr, int nextToken)
		{
			if (nextToken == '"') return SkipString(sr);
			else if (nextToken == '{')
			{
				nextToken = GetNextToken(sr);
				if (nextToken == '}') return GetNextToken(sr);
				if (nextToken == '"') nextToken = SkipString(sr);
				else throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				nextToken = GetNextToken(sr);
				nextToken = Skip(sr, nextToken);
				while (nextToken == ',')
				{
					nextToken = GetNextToken(sr);
					if (nextToken == '"') nextToken = SkipString(sr);
					else throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
					if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
					nextToken = GetNextToken(sr);
					Skip(sr, nextToken);
				}
				if (nextToken != '}') throw new SerializationException("Expecting '}' at position " + global::NGS.Serialization.JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				return GetNextToken(sr);
			}
			else if (nextToken == '[')
			{
				nextToken = GetNextToken(sr);
				nextToken = Skip(sr, nextToken);
				while (nextToken == ',')
				{
					nextToken = GetNextToken(sr);
					nextToken = Skip(sr, nextToken);
				}
				if (nextToken != ']') throw new SerializationException("Expecting ']' at position " + global::NGS.Serialization.JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				return GetNextToken(sr);
			}
			else
			{
				while (nextToken != ',' && nextToken != '}' && nextToken != ']' && nextToken != -1)
					nextToken = sr.Read();
				return MoveToNextToken(sr, nextToken);
			}
		}

		public static ChunkedMemoryStream Memorize(StreamReader sr, ref int nextToken)
		{
			var cms = ChunkedMemoryStream.Create();
			var writer = cms.GetWriter();
			writer.Write((char)nextToken);
			nextToken = Memorize(sr, nextToken, writer);
			writer.Flush();
			cms.Position = 0;
			return cms;
		}

		private static int MemoizeSkipString(StreamReader sr, StreamWriter sw)
		{
			var c = sr.Read();
			sw.Write((char)c);
			while (c != '"' && c != -1)
			{
				c = sr.Read();
				sw.Write((char)c);
				if (c == '\\')
				{
					c = sr.Read();
					sw.Write((char)c);
					c = (char)0;
				}
			}
			return MemoizeGetNextToken(sr, sw);
		}

		private static int MemoizeGetNextToken(StreamReader sr, StreamWriter sw)
		{
			int c = sr.Read();
			sw.Write((char)c);
			while (IsWhiteSpace(c))
			{
				c = sr.Read();
				sw.Write((char)c);
			}
			return c;
		}

		private static int Memorize(StreamReader sr, int nextToken, StreamWriter sw)
		{
			if (nextToken == '"') return MemoizeSkipString(sr, sw);
			else if (nextToken == '{')
			{
				nextToken = MemoizeGetNextToken(sr, sw);
				if (nextToken == '}') return MemoizeGetNextToken(sr, sw);
				if (nextToken == '"') nextToken = MemoizeSkipString(sr, sw);
				else throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				nextToken = MemoizeGetNextToken(sr, sw);
				nextToken = Memorize(sr, nextToken, sw);
				while (nextToken == ',')
				{
					nextToken = MemoizeGetNextToken(sr, sw);
					if (nextToken == '"') nextToken = MemoizeSkipString(sr, sw);
					else throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
					if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
					nextToken = MemoizeGetNextToken(sr, sw);
					nextToken = Memorize(sr, nextToken, sw);
				}
				if (nextToken != '}') throw new SerializationException("Expecting '}' at position " + global::NGS.Serialization.JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				return MemoizeGetNextToken(sr, sw);
			}
			else if (nextToken == '[')
			{
				nextToken = MemoizeGetNextToken(sr, sw);
				nextToken = Memorize(sr, nextToken, sw);
				while (nextToken == ',')
				{
					nextToken = MemoizeGetNextToken(sr, sw);
					nextToken = Memorize(sr, nextToken, sw);
				}
				if (nextToken != ']') throw new SerializationException("Expecting ']' at position " + global::NGS.Serialization.JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				return MemoizeGetNextToken(sr, sw);
			}
			else
			{
				while (nextToken != ',' && nextToken != '}' && nextToken != ']' && nextToken != -1)
				{
					nextToken = sr.Read();
					sw.Write((char)nextToken);
				}
				return MoveToNextToken(sr, nextToken);
			}
		}
	}
}
