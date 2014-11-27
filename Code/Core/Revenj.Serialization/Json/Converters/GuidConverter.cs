using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using System.Runtime.Serialization;

namespace Revenj.Serialization.Json.Converters
{
	public static class GuidConverter
	{
		static readonly Pair[] Lookup;

		struct Pair
		{
			public readonly char First;
			public readonly char Second;
			public Pair(int value)
			{
				var hi = (value >> 4) & 0xf;
				var lo = value & 0xf;
				First = (char)(hi < 0xA ? '0' + hi : 'a' + hi - 10);
				Second = (char)(lo < 0xA ? '0' + lo : 'a' + lo - 10);
			}
		}

		static GuidConverter()
		{
			Lookup = new Pair[256];
			for (int i = 0; i < Lookup.Length; i++)
				Lookup[i] = new Pair(i);
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
		public static Guid Deserialize(TextReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			int i = 0;
			//TODO: optimize
			for (; i < buffer.Length && nextToken != '"'; i++, nextToken = sr.Read())
				buffer[i] = (char)nextToken;
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			return new Guid(new string(buffer, 0, i));
		}
		public static List<Guid> DeserializeCollection(TextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<Guid>();
			DeserializeCollection(sr, buffer, nextToken, res);
			return res;
		}
		public static void DeserializeCollection(TextReader sr, char[] buffer, int nextToken, ICollection<Guid> res)
		{
			res.Add(Deserialize(sr, buffer, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(Deserialize(sr, buffer, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<Guid?> DeserializeNullableCollection(TextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<Guid?>();
			DeserializeNullableCollection(sr, buffer, nextToken, res);
			return res;
		}
		public static void DeserializeNullableCollection(TextReader sr, char[] buffer, int nextToken, ICollection<Guid?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for guid value. Expecting '\"' or null");
			}
			else res.Add(Deserialize(sr, buffer, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for guid value. Expecting '\"' or null");
				}
				else res.Add(Deserialize(sr, buffer, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
	}
}
