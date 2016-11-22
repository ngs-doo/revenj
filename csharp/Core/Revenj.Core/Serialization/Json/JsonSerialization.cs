using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Revenj.Utility;

namespace Revenj.Serialization
{
	public class JsonSerialization : ISerialization<string>, ISerialization<TextReader>
	{
		private readonly SerializationBinder Binder;
		private readonly JsonSerializer SharedSerializer;
		private static readonly StringEnumConverter EnumConverter = new StringEnumConverter();
		private static readonly TextReaderConverter TextReaderConverter = new TextReaderConverter();
		private static readonly TreePathConverter TreePathConverter = new TreePathConverter();

		public JsonSerialization(SerializationBinder binder)
		{
			Contract.Requires(binder != null);

			this.Binder = binder;
			SharedSerializer = new JsonSerializer();
			SharedSerializer.Converters.Add(EnumConverter);
			SharedSerializer.Converters.Add(TextReaderConverter);
			SharedSerializer.Converters.Add(TreePathConverter);
			//TODO: register all converters
			SharedSerializer.TypeNameHandling = TypeNameHandling.Auto;
			SharedSerializer.TypeNameAssemblyFormat = FormatterAssemblyStyle.Simple;
			SharedSerializer.Binder = binder;
		}

		public string Serialize<T>(T value)
		{
			using (var cms = ChunkedMemoryStream.Create())
			{
				Serialize(value, cms, true);
				cms.Position = 0;
				return cms.GetReader().ReadToEnd();
			}
		}

		public T Deserialize<T>(string data, StreamingContext context)
		{
			return Deserialize<T>(new StringReader(data), context);
		}

		TextReader ISerialization<TextReader>.Serialize<T>(T value)
		{
			var cms = ChunkedMemoryStream.Create();
			Serialize(value, cms, true);
			cms.Position = 0;
			return cms.GetReader();
		}

		public T Deserialize<T>(TextReader data, StreamingContext context)
		{
			return (T)DeserializeReader(null, data, typeof(T), context);
		}

		private static readonly byte[] NULL = new byte[] { (byte)'n', (byte)'u', (byte)'l', (byte)'l' };

		public object Deserialize(Stream stream, Type type, StreamingContext context)
		{
			var cms = stream as ChunkedMemoryStream;
			TextReader reader;
			if (cms != null)
			{
				if (cms.Matches(NULL))
					return null;
				reader = cms.GetReader();
			}
			else
				reader = new StreamReader(stream, Encoding.UTF8);
			return DeserializeReader(cms, reader, type, context);
		}

		private object DeserializeReader(ChunkedMemoryStream cms, TextReader reader, Type type, StreamingContext context)
		{
			var deserializer = GetDeserializer(type);
			if (deserializer == null)
			{
				try
				{
					if (context.Context == null)
						return SharedSerializer.Deserialize(new JsonTextReader(reader), type);
					var jsonNet = new JsonSerializer();
					jsonNet.Converters.Add(EnumConverter);
					jsonNet.Converters.Add(TextReaderConverter);
					jsonNet.Converters.Add(TreePathConverter);
					jsonNet.TypeNameHandling = TypeNameHandling.Auto;
					jsonNet.Context = context;
					jsonNet.Binder = Binder;
					return jsonNet.Deserialize(new JsonTextReader(reader), type);
				}
				catch (TargetInvocationException tex)
				{
					if (tex.InnerException != null)
						throw tex.InnerException;
					throw;
				}
			}
			return deserializer.Deserialize(cms, reader, context);
		}

		class InvariantWriter : StreamWriter
		{
			private static readonly CultureInfo Invariant = CultureInfo.InvariantCulture;

			public InvariantWriter(Stream s) : base(s) { }

			public override IFormatProvider FormatProvider { get { return Invariant; } }
		}

		public void Serialize(object value, Stream s, bool minimal)
		{
			TextWriter sw;
			var cms = s as ChunkedMemoryStream;
			if (cms != null)
				sw = cms.GetWriter();
			else
				sw = new InvariantWriter(s);
			var jo = value as IJsonObject;
			if (jo != null)
			{
				jo.Serialize(sw, minimal, SharedSerializer.Serialize);
			}
			else
			{
				var array = value as IJsonObject[];
				if (array != null)
				{
					sw.Write('[');
					if (array.Length > 0)
					{
						if (array[0] != null)
							array[0].Serialize(sw, minimal, SharedSerializer.Serialize);
						else
							sw.Write("null");
						for (int i = 1; i < array.Length; i++)
						{
							sw.Write(',');
							if (array[i] != null)
								array[i].Serialize(sw, minimal, SharedSerializer.Serialize);
							else
								sw.Write("null");
						}
					}
					sw.Write(']');
				}
				else if (value is IList<IJsonObject>)
				{
					var list = value as IList<IJsonObject>;
					sw.Write('[');
					if (list.Count > 0)
					{
						if (list[0] != null)
							list[0].Serialize(sw, minimal, SharedSerializer.Serialize);
						else
							sw.Write("null");
						for (int i = 1; i < list.Count; i++)
						{
							sw.Write(',');
							if (list[i] != null)
								list[i].Serialize(sw, minimal, SharedSerializer.Serialize);
							else
								sw.Write("null");
						}
					}
					sw.Write(']');
				}
				else if (value is IEnumerable<IJsonObject>)
				{
					var col = value as IEnumerable<IJsonObject>;
					sw.Write('[');
					var total = col.Count() - 1;
					if (total >= 0)
					{
						var enumerator = col.GetEnumerator();
						IJsonObject item;
						for (var i = 0; enumerator.MoveNext() && i < total; i++)
						{
							item = enumerator.Current;
							if (item != null)
								item.Serialize(sw, minimal, SharedSerializer.Serialize);
							else
								sw.Write("null");
							sw.Write(',');
						}
						item = enumerator.Current;
						if (item != null)
							item.Serialize(sw, minimal, SharedSerializer.Serialize);
						else
							sw.Write("null");
					}
					sw.Write(']');
				}
				else
				{
					var jw = new JsonTextWriter(sw);
					SharedSerializer.Serialize(jw, value);
					jw.Flush();
				}
			}
			sw.Flush();
		}

		private static Dictionary<Type, IDeserializer> Cache = new Dictionary<Type, IDeserializer>(17);
		private IDeserializer GetDeserializer(Type target)
		{
			IDeserializer des = null;
			if (Cache.TryGetValue(target, out des))
				return des;
			Type type = null;
			if (typeof(IJsonObject).IsAssignableFrom(target))
				type = target;
			else if (typeof(IJsonObject[]).IsAssignableFrom(target))
				type = target.GetElementType();
			else if (target.IsGenericType && typeof(ICollection<IJsonObject>).IsAssignableFrom(target))
				type = target.GetGenericArguments()[0];
			if (type != null && typeof(IJsonObject).IsAssignableFrom(type))
			{
				var desType = typeof(Deserializer<>).MakeGenericType(type);
				des = (IDeserializer)Activator.CreateInstance(desType, new object[] { target, SharedSerializer, Binder });
			}
			var newCache = new Dictionary<Type, IDeserializer>(Cache);
			newCache[target] = des;
			Cache = newCache;
			return des;
		}

		interface IDeserializer
		{
			object Deserialize(ChunkedMemoryStream cms, TextReader reader, StreamingContext context);
		}
		class Deserializer<T> : IDeserializer
			where T : IJsonObject
		{
			private readonly IJsonObject Converter;
			private readonly JsonSerializer JsonNet;
			private readonly SerializationBinder Binder;
			private readonly Type Target;
			private readonly Type Type;
			private readonly bool IsSimple;
			private readonly bool IsArray;
			private readonly bool IsList;
			private readonly bool IsSet;
			private readonly bool IsStack;
			private readonly bool IsQueue;
			private readonly bool IsLinkedList;

			public Deserializer(Type target, JsonSerializer jsonNet, SerializationBinder binder)
			{
				this.JsonNet = jsonNet;
				this.Binder = binder;
				this.Target = target;
				this.Type = typeof(T);
				try
				{
					Converter = (IJsonObject)FormatterServices.GetUninitializedObject(Type);
					IsSimple = Type == Target;
					IsArray = Target.IsArray;
					IsList = typeof(IList<IJsonObject>).IsAssignableFrom(Target);
					IsSet = typeof(ISet<IJsonObject>).IsAssignableFrom(Target);
					IsStack = typeof(Stack<T>) == Target;
					IsQueue = typeof(Queue<T>) == Target;
					IsLinkedList = typeof(LinkedList<T>) == Target;
				}
				catch { }
			}

			public object Deserialize(ChunkedMemoryStream cms, TextReader reader, StreamingContext context)
			{
				if (Converter == null)
				{
					try
					{
						if (context.Context == null)
							return JsonNet.Deserialize(new JsonTextReader(reader), Target);
						var jsonNet = new JsonSerializer();
						jsonNet.Converters.Add(EnumConverter);
						jsonNet.Converters.Add(TextReaderConverter);
						jsonNet.Converters.Add(TreePathConverter);
						jsonNet.TypeNameHandling = TypeNameHandling.Auto;
						jsonNet.Context = context;
						jsonNet.Binder = Binder;
						return jsonNet.Deserialize(new JsonTextReader(reader), Target);
					}
					catch (TargetInvocationException tex)
					{
						if (tex.InnerException != null)
							throw tex.InnerException;
						throw;
					}
				}
				object result;
				var btr = reader as BufferedTextReader;
				if (btr != null)
					result = Converter.Deserialize(btr, context, JsonNet.Deserialize);
				else if (cms != null)
					result = Converter.Deserialize(cms.UseBufferedReader(reader), context, JsonNet.Deserialize);
				else
				{
					using (var cms2 = ChunkedMemoryStream.Create())
						result = Converter.Deserialize(cms2.UseBufferedReader(reader), context, JsonNet.Deserialize);
				}
				if (IsSimple)
					return result;
				if (IsArray)
					return (result as List<T>).ToArray();
				if (IsSet)
					return new HashSet<T>(result as List<T>);
				if (IsList)
					return result;
				if (IsStack)
				{
					var list = result as List<T>;
					list.Reverse();
					return new Stack<T>(list);
				}
				if (IsQueue)
					return new Queue<T>(result as List<T>);
				if (IsLinkedList)
					return new LinkedList<T>(result as List<T>);
				return result;
			}
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

		public static int GetNextToken(BufferedTextReader sr)
		{
			int c = sr.Read();
			while (IsWhiteSpace(c))
				c = sr.Read();
			return c;
		}

		public static int MoveToNextToken(BufferedTextReader sr, int nextToken)
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

		public static long PositionInStream(TextReader tr)
		{
			var sr = tr as StreamReader;
			var btr = tr as BufferedTextReader;
			try
			{
				if (btr != null) return btr.Position;
				var binding = BindingFlags.DeclaredOnly | BindingFlags.Public | BindingFlags.NonPublic
					| BindingFlags.Instance | BindingFlags.GetField;
				if (sr != null)
				{
					var charpos = (int)sr.GetType().InvokeMember("charPos", binding, null, sr, null);
					var charlen = (int)sr.GetType().InvokeMember("charLen", binding, null, sr, null);

					return sr.BaseStream.Position - charlen + charpos;
				}
			}
			catch { }
			return -1;
		}

		public static int FillName(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			char c = (char)nextToken;
			var buffer = sr.SmallBuffer;
			int i = 0;
			var hash = 0x811C9DC5;
			for (; i < buffer.Length && c != '"'; i++, c = (char)sr.Read())
			{
				buffer[i] = c;
				hash = (hash ^ c) * 0x1000193;
			}
			nextToken = c;
			if (i < buffer.Length) buffer[i] = '\0';
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = GetNextToken(sr);
			if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			return (int)hash;
		}

		public static int CalcHash(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			char c = (char)nextToken;
			var buffer = sr.SmallBuffer;
			int i = 0;
			var hash = 0x811C9DC5;
			for (; i < buffer.Length && c != '"'; i++, c = (char)sr.Read())
			{
				buffer[i] = c;
				hash = (hash ^ c) * 0x1000193;
			}
			nextToken = c;
			if (i < buffer.Length) buffer[i] = '\0';
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			return (int)hash;
		}

		public static List<T> DeserializeObjectCollection<T>(BufferedTextReader sr, int nextToken, Func<T> factory)
		{
			var res = new List<T>();
			DeserializeObjectCollection(sr, nextToken, factory, res);
			return res;
		}
		public static void DeserializeObjectCollection<T>(BufferedTextReader sr, int nextToken, Func<T> factory, ICollection<T> res)
		{
			if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			res.Add(factory());
			while ((nextToken = GetNextToken(sr)) == ',')
			{
				if (GetNextToken(sr) != '{') throw new SerializationException("Expecting '{' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
				res.Add(factory());
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		public static List<T> DeserializeCollection<T>(BufferedTextReader sr, int nextToken, Func<int, T> factory)
		{
			var res = new List<T>();
			DeserializeCollection(sr, nextToken, factory, res);
			return res;
		}
		public static void DeserializeCollection<T>(BufferedTextReader sr, int nextToken, Func<int, T> factory, ICollection<T> res)
		{
			res.Add(factory(nextToken));
			while ((nextToken = GetNextToken(sr)) == ',')
			{
				nextToken = GetNextToken(sr);
				res.Add(factory(nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		public static List<T> DeserializeNullableObjectCollection<T>(BufferedTextReader sr, int nextToken, Func<T> factory)
			where T : class
		{
			var res = new List<T>();
			DeserializeNullableObjectCollection(sr, nextToken, factory, res);
			return res;
		}

		public static void DeserializeNullableObjectCollection<T>(BufferedTextReader sr, int nextToken, Func<T> factory, ICollection<T> res)
			where T : class
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + PositionInStream(sr) + " for string value. Expecting '\"' or null");
			}
			else if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			else res.Add(factory());
			while ((nextToken = GetNextToken(sr)) == ',')
			{
				nextToken = GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + PositionInStream(sr) + " for string value. Expecting '\"' or null");
				}
				else if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
				res.Add(factory());
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		public static List<T> DeserializeNullableCollection<T>(BufferedTextReader sr, int nextToken, Func<int, T> factory)
			where T : class
		{
			var res = new List<T>();
			DeserializeNullableCollection(sr, nextToken, factory, res);
			return res;
		}

		public static void DeserializeNullableCollection<T>(BufferedTextReader sr, int nextToken, Func<int, T> factory, ICollection<T> res)
			where T : class
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + PositionInStream(sr) + " for string value. Expecting '\"' or null");
			}
			else res.Add(factory(nextToken));
			while ((nextToken = GetNextToken(sr)) == ',')
			{
				nextToken = GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + PositionInStream(sr) + " for string value. Expecting '\"' or null");
				}
				else res.Add(factory(nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		public static List<T?> DeserializeNullableStructCollection<T>(BufferedTextReader sr, int nextToken, Func<int, T> factory)
			where T : struct
		{
			var res = new List<T?>();
			DeserializeNullableStructCollection(sr, nextToken, factory, res);
			return res;
		}

		public static void DeserializeNullableStructCollection<T>(BufferedTextReader sr, int nextToken, Func<int, T> factory, ICollection<T?> res)
			where T : struct
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + PositionInStream(sr) + " for string value. Expecting '\"' or null");
			}
			else res.Add(factory(nextToken));
			while ((nextToken = GetNextToken(sr)) == ',')
			{
				nextToken = GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + PositionInStream(sr) + " for string value. Expecting '\"' or null");
				}
				else res.Add(factory(nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		private static int SkipString(BufferedTextReader sr)
		{
			var c = sr.Read();
			var prev = c;
			while ((c != '"' || prev == '\\') && c != -1)
			{
				prev = c;
				c = sr.Read();
			}
			return GetNextToken(sr);
		}

		public static int Skip(BufferedTextReader sr, int nextToken)
		{
			if (nextToken == '"') return SkipString(sr);
			else if (nextToken == '{')
			{
				nextToken = GetNextToken(sr);
				if (nextToken == '}') return GetNextToken(sr);
				if (nextToken == '"') nextToken = SkipString(sr);
				else throw new SerializationException("Expecting '\"' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
				if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
				nextToken = GetNextToken(sr);
				nextToken = Skip(sr, nextToken);
				while (nextToken == ',')
				{
					nextToken = GetNextToken(sr);
					if (nextToken == '"') nextToken = SkipString(sr);
					else throw new SerializationException("Expecting '\"' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
					if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
					nextToken = GetNextToken(sr);
					nextToken = Skip(sr, nextToken);
				}
				if (nextToken != '}') throw new SerializationException("Expecting '}' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
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
				if (nextToken != ']') throw new SerializationException("Expecting ']' at position " + PositionInStream(sr) + ". Found " + (char)nextToken);
				return GetNextToken(sr);
			}
			else
			{
				while (nextToken != ',' && nextToken != '}' && nextToken != ']' && nextToken != -1)
					nextToken = sr.Read();
				return MoveToNextToken(sr, nextToken);
			}
		}

		public static ChunkedMemoryStream Memorize(BufferedTextReader sr, ref int nextToken)
		{
			var cms = ChunkedMemoryStream.Create();
			var writer = cms.GetWriter();
			writer.Write((char)nextToken);
			nextToken = Memorize(sr, nextToken, writer);
			writer.Flush();
			cms.Position = 0;
			return cms;
		}

		private static int MemoizeSkipString(BufferedTextReader sr, TextWriter sw)
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

		private static int MemoizeGetNextToken(BufferedTextReader sr, TextWriter sw)
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

		private static int Memorize(BufferedTextReader sr, int nextToken, TextWriter sw)
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
				if (nextToken != '}') throw new SerializationException("Expecting '}' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
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
				if (nextToken != ']') throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
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
