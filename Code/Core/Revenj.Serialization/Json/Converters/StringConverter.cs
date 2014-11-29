using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;
using System.Text;
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

		public static string DeserializeNullable(TextReader sr, char[] buffer, int nextToken)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid null value found at " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return Deserialize(sr, buffer, nextToken);
		}

		public static string Deserialize(TextReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			var btr = sr as BufferedTextReader;
			if (btr != null)
				return Deserialize(btr);
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
		private static int CleanUntil(char[] buffer, int len)
		{
			for (int i = 0; i < buffer.Length && i < len; i++)
				if (buffer[i] == '\\')
					return i;
			return len;
		}
		public static string Deserialize(BufferedTextReader sr)
		{
			var buffer = sr.TempBuffer;
			bool end;
			int i = sr.ReadUntil(buffer, 0, '"', out end);
			int safeUntil;
			if (i == 0 && end)
			{
				sr.Read();
				return string.Empty;
			}
			else if (i < buffer.Length && end)
			{
				safeUntil = CleanUntil(buffer, i);
				if (safeUntil == i)
				{
					sr.Read();
					return new string(buffer, 0, i);
				}
			}
			else safeUntil = CleanUntil(buffer, i);
			var largeBuffer = sr.LargeTempBuffer;
			int index = 0;
			var max = largeBuffer.Length - buffer.Length * 2;
			while (i != 0 && index < max)
			{
				Array.Copy(buffer, 0, largeBuffer, index, safeUntil);
				index += safeUntil;
				for (int x = safeUntil; x < buffer.Length && x < i; x++)
				{
					var nextToken = buffer[x];
					if (nextToken == '\\')
						nextToken = HandleEscape(sr, buffer, ref i, ref x);
					largeBuffer[index++] = nextToken;
				}
				i = sr.ReadUntil(buffer, 0, '"');
				safeUntil = CleanUntil(buffer, i);
			};

			if (i == 0)
			{
				if (sr.Read() != '"') throw new SerializationException("Expecting '\"' at then end of string at:" + JsonSerialization.PositionInStream(sr));
				return new string(largeBuffer, 0, index);
			}
			var sb = sr.GetBuilder();
			sb.Append(largeBuffer, 0, index);
			do
			{
				sb.Append(buffer, 0, safeUntil);
				for (int x = safeUntil; x < buffer.Length && x < i; x++)
				{
					var nextToken = buffer[x];
					if (nextToken == '\\')
					{
						nextToken = HandleEscape(sr, buffer, ref i, ref x);
					}
					sb.Append(nextToken);
				}
				i = sr.ReadUntil(buffer, 0, '"');
				safeUntil = CleanUntil(buffer, i);
			} while (i != 0);

			if (sr.Read() != '"') throw new SerializationException("Expecting '\"' at then end of string at:" + JsonSerialization.PositionInStream(sr));
			return sb.ToString();
		}

		private static char HandleEscape(BufferedTextReader sr, char[] buffer, ref int i, ref int x)
		{
			char nextToken;
			if (x == i - 1)
			{
				i = sr.ReadUntil(buffer, 0, '"');
				if (i == 0)
					throw new SerializationException("Invalid end of string at " + JsonSerialization.PositionInStream(sr));
				x = 1;
				nextToken = buffer[0];
			}
			else nextToken = buffer[++x];
			switch (nextToken)
			{
				case '\\': break;
				case '"': break;
				case 'b': nextToken = '\b'; break;
				case 't': nextToken = '\t'; break;
				case 'r': nextToken = '\r'; break;
				case 'n': nextToken = '\n'; break;
				case 'u':
					if (x + 4 < i)
					{
						nextToken = (char)Convert.ToInt32(new string(buffer, x + 1, 4), 16);
						x += 4;
					}
					else
					{
						var diff = i - x - 1;
						Array.Copy(buffer, x + 1, buffer, 0, diff);
						i = sr.ReadUntil(buffer, diff, '"');
						if (i + diff > 3)
						{
							nextToken = (char)Convert.ToInt32(new string(buffer, 0, 4), 16);
							i = i + diff;
						}
						else
						{
							var j = sr.ReadUntil(buffer, i + diff, '"');
							if (i + j + diff > 3)
								nextToken = (char)Convert.ToInt32(new string(buffer, 0, 4), 16);
							else
								throw new SerializationException("Unable to read json string");
							i = i + j + diff;
						}
						x = 3;
					}
					break;
				default:
					throw new SerializationException("Invalid char found: " + (char)nextToken);
			}
			return nextToken;
		}
		public static List<string> DeserializeCollection(TextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<string>();
			DeserializeCollection(sr, buffer, nextToken, res);
			return res;
		}
		public static void DeserializeCollection(TextReader sr, char[] buffer, int nextToken, ICollection<string> res)
		{
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
		}
		public static List<string> DeserializeNullableCollection(TextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<string>();
			DeserializeNullableCollection(sr, buffer, nextToken, res);
			return res;
		}
		public static void DeserializeNullableCollection(TextReader sr, char[] buffer, int nextToken, ICollection<string> res)
		{
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
		}
	}
}
