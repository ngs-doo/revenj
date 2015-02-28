using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
{
	public static class StringConverter
	{
		public static void SerializeNullable(string value, TextWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value, sw);
		}

		private static char[] EscapeChars = new[] {
			'\u0000',
			'\u0001',
			'\u0002',
			'\u0003',
			'\u0004',
			'\u0005',
			'\u0006',
			'\u0007',
			'\u0008',
			'\u0009',
			'\u000A',
			'\u000B',
			'\u000C',
			'\u000D',
			'\u000E',
			'\u000F',
			'\u0010',
			'\u0011',
			'\u0012',
			'\u0013',
			'\u0014',
			'\u0015',
			'\u0016',
			'\u0017',
			'\u0018',
			'\u0019',
			'\u001A',
			'\u001B',
			'\u001C',
			'\u001D',
			'\u001E',
			'\u001F',
			'"',
			'\\'
		};

		public static void Serialize(string value, TextWriter sw)
		{
			var escaped = value.IndexOfAny(EscapeChars);
			if (escaped == -1)
			{
				sw.Write('"');
				sw.Write(value);
				sw.Write('"');
				return;
			}
			else
			{
				sw.Write('"');
				if (escaped != 0)
				{
					if (value.Length < 85000 || escaped < 85000 && (value.Length - escaped) < 85000)
						sw.Write(value.Substring(0, escaped));
					else
						escaped = 0;
				}
			}
			char c;
			for (int i = escaped; i < value.Length; i++)
			{
				c = value[i];
				switch (c)
				{
					case '\u0000': sw.Write(@"\u0000"); break;
					case '\u0001': sw.Write(@"\u0001"); break;
					case '\u0002': sw.Write(@"\u0002"); break;
					case '\u0003': sw.Write(@"\u0003"); break;
					case '\u0004': sw.Write(@"\u0004"); break;
					case '\u0005': sw.Write(@"\u0005"); break;
					case '\u0006': sw.Write(@"\u0006"); break;
					case '\u0007': sw.Write(@"\u0007"); break;
					case '\u0008': sw.Write(@"\b"); break;
					case '\u0009': sw.Write(@"\t"); break;
					case '\u000A': sw.Write(@"\n"); break;
					case '\u000B': sw.Write(@"\u000B"); break;
					case '\u000C': sw.Write(@"\f"); break;
					case '\u000D': sw.Write(@"\r"); break;
					case '\u000E': sw.Write(@"\u000E"); break;
					case '\u000F': sw.Write(@"\u000F"); break;
					case '\u0010': sw.Write(@"\u0010"); break;
					case '\u0011': sw.Write(@"\u0011"); break;
					case '\u0012': sw.Write(@"\u0012"); break;
					case '\u0013': sw.Write(@"\u0013"); break;
					case '\u0014': sw.Write(@"\u0014"); break;
					case '\u0015': sw.Write(@"\u0015"); break;
					case '\u0016': sw.Write(@"\u0016"); break;
					case '\u0017': sw.Write(@"\u0017"); break;
					case '\u0018': sw.Write(@"\u0018"); break;
					case '\u0019': sw.Write(@"\u0019"); break;
					case '\u001A': sw.Write(@"\u001A"); break;
					case '\u001B': sw.Write(@"\u001B"); break;
					case '\u001C': sw.Write(@"\u001C"); break;
					case '\u001D': sw.Write(@"\u001D"); break;
					case '\u001E': sw.Write(@"\u001E"); break;
					case '\u001F': sw.Write(@"\u001F"); break;
					case '\\': sw.Write(@"\\"); break;
					case '"': sw.Write(@"\"""); break;
					default: sw.Write(c); break;
				}
			}
			sw.Write('"');
		}

		public static void SerializePart(char[] value, int max, TextWriter sw)
		{
			char c;
			int i = 0;
			for (; i < value.Length && i < max; i++)
			{
				c = value[i];
				if (c < 32 || c == '"' || c == '\\')
					break;
			}
			sw.Write(value, 0, i);
			for (; i < value.Length && i < max; i++)
			{
				c = value[i];
				switch (c)
				{
					case '\u0000': sw.Write(@"\u0000"); break;
					case '\u0001': sw.Write(@"\u0001"); break;
					case '\u0002': sw.Write(@"\u0002"); break;
					case '\u0003': sw.Write(@"\u0003"); break;
					case '\u0004': sw.Write(@"\u0004"); break;
					case '\u0005': sw.Write(@"\u0005"); break;
					case '\u0006': sw.Write(@"\u0006"); break;
					case '\u0007': sw.Write(@"\u0007"); break;
					case '\u0008': sw.Write(@"\b"); break;
					case '\u0009': sw.Write(@"\t"); break;
					case '\u000A': sw.Write(@"\n"); break;
					case '\u000B': sw.Write(@"\u000B"); break;
					case '\u000C': sw.Write(@"\f"); break;
					case '\u000D': sw.Write(@"\r"); break;
					case '\u000E': sw.Write(@"\u000E"); break;
					case '\u000F': sw.Write(@"\u000F"); break;
					case '\u0010': sw.Write(@"\u0010"); break;
					case '\u0011': sw.Write(@"\u0011"); break;
					case '\u0012': sw.Write(@"\u0012"); break;
					case '\u0013': sw.Write(@"\u0013"); break;
					case '\u0014': sw.Write(@"\u0014"); break;
					case '\u0015': sw.Write(@"\u0015"); break;
					case '\u0016': sw.Write(@"\u0016"); break;
					case '\u0017': sw.Write(@"\u0017"); break;
					case '\u0018': sw.Write(@"\u0018"); break;
					case '\u0019': sw.Write(@"\u0019"); break;
					case '\u001A': sw.Write(@"\u001A"); break;
					case '\u001B': sw.Write(@"\u001B"); break;
					case '\u001C': sw.Write(@"\u001C"); break;
					case '\u001D': sw.Write(@"\u001D"); break;
					case '\u001E': sw.Write(@"\u001E"); break;
					case '\u001F': sw.Write(@"\u001F"); break;
					case '\\': sw.Write(@"\\"); break;
					case '"': sw.Write(@"\"""); break;
					default: sw.Write(c); break;
				}
			}
		}

		public static string DeserializeNullable(BufferedTextReader sr, int nextToken)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid null value found at " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return Deserialize(sr, nextToken);
		}

		public static string Deserialize(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			sr.InitBuffer();
			nextToken = sr.FillUntil('"', '\\');
			if (nextToken == '"')
			{
				sr.Read();
				return sr.BufferToString();
			}
			var tmp = sr.SmallBuffer;
			do
			{
				nextToken = sr.Read(2);
				switch (nextToken)
				{
					case 'b': sr.AddToBuffer('\b'); break;
					case 't': sr.AddToBuffer('\t'); break;
					case 'r': sr.AddToBuffer('\r'); break;
					case 'n': sr.AddToBuffer('\n'); break;
					case 'f': sr.AddToBuffer('\f'); break;
					case 'u':
						var len = sr.Read(tmp, 0, 4);
						if (len < 4) sr.Read(tmp, len, 4 - len);//the second one must succeed
						sr.AddToBuffer((char)Convert.ToInt32(new string(tmp, 0, 4), 16));//TODO: optimize to no allocation
						break;
					default:
						sr.AddToBuffer((char)nextToken);
						break;
				}
				nextToken = sr.FillUntil('"', '\\');
			} while (nextToken == '\\');
			sr.Read();
			return sr.BufferToString();
		}
		public static List<string> DeserializeCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<string>();
			DeserializeCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeCollection(BufferedTextReader sr, int nextToken, ICollection<string> res)
		{
			res.Add(Deserialize(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(Deserialize(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<string> DeserializeNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<string>();
			DeserializeNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeNullableCollection(BufferedTextReader sr, int nextToken, ICollection<string> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for string value. Expecting '\"' or null");
			}
			else res.Add(Deserialize(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for string value. Expecting '\"' or null");
				}
				else res.Add(Deserialize(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
	}
}
