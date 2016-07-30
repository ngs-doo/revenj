using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Runtime.Serialization;
using System.Text;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
{
	public static class DateTimeConverter
	{
		private static readonly CultureInfo Invariant = CultureInfo.InvariantCulture;

		private readonly static string TimeZoneWithDaylightSaving;
		private readonly static string TimeZoneWithoutDaylightSaving;
		private readonly static TimeZoneInfo LocalZoneInfo;
		private readonly static TimeZone CurrentZone;

		static DateTimeConverter()
		{
			LocalZoneInfo = TimeZoneInfo.Local;
			CurrentZone = TimeZone.CurrentTimeZone;
			var offset = LocalZoneInfo.BaseUtcOffset;
			var sbWithout = new StringBuilder();
			if (offset.TotalSeconds >= 0)
				sbWithout.Append('+');
			sbWithout.Append(offset.Hours.ToString("00"));
			sbWithout.Append(':');
			sbWithout.Append(offset.Minutes.ToString("00"));
			//tough luck if you have seconds in timezone offset
			TimeZoneWithoutDaylightSaving = sbWithout.ToString();
			var rules = LocalZoneInfo.GetAdjustmentRules();
			if (rules.Length == 1 && rules[0].DateStart == DateTime.MinValue && rules[0].DateEnd == DateTime.MinValue)
			{
				var sbWith = new StringBuilder();
				var totalOffset = offset.Add(rules[0].DaylightDelta);
				if (totalOffset.TotalSeconds >= 0)
					sbWith.Append('+');
				sbWith.Append(totalOffset.Hours.ToString("00"));
				sbWith.Append(':');
				sbWith.Append(totalOffset.Minutes.ToString("00"));
				TimeZoneWithDaylightSaving = sbWith.ToString();
			}
		}

		private static void SaveWithLocal(DateTime value, TextWriter sw, char[] buffer, int end)
		{
			if (TimeZoneWithDaylightSaving == null)
			{
				var offset = CurrentZone.GetUtcOffset(value);
				if (offset.Hours >= 0)
				{
					buffer[end] = '+';
					NumberConverter.Write2(offset.Hours, buffer, end + 1);
				}
				else
				{
					buffer[end] = '-';
					NumberConverter.Write2(-offset.Hours, buffer, end + 1);
				}
				buffer[end + 3] = ':';
				NumberConverter.Write2(offset.Minutes, buffer, end + 4);
			}
			else if (LocalZoneInfo.IsDaylightSavingTime(value))
			{
				buffer[end] = TimeZoneWithDaylightSaving[0];
				buffer[end + 1] = TimeZoneWithDaylightSaving[1];
				buffer[end + 2] = TimeZoneWithDaylightSaving[2];
				buffer[end + 3] = TimeZoneWithDaylightSaving[3];
				buffer[end + 4] = TimeZoneWithDaylightSaving[4];
				buffer[end + 5] = TimeZoneWithDaylightSaving[5];
			}
			else
			{
				buffer[end] = TimeZoneWithoutDaylightSaving[0];
				buffer[end + 1] = TimeZoneWithoutDaylightSaving[1];
				buffer[end + 2] = TimeZoneWithoutDaylightSaving[2];
				buffer[end + 3] = TimeZoneWithoutDaylightSaving[3];
				buffer[end + 4] = TimeZoneWithoutDaylightSaving[4];
				buffer[end + 5] = TimeZoneWithoutDaylightSaving[5];
			}
			buffer[end + 6] = '"';
			sw.Write(buffer, 0, end + 7);
		}
		private static void SaveWithOffset(TimeSpan offset, TextWriter sw, char[] buffer, int end)
		{
			if (offset.Ticks >= 0)
				buffer[end] = '+';
			else
				buffer[end] = '-';
			NumberConverter.Write2(offset.Hours, buffer, end + 1);
			buffer[end + 3] = ':';
			NumberConverter.Write2(offset.Minutes, buffer, end + 4);
			if (offset.Seconds != 0)
			{
				buffer[end + 6] = ':';
				NumberConverter.Write2(offset.Seconds, buffer, end + 4);
				buffer[end + 9] = '"';
				sw.Write(buffer, 0, end + 10);
			}
			else
			{
				buffer[end + 6] = '"';
				sw.Write(buffer, 0, end + 7);
			}
		}

		public static void SerializeDate(DateTime value, TextWriter sw, char[] buffer)
		{
			buffer[0] = '"';
			buffer[5] = '-';
			buffer[8] = '-';
			buffer[11] = '"';
			NumberConverter.Write4(value.Year, buffer, 1);
			NumberConverter.Write2(value.Month, buffer, 6);
			NumberConverter.Write2(value.Day, buffer, 9);
			sw.Write(buffer, 0, 12);
		}
		public static void SerializeDate(DateTime? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				SerializeDate(value.Value, sw, buffer);
		}
		public static void Serialize(DateTime value, TextWriter sw, char[] buffer)
		{
			buffer[0] = '"';
			buffer[5] = '-';
			buffer[8] = '-';
			buffer[11] = 'T';
			buffer[14] = ':';
			buffer[17] = ':';
			NumberConverter.Write4(value.Year, buffer, 1);
			NumberConverter.Write2(value.Month, buffer, 6);
			NumberConverter.Write2(value.Day, buffer, 9);
			NumberConverter.Write2(value.Hour, buffer, 12);
			NumberConverter.Write2(value.Minute, buffer, 15);
			NumberConverter.Write2(value.Second, buffer, 18);
			int nano = (int)(value.Ticks - new DateTime(value.Year, value.Month, value.Day, value.Hour, value.Minute, value.Second, value.Kind).Ticks);
			if (nano != 0)
			{
				buffer[20] = '.';
				var div = nano / 100;
				var div2 = div / 100;
				var rem = nano - div * 100;
				int end;
				if (rem != 0)
				{
					NumberConverter.Write3(div2, buffer, 21);
					NumberConverter.Write2(div - div2 * 100, buffer, 24);
					NumberConverter.Write2(rem, buffer, 26);
					end = 28;
				}
				else
				{
					var rem2 = div - div2 * 100;
					if (rem2 != 0)
					{
						NumberConverter.Write3(div2, buffer, 21);
						NumberConverter.Write2(div - div2 * 100, buffer, 24);
						end = 26;
					}
					else
					{
						var div3 = div2 / 100;
						if (div2 != div3 * 100)
						{
							NumberConverter.Write3(div2, buffer, 21);
							end = 24;
						}
						else
						{
							buffer[21] = (char)(div3 + '0');
							end = 22;
						}
					}
				}
				if (value.Kind == DateTimeKind.Local)
				{
					SaveWithLocal(value, sw, buffer, end);
				}
				else if (value.Kind == DateTimeKind.Utc)
				{
					buffer[end] = 'Z';
					buffer[end + 1] = '"';
					sw.Write(buffer, 0, end + 2);
				}
				else
				{
					buffer[end] = '"';
					sw.Write(buffer, 0, end + 1);
				}
			}
			else
			{
				if (value.Kind == DateTimeKind.Local)
					SaveWithLocal(value, sw, buffer, 20);
				else if (value.Kind == DateTimeKind.Utc)
				{
					buffer[20] = 'Z';
					buffer[21] = '"';
					sw.Write(buffer, 0, 22);
				}
				else
				{
					buffer[20] = '"';
					sw.Write(buffer, 0, 21);
				}
			}
		}
		public static void Serialize(DateTimeOffset value, TextWriter sw, char[] buffer)
		{
			buffer[0] = '"';
			buffer[5] = '-';
			buffer[8] = '-';
			buffer[11] = 'T';
			buffer[14] = ':';
			buffer[17] = ':';
			NumberConverter.Write4(value.Year, buffer, 1);
			NumberConverter.Write2(value.Month, buffer, 6);
			NumberConverter.Write2(value.Day, buffer, 9);
			NumberConverter.Write2(value.Hour, buffer, 12);
			NumberConverter.Write2(value.Minute, buffer, 15);
			NumberConverter.Write2(value.Second, buffer, 18);
			int nano = (int)(value.ToUniversalTime().Ticks - new DateTime(value.Year, value.Month, value.Day, value.Hour, value.Minute, value.Second, DateTimeKind.Utc).Ticks);
			if (nano != 0)
			{
				buffer[20] = '.';
				var div = nano / 100;
				var div2 = div / 100;
				var rem = nano - div * 100;
				int end;
				if (rem != 0)
				{
					NumberConverter.Write3(div2, buffer, 21);
					NumberConverter.Write2(div - div2 * 100, buffer, 24);
					NumberConverter.Write2(rem, buffer, 26);
					end = 28;
				}
				else
				{
					var rem2 = div - div2 * 100;
					if (rem2 != 0)
					{
						NumberConverter.Write3(div2, buffer, 21);
						NumberConverter.Write2(div - div2 * 100, buffer, 24);
						end = 26;
					}
					else
					{
						var div3 = div2 / 100;
						if (div2 != div3 * 100)
						{
							NumberConverter.Write3(div2, buffer, 21);
							end = 24;
						}
						else
						{
							buffer[21] = (char)(div3 + '0');
							end = 22;
						}
					}
				}
				if (value.Offset == TimeSpan.Zero)
				{
					buffer[end] = 'Z';
					buffer[end + 1] = '"';
					sw.Write(buffer, 0, end + 2);
				}
				else SaveWithOffset(value.Offset, sw, buffer, end);
			}
			else
			{
				if (value.Offset == TimeSpan.Zero)
				{
					buffer[20] = 'Z';
					buffer[21] = '"';
					sw.Write(buffer, 0, 22);
				}
				else SaveWithOffset(value.Offset, sw, buffer, 20);
			}
		}
		public static void Serialize(DateTime? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else Serialize(value.Value, sw, buffer);
		}
		public static void Serialize(DateTimeOffset? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else Serialize(value.Value, sw, buffer);
		}
		public static DateTime DeserializeDate(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			int year = 0;
			//TODO: 6!?
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
		public static List<DateTime> DeserializeDateCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<DateTime>();
			DeserializeDateCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeDateCollection(BufferedTextReader sr, int nextToken, ICollection<DateTime> res)
		{
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
		}
		public static List<DateTime?> DeserializeDateNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<DateTime?>();
			DeserializeDateNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeDateNullableCollection(BufferedTextReader sr, int nextToken, ICollection<DateTime?> res)
		{
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
		}

		private static bool AllDigits(char[] buf, int start, int end)
		{
			for (var i = start; i < end; i++)
			{
				var ch = buf[i];
				if (ch < '0' || ch > '9') return false;
			}
			return true;
		}

		public static DateTime DeserializeTimestamp(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			int i = 0;
			var buffer = sr.SmallBuffer;
			nextToken = sr.Read();
			for (; i < buffer.Length && nextToken != '"'; i++, nextToken = sr.Read())
				buffer[i] = (char)nextToken;
			try
			{
				if (i > 0 && buffer[i - 1] == 'Z')
				{
					if (i > 18 && i < 29 && buffer[4] == '-' && buffer[7] == '-'
						&& (buffer[10] == 'T' || buffer[10] == 't' || buffer[10] == ' ')
						&& buffer[13] == ':' && buffer[16] == ':' && AllDigits(buffer, 20, i - 1))
					{
						var year = 1000 * (buffer[0] - '0') + 100 * (buffer[1] - '0') + 10 * (buffer[2] - '0') + buffer[3] - '0';
						var month = 10 * (buffer[5] - '0') + buffer[6] - '0';
						var day = 10 * (buffer[8] - '0') + buffer[9] - '0';
						var hour = 10 * (buffer[11] - '0') + buffer[12] - '0';
						var min = 10 * (buffer[14] - '0') + buffer[15] - '0';
						var sec = 10 * (buffer[17] - '0') + buffer[18] - '0';
						if (buffer[19] == '.')
						{
							int nanos;
							switch (i)
							{
								case 22:
									nanos = 1000000 * (buffer[20] - 48);
									break;
								case 23:
									nanos = 1000000 * (buffer[20] - 48) + 100000 * (buffer[21] - 48);
									break;
								case 24:
									nanos = 1000000 * (buffer[20] - 48) + 100000 * (buffer[21] - 48) + 10000 * (buffer[22] - 48);
									break;
								case 25:
									nanos = 1000000 * (buffer[20] - 48) + 100000 * (buffer[21] - 48) + 10000 * (buffer[22] - 48) + 1000 * (buffer[23] - 48);
									break;
								case 26:
									nanos = 1000000 * (buffer[20] - 48) + 100000 * (buffer[21] - 48) + 10000 * (buffer[22] - 48) + 1000 * (buffer[23] - 48) + 100 * (buffer[24] - 48);
									break;
								case 27:
									nanos = 1000000 * (buffer[20] - 48) + 100000 * (buffer[21] - 48) + 10000 * (buffer[22] - 48) + 1000 * (buffer[23] - 48) + 100 * (buffer[24] - 48) + 10 * (buffer[25] - 48);
									break;
								default:
									nanos = 1000000 * (buffer[20] - 48) + 100000 * (buffer[21] - 48) + 10000 * (buffer[22] - 48) + 1000 * (buffer[23] - 48) + 100 * (buffer[24] - 48) + 10 * (buffer[25] - 48) + buffer[26] - 48;
									break;
							}
							return new DateTime(year, month, day, hour, min, sec, DateTimeKind.Utc).AddTicks(nanos);
						}
						return new DateTime(year, month, day, hour, min, sec, DateTimeKind.Utc);
					}
					return DateTime.Parse(new string(buffer, 0, i), Invariant, DateTimeStyles.AssumeUniversal | DateTimeStyles.AdjustToUniversal);
				}
				return DateTime.Parse(new string(buffer, 0, i), Invariant);
			}
			catch (Exception ex)
			{
				throw new SerializationException("Error parsing timestamp at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
			}
		}
		public static List<DateTime> DeserializeTimestampCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<DateTime>();
			DeserializeTimestampCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeTimestampCollection(BufferedTextReader sr, int nextToken, ICollection<DateTime> res)
		{
			res.Add(DeserializeTimestamp(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeTimestamp(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<DateTime?> DeserializeTimestampNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<DateTime?>();
			DeserializeTimestampNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeTimestampNullableCollection(BufferedTextReader sr, int nextToken, ICollection<DateTime?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for DateTime value. Expecting timestamp or null");
			}
			else res.Add(DeserializeTimestamp(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for DateTime value. Expecting timestamp or null");
				}
				else res.Add(DeserializeTimestamp(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static DateTimeOffset DeserializeOffset(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			int i = 0;
			var buffer = sr.SmallBuffer;
			nextToken = sr.Read();
			for (; i < buffer.Length && nextToken != '"'; i++, nextToken = sr.Read())
				buffer[i] = (char)nextToken;
			try
			{
				if (i > 0 && buffer[i - 1] == 'Z')
					return DateTimeOffset.Parse(new string(buffer, 0, i), Invariant, DateTimeStyles.AssumeUniversal | DateTimeStyles.AdjustToUniversal);
				return DateTimeOffset.Parse(new string(buffer, 0, i), Invariant);
			}
			catch (Exception ex)
			{
				throw new SerializationException("Error parsing timestamp at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
			}
		}
		public static List<DateTimeOffset> DeserializeOffsetCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<DateTimeOffset>();
			DeserializeOffsetCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeOffsetCollection(BufferedTextReader sr, int nextToken, ICollection<DateTimeOffset> res)
		{
			res.Add(DeserializeOffset(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeOffset(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<DateTimeOffset?> DeserializeOffsetNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<DateTimeOffset?>();
			DeserializeOffsetNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeOffsetNullableCollection(BufferedTextReader sr, int nextToken, ICollection<DateTimeOffset?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for DateTime value. Expecting timestamp or null");
			}
			else res.Add(DeserializeOffset(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for DateTime value. Expecting timestamp or null");
				}
				else res.Add(DeserializeOffset(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
	}
}
