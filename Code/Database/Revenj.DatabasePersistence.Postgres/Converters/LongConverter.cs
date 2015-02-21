using System;
using System.Collections.Generic;
using System.IO;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class LongConverter
	{
		public static long? ParseNullable(TextReader reader, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseLong(reader, ref cur, buf);
		}

		public static long Parse(TextReader reader, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseLong(reader, ref cur, buf);
		}

		private static long ParseLong(TextReader reader, ref int cur, char[] buf)
		{
			var neg = cur == '-';
			if (neg)
				cur = reader.Read();
			long res = 0;
			do
			{
				res = res * 10 + (cur - 48);
				cur = reader.Read();
			} while (cur != -1 && cur != ',' && cur != ')' && cur != '}');
			return neg ? -res : res;
		}

		public static List<long?> ParseNullableCollection(TextReader reader, int context, char[] buf)
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
			var list = new List<long?>();
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
					cur = reader.Read();
				}
				else
				{
					list.Add(ParseLong(reader, ref cur, buf));
				}
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}

		public static List<long> ParseCollection(TextReader reader, int context, char[] buf)
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
			var list = new List<long>();
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
					list.Add(0);
					cur = reader.Read();
				}
				else
				{
					list.Add(ParseLong(reader, ref cur, buf));
				}
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}

		public static IPostgresTuple ToTuple(long value)
		{
			return new LongTuple(value);
		}

		class LongTuple : IPostgresTuple
		{
			private readonly long Value;

			public LongTuple(long value)
			{
				this.Value = value;
			}

			public bool MustEscapeRecord { get { return false; } }
			public bool MustEscapeArray { get { return false; } }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				if (Value == long.MinValue)
				{
					sw.Write("-9223372036854775808");
					return;
				}
				ulong abs;
				if (Value < 0)
				{
					sw.Write('-');
					abs = (ulong)(-Value);
				}
				else
					abs = (ulong)(Value);
				int pos = 20;
				do
				{
					var div = abs / 100;
					var rem = abs - div * 100;
					var num = NumberConverter.Numbers[rem];
					buf[pos--] = num.Second;
					buf[pos--] = num.First;
					abs = div;
				} while (abs != 0);
				if (buf[pos + 1] == '0')//TODO: remove branch
					pos++;
				sw.Write(buf, pos + 1, 20 - pos);
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				InsertRecord(sw, buf, escaping, mappings);
			}

			public string BuildTuple(bool quote)
			{
				return Value.ToString();
			}
		}
	}
}
