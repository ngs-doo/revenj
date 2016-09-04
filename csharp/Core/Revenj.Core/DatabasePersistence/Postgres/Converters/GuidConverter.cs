using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class GuidConverter
	{
		private static readonly Pair[] Lookup;
		private static readonly byte[] Values;

		struct Pair
		{
			public readonly char First;
			public readonly char Second;
			public Pair(int value)
			{
				var hi = (value >> 4) & 15;
				var lo = value & 15;
				First = (char)(hi < 10 ? '0' + hi : 'a' + hi - 10);
				Second = (char)(lo < 10 ? '0' + lo : 'a' + lo - 10);
			}
		}

		static GuidConverter()
		{
			Lookup = new Pair[256];
			Values = new byte['f' + 1 - '0'];
			for (int i = 0; i < Lookup.Length; i++)
				Lookup[i] = new Pair(i);
			for (char c = '0'; c <= '9'; c++)
				Values[c - '0'] = (byte)(c - '0');
			for (char c = 'a'; c <= 'f'; c++)
				Values[c - '0'] = (byte)(c - 'a' + 10);
			for (char c = 'A'; c <= 'F'; c++)
				Values[c - '0'] = (byte)(c - 'A' + 10);
		}

		[StructLayout(LayoutKind.Explicit)]
		struct GuidMapping
		{
			[FieldOffset(0)]
			private readonly Guid Reference;

			[FieldOffset(0)]
			public readonly int A;
			[FieldOffset(4)]
			public readonly short B;
			[FieldOffset(6)]
			public readonly short C;
			[FieldOffset(8)]
			public readonly byte D;
			[FieldOffset(9)]
			public readonly byte E;
			[FieldOffset(10)]
			public readonly byte F;
			[FieldOffset(11)]
			public readonly byte G;
			[FieldOffset(12)]
			public readonly byte H;
			[FieldOffset(13)]
			public readonly byte I;
			[FieldOffset(14)]
			public readonly byte J;
			[FieldOffset(15)]
			public readonly byte K;

			public GuidMapping(Guid value)
				: this()
			{
				Reference = value;
			}
		}

		public static void Serialize(Guid value, char[] buf, int start)
		{
			var map = new GuidMapping(value);
			var a = map.A;
			var l = Lookup[(a >> 24) & 255];
			buf[start] = l.First;
			buf[start + 1] = l.Second;
			l = Lookup[(a >> 16) & 255];
			buf[start + 2] = l.First;
			buf[start + 3] = l.Second;
			l = Lookup[(a >> 8) & 255];
			buf[start + 4] = l.First;
			buf[start + 5] = l.Second;
			l = Lookup[a & 255];
			buf[start + 6] = l.First;
			buf[start + 7] = l.Second;
			buf[start + 8] = '-';
			var b = map.B;
			l = Lookup[(b >> 8) & 255];
			buf[start + 9] = l.First;
			buf[start + 10] = l.Second;
			l = Lookup[b & 255];
			buf[start + 11] = l.First;
			buf[start + 12] = l.Second;
			buf[start + 13] = '-';
			var c = map.C;
			l = Lookup[(c >> 8) & 255];
			buf[start + 14] = l.First;
			buf[start + 15] = l.Second;
			l = Lookup[c & 255];
			buf[start + 16] = l.First;
			buf[start + 17] = l.Second;
			buf[start + 18] = '-';
			var d = map.D;
			l = Lookup[d];
			buf[start + 19] = l.First;
			buf[start + 20] = l.Second;
			var e = map.E;
			l = Lookup[e];
			buf[start + 21] = l.First;
			buf[start + 22] = l.Second;
			buf[start + 23] = '-';
			var f = map.F;
			l = Lookup[f];
			buf[start + 24] = l.First;
			buf[start + 25] = l.Second;
			var g = map.G;
			l = Lookup[g];
			buf[start + 26] = l.First;
			buf[start + 27] = l.Second;
			var h = map.H;
			l = Lookup[h];
			buf[start + 28] = l.First;
			buf[start + 29] = l.Second;
			var i = map.I;
			l = Lookup[i];
			buf[start + 30] = l.First;
			buf[start + 31] = l.Second;
			var j = map.J;
			l = Lookup[j];
			buf[start + 32] = l.First;
			buf[start + 33] = l.Second;
			var k = map.K;
			l = Lookup[k];
			buf[start + 34] = l.First;
			buf[start + 35] = l.Second;
		}

		public static Guid? ParseNullable(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseGuid(reader, cur);
		}

		public static Guid Parse(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return Guid.Empty;
			return ParseGuid(reader, cur);
		}

		private static Guid ParseGuid(BufferedTextReader reader, int cur)
		{
			var buf = reader.SmallBuffer;
			buf[0] = (char)cur;
			int read = reader.Read(buf, 1, 36);
			for (int i = read + 1; i < 37 && i < buf.Length; i++)
				buf[i] = (char)reader.Read();
			return ParseGuid36(buf);
		}
		private static Guid ParseGuid36(char[] buffer)
		{
			try
			{
				int a = 0;
				for (int x = 0; x < 8 && x < buffer.Length; x++)
					a = (a << 4) + Values[buffer[x] - '0'];
				int b = 0;
				for (int x = 9; x < 13 && x < buffer.Length; x++)
					b = (b << 4) + Values[buffer[x] - '0'];
				int c = 0;
				for (int x = 14; x < 18 && x < buffer.Length; x++)
					c = (c << 4) + Values[buffer[x] - '0'];
				int d = (Values[buffer[19] - '0'] << 4) + Values[buffer[20] - '0'];
				int e = (Values[buffer[21] - '0'] << 4) + Values[buffer[22] - '0'];
				int f = (Values[buffer[24] - '0'] << 4) + Values[buffer[25] - '0'];
				int g = (Values[buffer[26] - '0'] << 4) + Values[buffer[27] - '0'];
				int h = (Values[buffer[28] - '0'] << 4) + Values[buffer[29] - '0'];
				int i = (Values[buffer[30] - '0'] << 4) + Values[buffer[31] - '0'];
				int j = (Values[buffer[32] - '0'] << 4) + Values[buffer[33] - '0'];
				int k = (Values[buffer[34] - '0'] << 4) + Values[buffer[35] - '0'];
				return new Guid(a, (short)b, (short)c, (byte)d, (byte)e, (byte)f, (byte)g, (byte)h, (byte)i, (byte)j, (byte)k);
			}
			catch (IndexOutOfRangeException)
			{
				return new Guid(new string(buffer, 0, 32));
			}
		}

		private static Guid ParseCollectionGuid(BufferedTextReader reader, int cur)
		{
			var buf = reader.SmallBuffer;
			buf[0] = (char)cur;
			int read = reader.Read(buf, 1, 35);
			for (int i = read + 1; i < 36; i++)
				buf[i] = (char)reader.Read();
			return ParseGuid36(buf);
		}

		public static List<Guid?> ParseNullableCollection(BufferedTextReader reader, int context)
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
				return new List<Guid?>(0);
			}
			var list = new List<Guid?>();
			do
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(null);
				}
				else
				{
					list.Add(ParseCollectionGuid(reader, cur));
					cur = reader.Read();
				}
			} while (cur == ',');
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static List<Guid> ParseCollection(BufferedTextReader reader, int context)
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
				return new List<Guid>(0);
			}
			var list = new List<Guid>();
			do
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(Guid.Empty);
				}
				else
				{
					list.Add(ParseCollectionGuid(reader, cur));
					cur = reader.Read();
				}
			} while (cur == ',');
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static IPostgresTuple ToTuple(Guid value)
		{
			return new GuidTuple(value);
		}

		class GuidTuple : IPostgresTuple
		{
			private readonly Guid Value;

			public GuidTuple(Guid value)
			{
				this.Value = value;
			}

			public bool MustEscapeRecord { get { return false; } }
			public bool MustEscapeArray { get { return false; } }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				Serialize(Value, buf, 0);
				sw.Write(buf, 0, 36);
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				InsertRecord(sw, buf, escaping, mappings);
			}

			public string BuildTuple(bool quote)
			{
				if (quote)
				{
					var buf = new char[38];
					buf[0] = '\'';
					Serialize(Value, buf, 1);
					buf[37] = '\'';
					return new string(buf, 0, buf.Length);
				}
				else
				{
					var buf = new char[36];
					Serialize(Value, buf, 0);
					return new string(buf, 0, buf.Length);
				}
			}
		}
	}
}
