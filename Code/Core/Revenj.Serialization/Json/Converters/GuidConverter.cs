using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using System.Runtime.Serialization;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
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
			private Guid Reference;

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

		public static void Serialize(Guid value, TextWriter sw, char[] buffer)
		{
			var map = new GuidMapping(value);
			buffer[0] = '"';
			var a = map.A;
			var l = Lookup[(a >> 24) & 255];
			buffer[1] = l.First;
			buffer[2] = l.Second;
			l = Lookup[(a >> 16) & 255];
			buffer[3] = l.First;
			buffer[4] = l.Second;
			l = Lookup[(a >> 8) & 255];
			buffer[5] = l.First;
			buffer[6] = l.Second;
			l = Lookup[a & 255];
			buffer[7] = l.First;
			buffer[8] = l.Second;
			buffer[9] = '-';
			var b = map.B;
			l = Lookup[(b >> 8) & 255];
			buffer[10] = l.First;
			buffer[11] = l.Second;
			l = Lookup[b & 255];
			buffer[12] = l.First;
			buffer[13] = l.Second;
			buffer[14] = '-';
			var c = map.C;
			l = Lookup[(c >> 8) & 255];
			buffer[15] = l.First;
			buffer[16] = l.Second;
			l = Lookup[c & 255];
			buffer[17] = l.First;
			buffer[18] = l.Second;
			buffer[19] = '-';
			var d = map.D;
			l = Lookup[d];
			buffer[20] = l.First;
			buffer[21] = l.Second;
			var e = map.E;
			l = Lookup[e];
			buffer[22] = l.First;
			buffer[23] = l.Second;
			buffer[24] = '-';
			var f = map.F;
			l = Lookup[f];
			buffer[25] = l.First;
			buffer[26] = l.Second;
			var g = map.G;
			l = Lookup[g];
			buffer[27] = l.First;
			buffer[28] = l.Second;
			var h = map.H;
			l = Lookup[h];
			buffer[29] = l.First;
			buffer[30] = l.Second;
			var i = map.I;
			l = Lookup[i];
			buffer[31] = l.First;
			buffer[32] = l.Second;
			var j = map.J;
			l = Lookup[j];
			buffer[33] = l.First;
			buffer[34] = l.Second;
			var k = map.K;
			l = Lookup[k];
			buffer[35] = l.First;
			buffer[36] = l.Second;
			buffer[37] = '"';
			sw.Write(buffer, 0, 38);
		}
		public static void Serialize(Guid? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else Serialize(value.Value, sw, buffer);
		}
		public static Guid Deserialize(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			var buffer = sr.SmallBuffer;
			var i = sr.Read(buffer, 0, 33);
			nextToken = buffer[i - 1];
			for (; nextToken != '"' && i < buffer.Length; i++)
			{
				nextToken = sr.Read();
				buffer[i] = (char)nextToken;
			}
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			if (i == 33)
				return ParseGuid32(buffer);
			else if (i == 37)
				return ParseGuid36(buffer);
			return new Guid(new string(buffer, 0, i - 1));
		}
		private static Guid ParseGuid32(char[] buffer)
		{
			try
			{
				int a = 0;
				for (int x = 0; x < 8 && x < buffer.Length; x++)
					a = (a << 4) + Values[buffer[x] - '0'];
				int b = 0;
				for (int x = 8; x < 12 && x < buffer.Length; x++)
					b = (b << 4) + Values[buffer[x] - '0'];
				int c = 0;
				for (int x = 12; x < 16 && x < buffer.Length; x++)
					c = (c << 4) + Values[buffer[x] - '0'];
				int d = (Values[buffer[16] - '0'] << 4) + Values[buffer[17] - '0'];
				int e = (Values[buffer[18] - '0'] << 4) + Values[buffer[19] - '0'];
				int f = (Values[buffer[20] - '0'] << 4) + Values[buffer[21] - '0'];
				int g = (Values[buffer[22] - '0'] << 4) + Values[buffer[23] - '0'];
				int h = (Values[buffer[24] - '0'] << 4) + Values[buffer[25] - '0'];
				int i = (Values[buffer[26] - '0'] << 4) + Values[buffer[27] - '0'];
				int j = (Values[buffer[28] - '0'] << 4) + Values[buffer[29] - '0'];
				int k = (Values[buffer[30] - '0'] << 4) + Values[buffer[31] - '0'];
				return new Guid(a, (short)b, (short)c, (byte)d, (byte)e, (byte)f, (byte)g, (byte)h, (byte)i, (byte)j, (byte)k);
			}
			catch (IndexOutOfRangeException)
			{
				return new Guid(new string(buffer, 0, 32));
			}
		}
		private static Guid ParseGuid36(char[] buffer)
		{
			if (buffer[8] != '-' || buffer[13] != '-' || buffer[18] != '-' || buffer[23] != '-')
				return new Guid(new string(buffer, 0, 36));
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
		public static List<Guid> DeserializeCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<Guid>();
			DeserializeCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeCollection(BufferedTextReader sr, int nextToken, ICollection<Guid> res)
		{
			res.Add(Deserialize(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(Deserialize(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<Guid?> DeserializeNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<Guid?>();
			DeserializeNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeNullableCollection(BufferedTextReader sr, int nextToken, ICollection<Guid?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for guid value. Expecting '\"' or null");
			}
			else res.Add(Deserialize(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for guid value. Expecting '\"' or null");
				}
				else res.Add(Deserialize(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
	}
}
