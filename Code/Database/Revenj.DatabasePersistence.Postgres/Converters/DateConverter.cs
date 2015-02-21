using System;
using System.Collections.Generic;
using System.IO;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class DateConverter
	{
		public static DateTime? ParseNullable(TextReader reader, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var res = ParseDate(reader, cur, buf);
			reader.Read();
			return res;
		}

		public static DateTime Parse(TextReader reader, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return DateTime.MinValue;
			var res = ParseDate(reader, cur, buf);
			reader.Read();
			return res;
		}

		private static DateTime ParseDate(TextReader reader, int cur, char[] buf)
		{
			//TODO: BC after date for year < 0 ... not supported by .NET
			if (cur == '\\' || cur == '"')
				throw new NotSupportedException("Negative dates are not supported by .NET.");
			buf[0] = (char)cur;
			var read = reader.Read(buf, 1, 9);
			for (int i = read + 1; i < 10; i++)
				buf[i] = (char)reader.Read();
			if (buf[4] != '-')
				return ParseDateSlow(buf, reader);
			return new DateTime(IntConverter.ParsePositive(buf, 0, 4), NumberConverter.Read2(buf, 5), NumberConverter.Read2(buf, 8));
		}

		private static DateTime ParseDateSlow(char[] buf, TextReader reader)
		{
			int foundAt = 4;
			for (; foundAt < buf.Length; foundAt++)
				if (buf[foundAt] == '-')
					break;
			if (foundAt == buf.Length)
				throw new NotSupportedException("Invalid date value.");
			var year = IntConverter.ParsePositive(buf, 0, foundAt);
			var newBuf = new char[5];
			for (int i = foundAt + 1; i < buf.Length; i++)
				newBuf[i - foundAt - 1] = buf[i];
			for (int i = buf.Length - foundAt - 1; i < 5; i++)
				newBuf[i] = (char)reader.Read();
			return new DateTime(year, NumberConverter.Read2(newBuf, 0), NumberConverter.Read2(newBuf, 3));
		}

		public static List<DateTime?> ParseNullableCollection(TextReader reader, int context, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur == '"' || cur == '\\';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					cur = reader.Read();
			}
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
					list.Add(ParseDate(reader, cur, buf));
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
			var espaced = cur == '"' || cur == '\\';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					cur = reader.Read();
			}
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
					list.Add(ParseDate(reader, cur, buf));
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
				NumberConverter.Write4(Date.Year, buf, 0);
				buf[4] = '-';
				NumberConverter.Write2(Date.Month, buf, 5);
				buf[7] = '-';
				NumberConverter.Write2(Date.Day, buf, 8);
				sw.Write(buf, 0, 10);
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				InsertRecord(sw, buf, escaping, mappings);
			}

			public string BuildTuple(bool quote)
			{
				if (quote)
					return "'" + Date.Year + "-" + Date.Month.ToString("00") + "-" + Date.Day.ToString("00") + "'";
				return Date.Year + "-" + Date.Month.ToString("00") + "-" + Date.Day.ToString("00");
			}
		}
	}
}
