using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Text;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class TimestampConverter
	{
		public static DateTime FromDatabase(string value)
		{
			return DateTime.Parse(value, CultureInfo.InvariantCulture);
		}

		public static DateTime FromDatabase(StringBuilder value)
		{
			return DateTime.Parse(value.ToString(), CultureInfo.InvariantCulture);
		}

		private readonly static TimeZone CurrentZone = TimeZone.CurrentTimeZone;

		//TODO private
		public static string ToDatabase(DateTime value)
		{
			if (value.Kind == DateTimeKind.Utc)
				return value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFF+00");
			var offset = CurrentZone.GetUtcOffset(value);
			if (offset.Minutes != 0)
				value = value.AddMinutes(offset.Minutes);
			if (offset.Hours >= 0)
				return value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFF") + "+" + offset.Hours.ToString("00");
			return value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFF") + offset.Hours.ToString("00");
		}

		public static IPostgresTuple ToTuple(DateTime value)
		{
			return new ValueTuple(ToDatabase(value), false, false);
		}

		public static IPostgresTuple ToTuple(DateTime? value)
		{
			return value != null ? new ValueTuple(ToDatabase(value.Value), false, false) : null;
		}

		public static DateTime? ParseNullable(TextReader reader, int context, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var dt = ParseTimestamp(reader, context, buf);
			reader.Read();
			return dt;
		}

		public static DateTime Parse(TextReader reader, int context, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return DateTime.MinValue;
			var dt = ParseTimestamp(reader, context, buf);
			reader.Read();
			return dt;
		}

		public static DateTime ParseTimestamp(TextReader reader, int context, char[] buf)
		{
			for (int i = 0; i < context - 1; i++)
				reader.Read();
			var x = reader.Read(buf, 0, 19);
			int cur;
			do
			{
				cur = reader.Read();
				buf[x++] = (char)cur;
			} while (cur != -1 && cur != '\\' && cur != '"' && cur != ',' && cur != ')' && cur != '}');
			for (int i = 0; i < context - 1; i++)
				reader.Read();
			//TODO optimize
			return DateTime.Parse(new string(buf, 0, x - 1), CultureInfo.InvariantCulture);
		}

		public static List<DateTime?> ParseNullableCollection(TextReader reader, int context, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			var innerContext = context << 1;
			var list = new List<DateTime?>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					reader.Read();
					reader.Read();
					reader.Read();
					list.Add(null);
				}
				else
				{
					list.Add(ParseTimestamp(reader, innerContext, buf));
				}
				cur = reader.Read();
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}

		public static List<DateTime> ParseCollection(TextReader reader, int context, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			var innerContext = context << 1;
			var list = new List<DateTime>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					reader.Read();
					reader.Read();
					reader.Read();
					list.Add(DateTime.MinValue);
				}
				else
				{
					list.Add(ParseTimestamp(reader, innerContext, buf));
				}
				cur = reader.Read();
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}
	}
}