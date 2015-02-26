using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Runtime.Serialization;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
{
	public static class NumberConverter
	{
		private static readonly CultureInfo Invariant = CultureInfo.InvariantCulture;
		private static readonly decimal[] Decimals;

		struct Pair
		{
			public readonly char First;
			public readonly char Second;
			public Pair(int number)
			{
				First = (char)((number / 10) + '0');
				Second = (char)((number % 10) + '0');
			}
		}
		private static readonly Pair[] Numbers;

		static NumberConverter()
		{
			Numbers = new Pair[100];
			for (int i = 0; i < Numbers.Length; i++)
				Numbers[i] = new Pair(i);
			Decimals = new decimal[28];
			var pow = 1m;
			for (int i = 0; i < Decimals.Length; i++)
			{
				pow = pow / 10;
				Decimals[i] = pow;
			}
		}

		internal static void Write2(int number, char[] buffer, int start)
		{
			var pair = Numbers[number];
			buffer[start] = pair.First;
			buffer[start + 1] = pair.Second;
		}

		internal static void Write3(int number, char[] buffer, int start)
		{
			var hi = number / 100;
			buffer[start] = (char)(hi + '0');
			var pair = Numbers[number - hi * 100];
			buffer[start + 1] = pair.First;
			buffer[start + 2] = pair.Second;
		}

		internal static void Write4(int number, char[] buffer, int start)
		{
			var div = number / 100;
			var pair1 = Numbers[div];
			buffer[start] = pair1.First;
			buffer[start + 1] = pair1.Second;
			var rem = number - div * 100;
			var pair2 = Numbers[rem];
			buffer[start + 2] = pair2.First;
			buffer[start + 3] = pair2.Second;
		}

		public static void Serialize(decimal value, TextWriter sw)
		{
			sw.Write(value);
		}
		public static void Serialize(decimal? value, TextWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static void Serialize(int value, TextWriter sw, char[] buffer)
		{
			if (value == int.MinValue)
			{
				sw.Write("-2147483648");
				return;
			}
			uint abs;
			if (value < 0)
			{
				sw.Write('-');
				abs = (uint)(-value);
			}
			else
				abs = (uint)(value);
			int pos = 10;
			do
			{
				var div = abs / 100;
				var rem = abs - div * 100;
				var num = Numbers[rem];
				buffer[pos--] = num.Second;
				buffer[pos--] = num.First;
				abs = div;
			} while (abs != 0);
			if (buffer[pos + 1] == '0') // TODO: remove branch
				pos++;
			sw.Write(buffer, pos + 1, 10 - pos);
		}
		public static void Serialize(int? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value.Value, sw, buffer);
		}

		public static void Serialize(long value, TextWriter sw, char[] buffer)
		{
			if (value == long.MinValue)
			{
				sw.Write("-9223372036854775808");
				return;
			}
			ulong abs;
			if (value < 0)
			{
				sw.Write('-');
				abs = (ulong)(-value);
			}
			else
				abs = (ulong)(value);
			int pos = 20;
			do
			{
				var div = abs / 100;
				var rem = abs - div * 100;
				var num = Numbers[rem];
				buffer[pos--] = num.Second;
				buffer[pos--] = num.First;
				abs = div;
			} while (abs != 0);
			if (buffer[pos + 1] == '0')//TODO: remove branch
				pos++;
			sw.Write(buffer, pos + 1, 20 - pos);
		}
		public static void Serialize(long? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static void Serialize(double value, TextWriter sw, char[] buffer)
		{
			sw.Write(value);
		}
		public static void Serialize(double? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static void Serialize(float value, TextWriter sw, char[] buffer)
		{
			sw.Write(value);
		}
		public static void Serialize(float? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static decimal DeserializeDecimal(BufferedTextReader sr, ref int nextToken)
		{
			var negative = nextToken == '-';
			if (negative) nextToken = sr.Read();
			long value = 0;
			for (int i = 0; i < 18; i++)
			{
				if (nextToken == '.')
					break;
				if (nextToken < '0' || nextToken > '9')
					return negative ? -value : value;
				value = (value << 3) + (value << 1) + nextToken - 48;
				nextToken = sr.Read();
			}
			decimal res = value;
			while (nextToken >= '0' && nextToken <= '9')
			{
				res = res * 10 + (nextToken - 48);
				nextToken = sr.Read();
			}
			if (nextToken == '.')
			{
				nextToken = sr.Read();
				for (int i = 0; i < Decimals.Length; i++)
				{
					if (nextToken < '0' || nextToken > '9')
						return negative ? -res : res;
					res += Decimals[i] * (nextToken - 48);
					nextToken = sr.Read();
				}
				if (nextToken >= '0' && nextToken <= '9')
					throw new SerializationException("Decimal number contains too many decimal at position: " + JsonSerialization.PositionInStream(sr));
			}
			return negative ? -res : res;
		}

		public static List<decimal> DeserializeDecimalCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<decimal>();
			DeserializeDecimalCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeDecimalCollection(BufferedTextReader sr, int nextToken, ICollection<decimal> res)
		{
			res.Add(DeserializeDecimal(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeDecimal(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<decimal?> DeserializeDecimalNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<decimal?>();
			DeserializeDecimalNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeDecimalNullableCollection(BufferedTextReader sr, int nextToken, ICollection<decimal?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for decimal value. Expecting number or null");
				nextToken = sr.Read();
			}
			else res.Add(DeserializeDecimal(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for decimal value. Expecting number or null");
					nextToken = sr.Read();
				}
				else res.Add(DeserializeDecimal(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		public static int DeserializeInt(BufferedTextReader sr, ref int nextToken)
		{
			int value = 0;
			int sign = 1;
			var negative = nextToken == '-';
			if (negative)
			{
				nextToken = sr.Read();
				sign = -1;
			}
			while (nextToken >= '0' && nextToken <= '9')
			{
				value = value * 10 + (nextToken - '0');
				nextToken = sr.Read();
			}
			return sign * value;
		}
		public static List<int> DeserializeIntCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<int>();
			DeserializeIntCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeIntCollection(BufferedTextReader sr, int nextToken, ICollection<int> res)
		{
			res.Add(DeserializeInt(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeInt(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<int?> DeserializeIntNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<int?>();
			DeserializeIntNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeIntNullableCollection(BufferedTextReader sr, int nextToken, ICollection<int?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for int value. Expecting number or null");
				nextToken = sr.Read();
			}
			else res.Add(DeserializeInt(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for int value. Expecting number or null");
					nextToken = sr.Read();
				}
				else res.Add(DeserializeInt(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		public static long DeserializeLong(BufferedTextReader sr, ref int nextToken)
		{
			long value = 0;
			int sign = 1;
			var negative = nextToken == '-';
			if (negative)
			{
				nextToken = sr.Read();
				sign = -1;
			}
			while (nextToken >= '0' && nextToken <= '9')
			{
				value = value * 10 + (nextToken - '0');
				nextToken = sr.Read();
			}
			return sign * value;
		}
		public static List<long> DeserializeLongCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<long>();
			DeserializeLongCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeLongCollection(BufferedTextReader sr, int nextToken, ICollection<long> res)
		{
			res.Add(DeserializeLong(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeLong(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<long?> DeserializeLongNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<long?>();
			DeserializeLongNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeLongNullableCollection(BufferedTextReader sr, int nextToken, ICollection<long?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for long value. Expecting number or null");
				nextToken = sr.Read();
			}
			else res.Add(DeserializeLong(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for long value. Expecting number or null");
					nextToken = sr.Read();
				}
				else res.Add(DeserializeLong(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		public static double DeserializeDouble(BufferedTextReader sr, char[] buffer, ref int nextToken)
		{
			var ind = 0;
			do
			{
				buffer[ind++] = (char)nextToken;
				nextToken = sr.Read();
			} while (ind < buffer.Length && nextToken != ',' && nextToken != '}' && nextToken != ']');
			return double.Parse(new string(buffer, 0, ind), NumberStyles.Float, Invariant);
		}
		public static List<double> DeserializeDoubleCollection(BufferedTextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<double>();
			DeserializeDoubleCollection(sr, buffer, nextToken, res);
			return res;
		}
		public static void DeserializeDoubleCollection(BufferedTextReader sr, char[] buffer, int nextToken, ICollection<double> res)
		{
			res.Add(DeserializeDouble(sr, buffer, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeDouble(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<double?> DeserializeDoubleNullableCollection(BufferedTextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<double?>();
			DeserializeDoubleNullableCollection(sr, buffer, nextToken, res);
			return res;
		}
		public static void DeserializeDoubleNullableCollection(BufferedTextReader sr, char[] buffer, int nextToken, ICollection<double?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for double value. Expecting number or null");
				nextToken = sr.Read();
			}
			else res.Add(DeserializeDouble(sr, buffer, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for double value. Expecting number or null");
					nextToken = sr.Read();
				}
				else res.Add(DeserializeDouble(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		public static float DeserializeFloat(BufferedTextReader sr, ref int nextToken)
		{
			var ind = 0;
			var buffer = sr.SmallBuffer;
			do
			{
				buffer[ind++] = (char)nextToken;
				nextToken = sr.Read();
			} while (ind < buffer.Length && nextToken != ',' && nextToken != '}' && nextToken != ']');
			return float.Parse(new string(buffer, 0, ind), NumberStyles.Float, Invariant);
		}
		public static List<float> DeserializeFloatCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<float>();
			DeserializeFloatCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeFloatCollection(BufferedTextReader sr, int nextToken, ICollection<float> res)
		{
			res.Add(DeserializeFloat(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeFloat(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<float?> DeserializeFloatNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<float?>();
			DeserializeFloatNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeFloatNullableCollection(BufferedTextReader sr, int nextToken, ICollection<float?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for float value. Expecting number or null");
				nextToken = sr.Read();
			}
			else res.Add(DeserializeFloat(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for float value. Expecting number or null");
					nextToken = sr.Read();
				}
				else res.Add(DeserializeFloat(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
	}
}
