using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Runtime.Serialization;

namespace NGS.Serialization.Json.Converters
{
	public static class DateTimeConverter
	{
		private static readonly CultureInfo Invariant = CultureInfo.InvariantCulture;

		public static void SerializeDate(DateTime value, StreamWriter sw, char[] buffer)
		{
			buffer[0] = '"';
			var n = value.Year;
			var i = n % 10;
			buffer[4] = (char)('0' + i);
			n = n / 10;
			i = n % 10;
			buffer[3] = (char)('0' + i);
			n = n / 10;
			i = n % 10;
			buffer[2] = (char)('0' + i);
			n = n / 10;
			i = n % 10;
			buffer[1] = (char)('0' + i);
			buffer[5] = '-';
			n = value.Month;
			buffer[7] = (char)('0' + n % 10);
			buffer[6] = (char)('0' + n / 10);
			buffer[8] = '-';
			n = value.Day;
			buffer[10] = (char)('0' + n % 10);
			buffer[9] = (char)('0' + n / 10);
			buffer[11] = '"';
			sw.Write(buffer, 0, 12);
		}
		public static void SerializeDate(DateTime? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				SerializeDate(value.Value, sw, buffer);
		}
		public static void Serialize(DateTime value, StreamWriter sw, char[] buffer)
		{
			sw.Write('"');
			sw.Write(value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFFFK"));
			sw.Write('"');
		}
		public static void Serialize(DateTime? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else Serialize(value.Value, sw, buffer);
		}
		public static DateTime DeserializeDate(StreamReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			int year = 0;
			for (int i = 0; i < 6 && (nextToken != '-'); i++, nextToken = sr.Read())
				year = year * 10 + (nextToken - '0');
			nextToken = sr.Read();
			int month = nextToken - 48;
			nextToken = sr.Read();
			if (nextToken != '-')
			{
				month = month * 10 + (nextToken - '0');
				if ((nextToken = sr.Read()) != '-') throw new SerializationException("Expecting '-' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			nextToken = sr.Read();
			int day = nextToken - 48;
			nextToken = sr.Read();
			if (nextToken != '"' && nextToken != ' ' && nextToken != 'T')
			{
				day = day * 10 + (nextToken - '0');
				nextToken = sr.Read();
			}
			for (int i = 0; i < 24 && nextToken != '"'; i++)
				nextToken = sr.Read();
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			return new DateTime(year, month, day);
		}
		public static List<DateTime> DeserializeDateCollection(StreamReader sr, int nextToken)
		{
			var res = new List<DateTime>();
			res.Add(DeserializeDate(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeDate(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}
		public static List<DateTime?> DeserializeDateNullableCollection(StreamReader sr, int nextToken)
		{
			var res = new List<DateTime?>();
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for DateTime value. Expecting date or null");
			}
			else res.Add(DeserializeDate(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for DateTime value. Expecting date or null");
				}
				else res.Add(DeserializeDate(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}

		public static DateTime DeserializeTimestamp(StreamReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			int i = 0;
			nextToken = sr.Read();
			for (; i < buffer.Length && nextToken != '"'; i++, nextToken = sr.Read())
				buffer[i] = (char)nextToken;
			if (i > 0 && buffer[i - 1] == 'Z')
				return DateTime.Parse(new string(buffer, 0, i), Invariant, DateTimeStyles.AssumeUniversal | DateTimeStyles.AdjustToUniversal);
			return DateTime.Parse(new string(buffer, 0, i), Invariant);
		}
		public static List<DateTime> DeserializeTimestampCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			var res = new List<DateTime>();
			res.Add(DeserializeTimestamp(sr, buffer, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeTimestamp(sr, buffer, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}
		public static List<DateTime?> DeserializeTimestampNullableCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			var res = new List<DateTime?>();
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for DateTime value. Expecting timestamp or null");
			}
			else res.Add(DeserializeTimestamp(sr, buffer, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for DateTime value. Expecting timestamp or null");
				}
				else res.Add(DeserializeTimestamp(sr, buffer, nextToken));
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
