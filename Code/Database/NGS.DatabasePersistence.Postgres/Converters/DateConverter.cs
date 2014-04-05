using System;
using System.Collections.Generic;
using System.IO;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public static class DateConverter
	{
		public static DateTime? ParseNullable(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var res = ParseDate(reader, cur);
			reader.Read();
			return res;
		}

		public static DateTime Parse(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return DateTime.MinValue;
			var res = ParseDate(reader, cur);
			reader.Read();
			return res;
		}

		private static DateTime ParseDate(TextReader reader, int cur)
		{
			//TODO: BC after date for year < 0 ... not supported by .NET
			if (cur == '\\' || cur == '"')
				throw new NotSupportedException("Negative dates are not supported by .NET.");
			var buf = new char[10];
			buf[0] = (char)cur;
			reader.Read(buf, 1, 9);
			if (buf[4] != '-')
				return ParseDateSlow(buf, reader);
			return new DateTime(IntConverter.ParsePositive(buf, 0, 4), IntConverter.ParsePositive(buf, 5, 7), IntConverter.ParsePositive(buf, 8, 10));
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
			return new DateTime(year, IntConverter.ParsePositive(newBuf, 0, 2), IntConverter.ParsePositive(newBuf, 3, 5));
		}

		public static List<DateTime?> ParseNullableCollection(TextReader reader, int context)
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
					list.Add(ParseDate(reader, cur));
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

		public static List<DateTime> ParseCollection(TextReader reader, int context)
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
					list.Add(ParseDate(reader, cur));
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
