using System;
using System.Collections.Generic;
using System.IO;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class IntConverter
	{
		public static int? ParseNullable(TextReader reader, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseInt(reader, ref cur, buf);
		}

		public static int Parse(TextReader reader, char[] buf)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseInt(reader, ref cur, buf);
		}

		private static int ParseInt(TextReader reader, ref int cur, char[] buf)
		{
			var neg = cur == '-';
			if (neg)
				cur = reader.Read();
			int res = 0;
			do
			{
				res = res * 10 + (cur - 48);
				cur = reader.Read();
			} while (cur != -1 && cur != ',' && cur != ')' && cur != '}');
			return neg ? -res : res;
		}

		public static List<int?> ParseNullableCollection(TextReader reader, int context, char[] buf)
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
			var list = new List<int?>();
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
					list.Add(ParseInt(reader, ref cur, buf));
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

		public static List<int> ParseCollection(TextReader reader, int context, char[] buf)
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
			var list = new List<int>();
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
					list.Add(ParseInt(reader, ref cur, buf));
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

		public static int ParsePositive(char[] source, int start, int end)
		{
			int res = 0;
			for (int i = start; i < source.Length; i++)
			{
				if (i == end) break;
				res = res * 10 + (source[i] - 48);
			}
			return res;
		}

		public static IPostgresTuple ToTuple(int value)
		{
			return new IntTuple(value);
		}

		class IntTuple : IPostgresTuple
		{
			private readonly int Value;

			public IntTuple(int value)
			{
				this.Value = value;
			}

			public bool MustEscapeRecord { get { return false; } }
			public bool MustEscapeArray { get { return false; } }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				if (Value == int.MinValue)
				{
					sw.Write("-2147483648");
					return;
				}
				uint abs;
				if (Value < 0)
				{
					sw.Write('-');
					abs = (uint)(-Value);
				}
				else
					abs = (uint)(Value);
				int pos = 10;
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
				sw.Write(buf, pos + 1, 10 - pos);
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
