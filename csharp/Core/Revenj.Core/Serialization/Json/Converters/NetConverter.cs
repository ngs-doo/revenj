using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Runtime.Serialization;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
{
	public static class NetConverter
	{
		public static void Serialize(IPAddress value, TextWriter sw)
		{
			sw.Write('"');
			sw.Write(value.ToString());
			sw.Write('"');
		}
		public static void SerializeNullable(IPAddress value, TextWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value, sw);
		}
		public static IPAddress DeserializeNullableIP(BufferedTextReader sr, int nextToken)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid null value found at " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return DeserializeIP(sr, nextToken);
		}
		public static IPAddress DeserializeIP(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			var buffer = sr.SmallBuffer;
			int i = 0;
			for (; nextToken != '"' && i < buffer.Length; i++, nextToken = sr.Read())
				buffer[i] = (char)nextToken;
			if (nextToken == '"')
			{
				try
				{
					return IPAddress.Parse(new string(buffer, 0, i));
				}
				catch (Exception ex)
				{
					throw new SerializationException("Error parsing IP address at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
				}
			}
			throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for ip value. Expecting \"");
		}
		public static List<IPAddress> DeserializeIPCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeIP(sr, next));
		}
		public static void DeserializeIPCollection(BufferedTextReader sr, int nextToken, ICollection<IPAddress> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeIP(sr, next), res);
		}
		public static List<IPAddress> DeserializeIPNullableCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeIP(sr, next));
		}
		public static void DeserializeIPNullableCollection(BufferedTextReader sr, int nextToken, ICollection<IPAddress> res)
		{
			JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeIP(sr, next), res);
		}

		public static void Serialize(Uri value, TextWriter sw)
		{
			sw.Write('"');
			sw.Write(value.ToString());
			sw.Write('"');
		}
		public static void SerializeNullable(Uri value, TextWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value, sw);
		}
		public static Uri DeserializeNullableUri(BufferedTextReader sr, int nextToken)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid null value found at " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return DeserializeUri(sr, nextToken);
		}
		public static Uri DeserializeUri(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			var buffer = sr.CharBuffer;
			int i = 0;
			for (; nextToken != '"' && i < buffer.Length; i++, nextToken = sr.Read())
				buffer[i] = (char)nextToken;
			if (nextToken != '"')
				throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for Uri value. Expecting \"");
			return new Uri(new string(buffer, 0, i));
		}
		public static List<Uri> DeserializeUriCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeUri(sr, next));
		}
		public static void DeserializeUriCollection(BufferedTextReader sr, int nextToken, ICollection<Uri> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeUri(sr, next), res);
		}
		public static List<Uri> DeserializeUriNullableCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeUri(sr, next));
		}
		public static void DeserializeUriNullableCollection(BufferedTextReader sr, int nextToken, ICollection<Uri> res)
		{
			JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeUri(sr, next), res);
		}
	}
}
