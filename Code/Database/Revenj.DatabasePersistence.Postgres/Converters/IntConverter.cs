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
			return ParseInt(reader, ref cur, ')');
		}

		public static int Parse(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseInt(reader, ref cur, ')');
		}

		private static int ParseInt(BufferedTextReader reader, ref int cur, char matchEnd)
		{
			int res = 0;
			if (cur == '-')
			{
				cur = reader.Read();
				do
				{
					res = (res << 3) + (res << 1) - (cur - 48);
					cur = reader.Read();
				} while (cur != -1 && cur != ',' && cur != matchEnd);
			}
			else
			{
				do
				{
					res = (res << 3) + (res << 1) + (cur - 48);
					cur = reader.Read();
				} while (cur != -1 && cur != ',' && cur != matchEnd);
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
				reader.Read(context);
			cur = reader.Peek();
			if (cur == '}')
			{
				if (espaced)
					reader.Read(context + 2);
				else
					reader.Read(2);
				return new List<int?>(0);
			}
			var list = new List<int?>();
			do
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					list.Add(null);
					cur = reader.Read(4);
				}
				else
				{
					list.Add(ParseInt(reader, ref cur, '}'));
				}
			} while (cur == ',');
			if (espaced)
				reader.Read(context + 1);
			else
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
				reader.Read(context);
			cur = reader.Peek();
			if (cur == '}')
			{
				if (espaced)
					reader.Read(context + 2);
				else
					reader.Read(2);
				return new List<int>(0);
			}
			var list = new List<int>();
			do
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					list.Add(0);
					cur = reader.Read(4);
				}
				else
				{
					list.Add(ParseInt(reader, ref cur, '}'));
				}
			} while (cur == ',');
			if (espaced)
				reader.Read(context + 1);
			else
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

		public static string ToString(char[] buf, int value)
		{
			if (value == int.MinValue)
				return "-2147483648";
			if (value < 0)
			{
				int pos = 11;
				var abs = (uint)(-value);
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
				} while (pos > 2);
				pos += num.Offset;
				buf[pos] = '-';
				return new string(buf, pos, 12 - pos);
			}
			else
			{
				int pos = 10;
				var abs = (uint)(value);
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
				return new string(buf, pos + 1, 10 - pos);
			}
		}

		public static int Serialize(int value, char[] buf, int start)
		{
			if (value == int.MinValue)
			{
				"-2147483648".CopyTo(0, buf, start, 11);
				return start + 11;
			}
			int len;
			int pos;
			if (value < 0)
			{
				pos = start + 11;
				var abs = (uint)(-value);
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
				} while (pos > 2);
				pos += num.Offset;
				buf[pos] = '-';
				len = start + 12 - pos;
				pos = pos - start;
			}
			else
			{
				pos = start + 10;
				var abs = (uint)(value);
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
				len = start + 10 - pos;
				pos = pos + 1 - start;
			}
			for (int i = start; i < start + len; i++)
				buf[i] = buf[i + pos];
			return start + len;
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
				else abs = (uint)(Value);
				int pos = 10;
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
