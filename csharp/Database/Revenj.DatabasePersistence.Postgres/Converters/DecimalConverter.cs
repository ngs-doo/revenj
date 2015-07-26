using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Runtime.InteropServices;
using Revenj.Common;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class DecimalConverter
	{
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
					return -decimal.Parse(new string(buf, 0, size), Invariant);
				return decimal.Parse(new string(buf, 0, size), Invariant);
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
				if (espaced)
					reader.Read(context + 2);
				else
					reader.Read(2);
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
				reader.Read(context + 1);
			else
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
				if (espaced)
					reader.Read(context + 2);
				else
					reader.Read(2);
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
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		private static readonly CultureInfo Invariant = CultureInfo.InvariantCulture;

		public static int Serialize(decimal value, char[] buf, int pos)
		{
			var str = value.ToString(Invariant);
			str.CopyTo(0, buf, pos, str.Length);
			return pos + str.Length;
		}

		public static int Serialize(decimal value, char[] buf, int pos, int scale)
		{
			var str = value.ToString("F" + scale, Invariant);
			str.CopyTo(0, buf, pos, str.Length);
			return pos + str.Length;
		}

		public static int Serialize2(decimal value, char[] buf, int pos)
		{
			var str = value.ToString("F2", Invariant);
			str.CopyTo(0, buf, pos, str.Length);
			return pos + str.Length;
		}

		public static IPostgresTuple ToTuple(decimal value)
		{
			return new DecimalTuple(value);
		}

		//TODO: Mono
		[StructLayout(LayoutKind.Explicit)]
		struct DecimalMapping
		{
			[FieldOffset(0)]
			private decimal Reference;

			[FieldOffset(0)]
			public readonly int flags;
			[FieldOffset(4)]
			public readonly int hi;
			[FieldOffset(8)]
			public readonly int lo;
			[FieldOffset(12)]
			public readonly int mid;

			public DecimalMapping(decimal reference)
				: this()
			{
				this.Reference = reference;
			}
		}

		public static IPostgresTuple ToTuple(decimal value, int scale)
		{
			//TODO: optimize
			return new ValueTuple(value.ToString("F" + scale, Invariant), false, false);
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
