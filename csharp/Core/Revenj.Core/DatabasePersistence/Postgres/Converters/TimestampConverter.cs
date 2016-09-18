using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class TimestampConverter
	{
		private readonly static TimeZone CurrentZone = TimeZone.CurrentTimeZone;

		private readonly static int[] TimestampReminder = new int[]{
			1000000,
			100000,
			10000,
			1000,
			100,
			10
		};

		public static DateTime FromDatabase(string value)
		{
			return DateTime.Parse(value, CultureInfo.InvariantCulture);
		}

		public static string ToDatabase(DateTime value)
		{
			if (value.Kind == DateTimeKind.Utc)
				return value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFF+00");
			if (value.Kind == DateTimeKind.Unspecified)
				return value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFF");
			var offset = CurrentZone.GetUtcOffset(value);
			if (offset.Minutes != 0)
				value = value.AddMinutes(offset.Minutes);
			if (offset.Hours >= 0)
				return value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFF") + "+" + offset.Hours.ToString("00");
			return value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFF") + offset.Hours.ToString("00");
		}

		public static string ToDatabase(DateTimeOffset value)
		{
			if (value.Offset == TimeSpan.Zero)
				return value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFF+00");
			var offset = value.Offset;
			if (offset.Minutes != 0)
				value = value.AddMinutes(offset.Minutes);
			if (offset.Hours >= 0)
				return value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFF") + "+" + offset.Hours.ToString("00");
			return value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFF") + offset.Hours.ToString("00");
		}

		public static int Serialize(DateTime value, char[] buffer, int pos, int hours)
		{
			buffer[pos + 4] = '-';
			buffer[pos + 7] = '-';
			buffer[pos + 10] = ' ';
			buffer[pos + 13] = ':';
			buffer[pos + 16] = ':';
			NumberConverter.Write4(value.Year, buffer, pos);
			NumberConverter.Write2(value.Month, buffer, pos + 5);
			NumberConverter.Write2(value.Day, buffer, pos + 8);
			NumberConverter.Write2(value.Hour, buffer, pos + 11);
			NumberConverter.Write2(value.Minute, buffer, pos + 14);
			NumberConverter.Write2(value.Second, buffer, pos + 17);
			int micro = (int)(value.Ticks - new DateTime(value.Year, value.Month, value.Day, value.Hour, value.Minute, value.Second, value.Kind).Ticks) / 10;
			int end = pos + 19;
			if (micro != 0)
			{
				buffer[pos + 19] = '.';
				var div = micro / 100;
				var rem = micro - div * 100;
				NumberConverter.Write4(div, buffer, 20);
				NumberConverter.Write2(rem, buffer, 24);
				end = pos + 25;
				while (buffer[end] == '0')
					end--;
				end++;
			}
			if (hours >= 0)
				buffer[end] = '+';
			else
				buffer[end] = '-';
			if (value.Year == 1 && value.Month == 1 && value.Day == 1 && (value.Hour - hours) < 0)
				throw new ArgumentOutOfRangeException("Detected DateTime bellow minimum range: " + value);
			NumberConverter.Write2(hours, buffer, end + 1);
			return end + 3;
		}

		public static int Serialize(DateTimeOffset value, char[] buffer, int pos, int hours)
		{
			buffer[pos + 4] = '-';
			buffer[pos + 7] = '-';
			buffer[pos + 10] = ' ';
			buffer[pos + 13] = ':';
			buffer[pos + 16] = ':';
			NumberConverter.Write4(value.Year, buffer, pos);
			NumberConverter.Write2(value.Month, buffer, pos + 5);
			NumberConverter.Write2(value.Day, buffer, pos + 8);
			NumberConverter.Write2(value.Hour, buffer, pos + 11);
			NumberConverter.Write2(value.Minute, buffer, pos + 14);
			NumberConverter.Write2(value.Second, buffer, pos + 17);
			int micro = (int)(value.ToUniversalTime().Ticks - new DateTime(value.Year, value.Month, value.Day, value.Hour, value.Minute, value.Second, DateTimeKind.Utc).Ticks) / 10;
			int end = pos + 19;
			if (micro != 0)
			{
				buffer[pos + 19] = '.';
				var div = micro / 100;
				var rem = micro - div * 100;
				NumberConverter.Write4(div, buffer, 20);
				NumberConverter.Write2(rem, buffer, 24);
				end = pos + 25;
				while (buffer[end] == '0')
					end--;
				end++;
			}
			if (hours >= 0)
				buffer[end] = '+';
			else
				buffer[end] = '-';
			NumberConverter.Write2(hours, buffer, end + 1);
			return end + 3;
		}

		public static IPostgresTuple ToTuple(DateTime value)
		{
			return new TimestampTuple(value);
		}

		public static IPostgresTuple ToTupleUtc(DateTime value)
		{
			return new TimestampTuple(value.ToUniversalTime(), 0);
		}

		public static DateTime? ParseNullable(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var dt = ParseTimestampUtc(reader, context);
			reader.Read();
			return dt.ToLocalTime();
		}

		public static DateTime Parse(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return DateTime.MinValue;
			var dt = ParseTimestampUtc(reader, context);
			reader.Read();
			return dt.ToLocalTime();
		}

		public static DateTime? ParseNullableUtc(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var dt = ParseTimestampUtc(reader, context);
			reader.Read();
			return dt;
		}

		private static readonly DateTime MinUtc = DateTime.SpecifyKind(DateTime.MinValue, DateTimeKind.Utc);

		public static DateTime ParseUtc(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return MinUtc;
			var dt = ParseTimestampUtc(reader, context);
			reader.Read();
			return dt;
		}

		private static DateTime ParseTimestampUtc(BufferedTextReader reader, int context)
		{
			var cur = reader.Read(context);
			var buf = reader.SmallBuffer;
			buf[0] = (char)cur;
			var len = reader.ReadUntil(buf, 1, '\\', '"') + 1;
			reader.Read(context);
			if (buf[10] != ' ')
				return DateTime.Parse(new string(buf, 0, len), CultureInfo.InvariantCulture);
			var year = NumberConverter.Read4(buf, 0);
			var month = NumberConverter.Read2(buf, 5);
			var date = NumberConverter.Read2(buf, 8);
			var hour = NumberConverter.Read2(buf, 11);
			var minutes = NumberConverter.Read2(buf, 14);
			var seconds = NumberConverter.Read2(buf, 17);
			if (buf[19] == '.')
			{
				long nano = 0;
				var max = len - 3;
				for (int i = 20, r = 0; i < max && r < TimestampReminder.Length && i < buf.Length; i++, r++)
					nano += TimestampReminder[r] * (buf[i] - 48);
				var pos = buf[len - 3] == '+';
				var offset = NumberConverter.Read2(buf, len - 2);
				var dt = offset != 0
					? new DateTime(year, month, date, hour, minutes, seconds, DateTimeKind.Utc).AddHours(pos ? -offset : offset)
					: new DateTime(year, month, date, hour, minutes, seconds, DateTimeKind.Utc);
				return new DateTime(dt.Ticks + nano, DateTimeKind.Utc);
			}
			else if (len == 20 && buf[19] == 'Z')
			{
				return new DateTime(year, month, date, hour, minutes, seconds, DateTimeKind.Utc);
			}
			else if (len == 22)
			{
				var pos = buf[19] == '+';
				var offset = NumberConverter.Read2(buf, 20);
				if (offset != 0)
					return new DateTime(year, month, date, hour, minutes, seconds, DateTimeKind.Utc).AddHours(pos ? -offset : offset);
				return new DateTime(year, month, date, hour, minutes, seconds, DateTimeKind.Utc);
			}
			else if (len == 25)
			{
				var pos = buf[19] == '+';
				var offsetHour = NumberConverter.Read2(buf, 20);
				var offsetMin = NumberConverter.Read2(buf, 23);
				return new DateTime(year, month, date, hour, minutes, seconds, DateTimeKind.Utc)
					.AddHours(pos ? -offsetHour : offsetHour)
					.AddMinutes(pos ? -offsetMin : offsetMin);
			}
			else
			{
				buf[10] = 'T';
				return DateTime.Parse(new string(buf, 0, len));
			}
		}

		public static List<DateTime?> ParseNullableCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var escaped = cur != '{';
			if (escaped)
				reader.Read(context);
			int innerContext = context == 0 ? 1 : context << 1;
			cur = reader.Peek();
			if (cur == '}')
			{
				if (escaped)
					reader.Read(context + 2);
				else
					reader.Read(2);
				return new List<DateTime?>(0);
			}
			var list = new List<DateTime?>();
			do
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(null);
				}
				else
				{
					list.Add(ParseTimestampUtc(reader, innerContext).ToLocalTime());
					cur = reader.Read();
				}
			} while (cur == ',');
			if (escaped)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static List<DateTime> ParseCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var escaped = cur != '{';
			if (escaped)
				reader.Read(context);
			int innerContext = context == 0 ? 1 : context << 1;
			cur = reader.Peek();
			if (cur == '}')
			{
				if (escaped)
					reader.Read(context + 2);
				else
					reader.Read(2);
				return new List<DateTime>(0);
			}
			var list = new List<DateTime>();
			do
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(DateTime.MinValue);
				}
				else
				{
					list.Add(ParseTimestampUtc(reader, innerContext).ToLocalTime());
					cur = reader.Read();
				}
			} while (cur == ',');
			if (escaped)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static List<DateTime?> ParseNullableCollectionUtc(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var escaped = cur != '{';
			if (escaped)
				reader.Read(context);
			int innerContext = context == 0 ? 1 : context << 1;
			cur = reader.Peek();
			if (cur == '}')
			{
				if (escaped)
					reader.Read(context + 2);
				else
					reader.Read(2);
				return new List<DateTime?>(0);
			}
			var list = new List<DateTime?>();
			do
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(null);
				}
				else
				{
					list.Add(ParseTimestampUtc(reader, innerContext));
					cur = reader.Read();
				}
			} while (cur == ',');
			if (escaped)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static List<DateTime> ParseCollectionUtc(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var escaped = cur != '{';
			if (escaped)
				reader.Read(context);
			int innerContext = context == 0 ? 1 : context << 1;
			cur = reader.Peek();
			if (cur == '}')
			{
				if (escaped)
					reader.Read(context + 2);
				else
					reader.Read(2);
				return new List<DateTime>(0);
			}
			var list = new List<DateTime>();
			do
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(MinUtc);
				}
				else
				{
					list.Add(ParseTimestampUtc(reader, innerContext));
					cur = reader.Read();
				}
			} while (cur == ',');
			if (escaped)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		class TimestampTuple : IPostgresTuple
		{
			private readonly DateTime Value;
			private readonly int HoursOffset;

			public TimestampTuple(DateTime value)
			{
				if (value.Kind != DateTimeKind.Local)
					value = value.ToLocalTime();
				var offset = CurrentZone.GetUtcOffset(value);
				if (offset.Minutes != 0)
					this.Value = value.AddMinutes(offset.Minutes);
				else
					this.Value = value;
				HoursOffset = offset.Hours;
			}

			public TimestampTuple(DateTime value, int offset)
			{
				this.Value = value;
				HoursOffset = offset;
			}

			public bool MustEscapeRecord { get { return true; } }
			public bool MustEscapeArray { get { return true; } }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				var len = Serialize(Value, buf, 0, HoursOffset);
				sw.Write(buf, 0, len);
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				var len = Serialize(Value, buf, 0, HoursOffset);
				sw.Write(buf, 0, len);
			}

			public string BuildTuple(bool quote)
			{
				var buf = new char[32];
				if (quote)
				{
					buf[0] = '\'';
					var len = Serialize(Value, buf, 1, HoursOffset);
					buf[len] = '\'';
					return new string(buf, 0, len + 1);
				}
				else
				{
					var len = Serialize(Value, buf, 0, HoursOffset);
					return new string(buf, 0, len);
				}
			}
		}
	}
}