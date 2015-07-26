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
				reader.Read(context);
			var list = new List<long?>();
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
					list.Add(ParseLong(reader, ref cur));
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
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
				reader.Read(context);
			var list = new List<long>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(0);
				}
				else
				{
					list.Add(ParseLong(reader, ref cur));
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static int Serialize(long value, char[] buf, int start)
		{
			if (value == long.MinValue)
			{
				"-9223372036854775808".CopyTo(0, buf, start, 20);
				return start + 20;
			}
			int len;
			int pos;
			if (value < 0)
			{
				pos = start + 21;
				var abs = (ulong)(-value);
				NumberConverter.Pair num;
				do
				{
					var div = abs / 100;
					var rem = abs - div * 100;
					num = NumberConverter.Numbers[rem];
					buf[pos--] = num.Second;
					buf[pos--] = num.First;
					abs = div;
					if (abs == 0) break;
				} while (pos > 1);
				pos += num.Offset;
				buf[pos] = '-';
				len = start + 22 - pos;
				pos = pos - start;
			}
			else
			{
				pos = 20;
				var abs = (ulong)(value);
				do
				{
					var div = abs / 100;
					var rem = abs - div * 100;
					var num = NumberConverter.Numbers[rem];
					buf[pos--] = num.Second;
					buf[pos--] = num.First;
					abs = div;
				} while (abs != 0);
				if (buf[pos + 1] == '0') // TODO: remove branch
					pos++;
				len = start + 20 - pos;
				pos = pos + 1 - start;
			}
			for (int i = start; i < start + len; i++)
				buf[i] = buf[i + pos];
			return start + len;
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
				NumberConverter.Pair num;
				do
				{
					var div = abs / 100;
					var rem = abs - div * 100;
					num = NumberConverter.Numbers[rem];
					buf[pos--] = num.Second;
					buf[pos--] = num.First;
					abs = div;
					if (abs == 0) break;
				} while (pos > 1);
				pos += num.Offset;
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
