using System;
using System.Collections.Generic;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class IntConverter
	{
		public static int? ParseNullable(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseInt(reader, ref cur);
		}

		public static int Parse(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseInt(reader, ref cur);
		}

		private static int ParseInt(BufferedTextReader reader, ref int cur)
		{
			int res = 0;
			if (cur == '-')
			{
				cur = reader.Read();
				do
				{
					res = (res << 3) + (res << 1) - (cur - 48);
					cur = reader.Read();
				} while (cur != -1 && cur != ',' && cur != ')' && cur != '}');
			}
			else
			{
				do
				{
					res = (res << 3) + (res << 1) + (cur - 48);
					cur = reader.Read();
				} while (cur != -1 && cur != ',' && cur != ')' && cur != '}');
			}
			return res;
		}

		public static List<int?> ParseNullableCollection(BufferedTextReader reader, int context)
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
					list.Add(ParseInt(reader, ref cur));
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

		public static List<int> ParseCollection(BufferedTextReader reader, int context)
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
					list.Add(ParseInt(reader, ref cur));
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
