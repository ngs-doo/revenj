using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;
using System.Text;

namespace NGS.Serialization.Json.Converters
{
	public static class StringConverter
	{
		public static void SerializeNullable(string value, StreamWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value, sw);
		}

		public static void Serialize(string value, StreamWriter sw)
		{
			sw.Write('"');
			char c;
			for (int i = 0; i < value.Length; i++)
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

		public static string DeserializeNullable(StreamReader sr, char[] buffer, int nextToken)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid null value found at " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return Deserialize(sr, buffer, nextToken);
		}

		public static string Deserialize(StreamReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			int i = 0;
			nextToken = sr.Read();
			for (; nextToken != '"' && i < buffer.Length; i++, nextToken = sr.Read())
			{
				if (nextToken == '\\')
				{
					nextToken = sr.Read();
					switch (nextToken)
					{
						case (int)'\\': break;
						case (int)'"': break;
						case (int)'b': nextToken = '\b'; break;
						case (int)'t': nextToken = '\t'; break;
						case (int)'r': nextToken = '\r'; break;
						case (int)'n': nextToken = '\n'; break;
						case (int)'u':
							if (i < buffer.Length - 4)
							{
								buffer[i] = (char)sr.Read();
								buffer[i + 1] = (char)sr.Read();
								buffer[i + 2] = (char)sr.Read();
								buffer[i + 3] = (char)sr.Read();
								nextToken = Convert.ToInt32(new string(buffer, i, 4), 16);
							}
							else
							{
								var tmp = new char[4];
								tmp[0] = (char)sr.Read();
								tmp[1] = (char)sr.Read();
								tmp[2] = (char)sr.Read();
								tmp[3] = (char)sr.Read();
								nextToken = Convert.ToInt32(new string(tmp, 0, 4), 16);
							}
							break;
						default:
							throw new SerializationException("Invalid char found: " + (char)nextToken);
					}
				}
				buffer[i] = (char)nextToken;
			}
			if (i < buffer.Length) return new string(buffer, 0, i);
			var sb = new StringBuilder(128);
			sb.Append(buffer);
			while (nextToken != '"' && nextToken != -1)
			{
				if (nextToken == '\\')
				{
					nextToken = sr.Read();
					switch (nextToken)
					{
						case (int)'\\': break;
						case (int)'"': break;
						case (int)'b': nextToken = '\b'; break;
						case (int)'t': nextToken = '\t'; break;
						case (int)'r': nextToken = '\r'; break;
						case (int)'n': nextToken = '\n'; break;
						case (int)'u':
							buffer[0] = (char)sr.Read();
							buffer[1] = (char)sr.Read();
							buffer[2] = (char)sr.Read();
							buffer[3] = (char)sr.Read();
							nextToken = Convert.ToInt32(new string(buffer, 0, 4), 16);
							break;
						default:
							throw new SerializationException("Invalid char found: " + (char)nextToken);
					}
				}
				sb.Append((char)nextToken);
				nextToken = sr.Read();
			}
			//if (nextToken == -1) throw new SerializationException("Invalid end of string found.");
			return sb.ToString();
		}
		public static List<string> DeserializeCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			var res = new List<string>();
			res.Add(Deserialize(sr, buffer, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(Deserialize(sr, buffer, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}
		public static List<string> DeserializeNullableCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			var res = new List<string>();
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for string value. Expecting '\"' or null");
			}
			else res.Add(Deserialize(sr, buffer, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for string value. Expecting '\"' or null");
				}
				else res.Add(Deserialize(sr, buffer, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}
	}
}
