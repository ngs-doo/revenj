using System;
using System.Collections.Generic;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class LongConverter
	{
		public static long? ParseNullable(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseLong(reader, ref cur);
		}

		public static long Parse(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseLong(reader, ref cur);
		}

		private static long ParseLong(BufferedTextReader reader, ref int cur)
		{
			long res = 0;
			if (cur == '-')
			{
				cur = reader.Read();
				do
				{
					res = (res << 3) + (res << 1) - (cur - 48);
					cur = reader.Read();
				} while (cur != -1 && cur != ',' && cur != ')' && cur != '}');
				return res;
			}
			else
			{
				do
				{
					res = (res << 3) + (res << 1) + (cur - 48);
					cur = reader.Read();
				} while (cur != -1 && cur != ',' && cur != ')' && cur != '}');
				return res;
			}
		}

		public static List<long?> ParseNullableCollection(BufferedTextReader reader, int context)
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
					list.Add(ParseLong(reader, ref cur));
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

		public static List<long> ParseCollection(BufferedTextReader reader, int context)
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
					list.Add(ParseLong(reader, ref cur));
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
				while (pos > 1)
				{
					var div = abs / 100;
					var rem = abs - div * 100;
					var num = NumberConverter.Numbers[rem];
					buf[pos--] = num.Second;
					buf[pos--] = num.First;
					abs = div;
					if (abs == 0) break;
				}
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
