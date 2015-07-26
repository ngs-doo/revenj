using System;
using System.Collections.Generic;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class DateConverter
	{
		public static void Serialize(DateTime value, char[] buf, int start)
		{
			NumberConverter.Write4(value.Year, buf, start);
			buf[start + 4] = '-';
			NumberConverter.Write2(value.Month, buf, start + 5);
			buf[start + 7] = '-';
			NumberConverter.Write2(value.Day, buf, start + 8);
		}

		public static DateTime? ParseNullable(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var res = ParseDate(reader, cur);
			reader.Read();
			return res;
		}

		public static DateTime Parse(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return DateTime.MinValue;
			var res = ParseDate(reader, cur);
			reader.Read();
			return res;
		}

		private static DateTime ParseDate(BufferedTextReader reader, int cur)
		{
			//TODO: BC after date for year < 0 ... not supported by .NET
			if (cur == '\\' || cur == '"')
				throw new NotSupportedException("Negative dates are not supported by .NET.");
			var buf = reader.SmallBuffer;
			buf[0] = (char)cur;
			var read = reader.Read(buf, 1, 9);
			for (int i = read + 1; i < 10; i++)
				buf[i] = (char)reader.Read();
			if (buf[4] != '-')
				return ParseDateSlow(buf, reader);
			return new DateTime(NumberConverter.Read4(buf, 0), NumberConverter.Read2(buf, 5), NumberConverter.Read2(buf, 8));
		}

		private static DateTime ParseDateSlow(char[] buf, BufferedTextReader reader)
		{
			int foundAt = 4;
			for (; foundAt < buf.Length; foundAt++)
				if (buf[foundAt] == '-')
					break;
			if (foundAt == buf.Length)
				throw new NotSupportedException("Invalid date value.");
			var year = IntConverter.ParsePositive(buf, 0, foundAt);
			var newBuf = reader.CharBuffer;
			for (int i = foundAt + 1; i < buf.Length; i++)
				newBuf[i - foundAt - 1] = buf[i];
			for (int i = buf.Length - foundAt - 1; i < 5; i++)
				newBuf[i] = (char)reader.Read();
			return new DateTime(year, NumberConverter.Read2(newBuf, 0), NumberConverter.Read2(newBuf, 3));
		}

		public static List<DateTime?> ParseNullableCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var escaped = cur == '"' || cur == '\\';
			if (escaped)
				reader.Read(context);
			var list = new List<DateTime?>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(null);
				}
				else
				{
					list.Add(ParseDate(reader, cur));
					cur = reader.Read();
				}
			}
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
			var escaped = cur == '"' || cur == '\\';
			if (escaped)
				reader.Read(context);
			var list = new List<DateTime>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(DateTime.MinValue);
				}
				else
				{
					list.Add(ParseDate(reader, cur));
					cur = reader.Read();
				}
			}
			if (escaped)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static IPostgresTuple ToTuple(DateTime date)
		{
			return new DateTuple(date);
		}

		class DateTuple : IPostgresTuple
		{
			private readonly DateTime Date;

			public DateTuple(DateTime date)
			{
				this.Date = date;
			}

			public bool MustEscapeRecord { get { return false; } }
			public bool MustEscapeArray { get { return false; } }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				Serialize(Date, buf, 0);
				sw.Write(buf, 0, 10);
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				InsertRecord(sw, buf, escaping, mappings);
			}

			public string BuildTuple(bool quote)
			{
				if (quote)
				{
					var buf = new char[12];
					buf[0] = '\'';
					Serialize(Date, buf, 1);
					buf[11] = '\'';
					return new string(buf, 0, 12);
				}
				else
				{
					var buf = new char[10];
					Serialize(Date, buf, 0);
					return new string(buf, 0, 10);
				}
			}
		}
	}
}
