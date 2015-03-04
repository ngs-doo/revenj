using System;
using System.Collections.Generic;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class BoolConverter
	{
		public static bool? ParseNullable(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			reader.Read();
			return cur == 't';
		}

		public static bool Parse(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return false;
			reader.Read();
			return cur == 't';
		}

		public static List<bool?> ParseNullableCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			var list = new List<bool?>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 't')
					list.Add(true);
				else if (cur == 'f')
					list.Add(false);
				else
				{
					reader.Read(3);
					list.Add(null);
				}
				cur = reader.Read();
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static List<bool> ParseCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			var list = new List<bool>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 't')
					list.Add(true);
				else if (cur == 'f')
					list.Add(false);
				else
				{
					reader.Read(3);
					list.Add(false);
				}
				cur = reader.Read();
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static int SerializeURI(bool value, char[] buf, int pos)
		{
			if (value)
			{
				buf[pos] = 't';
				buf[pos + 1] = 'r';
				buf[pos + 2] = 'u';
				buf[pos + 3] = 'e';
				return pos + 4;
			}
			buf[pos] = 'f';
			buf[pos + 1] = 'a';
			buf[pos + 2] = 'l';
			buf[pos + 3] = 's';
			buf[pos + 4] = 'e';
			return pos + 5;
		}

		public static IPostgresTuple ToTuple(bool value)
		{
			return new BoolTuple(value);
		}

		class BoolTuple : IPostgresTuple
		{
			private readonly char Value;

			public BoolTuple(bool value)
			{
				Value = value ? 't' : 'f';
			}

			public bool MustEscapeRecord { get { return false; } }
			public bool MustEscapeArray { get { return false; } }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write(Value);
			}
			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write(Value);
			}
			public string BuildTuple(bool quote)
			{
				return quote ? "'" + Value + "'" : Value.ToString();
			}
		}
	}
}
