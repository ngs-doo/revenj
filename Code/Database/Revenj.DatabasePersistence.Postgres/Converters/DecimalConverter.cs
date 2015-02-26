using System;
using System.Collections.Generic;
using System.IO;
using Revenj.Common;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class DecimalConverter
	{
		private static readonly decimal[] Decimals;

		static DecimalConverter()
		{
			Decimals = new decimal[28];
			var pow = 1m;
			for (int i = 0; i < Decimals.Length; i++)
			{
				pow = pow / 10;
				Decimals[i] = pow;
			}
		}

		public static decimal? ParseNullable(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseDecimal(reader, ref cur, ')');
		}

		public static decimal Parse(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseDecimal(reader, ref cur, ')');
		}

		private static decimal ParseDecimal(BufferedTextReader reader, ref int cur, char matchEnd)
		{
			var neg = cur == '-';
			if (neg)
				cur = reader.Read();
			var buf = reader.SmallBuffer;
			buf[0] = (char)cur;
			var size = reader.ReadUntil(buf, 1, ',', matchEnd) + 1;
			cur = reader.Read();
			if (cur >= '0' && cur <= '9' || cur == '.')
				throw new FrameworkException("Too long decimal number: " + new string(buf, 0, size));
			if (size > 18)
			{
				if (neg)
					return -decimal.Parse(new string(buf, 0, size));
				return decimal.Parse(new string(buf, 0, size));
			}
			long value = 0;
			int scale = 0;
			char ch;
			for (int i = 0; i < size && i < buf.Length; i++)
			{
				ch = buf[i];
				if (ch == '.')
					scale = size - i - 1;
				else
					value = (value << 3) + (value << 1) + ch - 48;
			}
			return new decimal((int)value, (int)(value >> 32), 0, neg, (byte)scale);
		}

		public static List<decimal?> ParseNullableCollection(BufferedTextReader reader, int context)
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
				reader.Read(2);
				if (espaced)
					reader.Read(context);
				return new List<decimal?>(0);
			}
			var list = new List<decimal?>();
			do
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					list.Add(null);
					cur = reader.Read(4);
				}
				else list.Add(ParseDecimal(reader, ref cur, '}'));
			} while (cur == ',');
			if (espaced)
				reader.Read(context);
			reader.Read();
			return list;
		}

		public static List<decimal> ParseCollection(BufferedTextReader reader, int context)
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
				reader.Read(2);
				if (espaced)
					reader.Read(context);
				return new List<decimal>(0);
			}
			var list = new List<decimal>();
			do
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					list.Add(0);
					cur = reader.Read(4);
				}
				else list.Add(ParseDecimal(reader, ref cur, '}'));
			} while (cur == ',');
			if (espaced)
				reader.Read(context);
			reader.Read();
			return list;
		}

		public static IPostgresTuple ToTuple(decimal value)
		{
			return new DecimalTuple(value);
		}

		class DecimalTuple : IPostgresTuple
		{
			private readonly decimal Value;

			public DecimalTuple(decimal value)
			{
				this.Value = value;
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
				return Value.ToString();
			}
		}
	}
}
