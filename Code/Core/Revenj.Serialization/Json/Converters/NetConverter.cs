using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Runtime.Serialization;
using System.Text;

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
		public static IPAddress DeserializeNullableIP(TextReader sr, char[] buffer, int nextToken)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid null value found at " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return DeserializeIP(sr, buffer, nextToken);
		}
		public static IPAddress DeserializeIP(TextReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			int i = 0;
			for (; nextToken != '"' && i < buffer.Length; i++, nextToken = sr.Read())
				buffer[i] = (char)nextToken;
			if (nextToken == '"')
				return IPAddress.Parse(new string(buffer, 0, i));
			throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for ip value. Expecting \"");
		}
		public static List<IPAddress> DeserializeIPCollection(TextReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeIP(sr, buffer, next));
		}
		public static void DeserializeIPCollection(TextReader sr, char[] buffer, int nextToken, ICollection<IPAddress> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeIP(sr, buffer, next), res);
		}
		public static List<IPAddress> DeserializeIPNullableCollection(TextReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeIP(sr, buffer, next));
		}
		public static void DeserializeIPNullableCollection(TextReader sr, char[] buffer, int nextToken, ICollection<IPAddress> res)
		{
			JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeIP(sr, buffer, next), res);
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
		public static Uri DeserializeNullableUri(TextReader sr, char[] buffer, int nextToken)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid null value found at " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return DeserializeUri(sr, buffer, nextToken);
		}
		public static Uri DeserializeUri(TextReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			int i = 0;
			for (; nextToken != '"' && i < buffer.Length; i++, nextToken = sr.Read())
				buffer[i] = (char)nextToken;
			if (nextToken != '"')
			{
				var sb = new StringBuilder(buffer.Length * 2);
				sb.Append(buffer);
				for (i = 0; nextToken != '"' && i < 2048; i++, nextToken = sr.Read())
					sb.Append((char)nextToken);
				if (nextToken == '"')
					return new Uri(sb.ToString());
			}
			else if (nextToken == '"')
				return new Uri(new string(buffer, 0, i));
			throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for Uri value. Expecting \"");
		}
		public static List<Uri> DeserializeUriCollection(TextReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeUri(sr, buffer, next));
		}
		public static void DeserializeUriCollection(TextReader sr, char[] buffer, int nextToken, ICollection<Uri> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeUri(sr, buffer, next), res);
		}
		public static List<Uri> DeserializeUriNullableCollection(TextReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeUri(sr, buffer, next));
		}
		public static void DeserializeUriNullableCollection(TextReader sr, char[] buffer, int nextToken, ICollection<Uri> res)
		{
			JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeUri(sr, buffer, next), res);
		}
	}
}
