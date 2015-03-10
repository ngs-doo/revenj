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

		struct Pair
		{
			public readonly char First;
			public readonly char Second;
			public readonly byte Offset;
			public Pair(int number)
			{
				First = (char)((number / 10) + '0');
				Second = (char)((number % 10) + '0');
				Offset = number < 10 ? (byte)1 : (byte)0;
			}
		}
		private static readonly Pair[] Numbers;

		static NumberConverter()
		{
			Numbers = new Pair[100];
			for (int i = 0; i < Numbers.Length; i++)
				Numbers[i] = new Pair(i);
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
			Pair num;
			do
			{
				var div = abs / 100;
				var rem = abs - div * 100;
				num = Numbers[rem];
				buffer[pos--] = num.Second;
				buffer[pos--] = num.First;
				abs = div;
				if (abs == 0) break;
			} while (pos > 1);
			pos += num.Offset;
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
			Pair num;
			do
			{
				var div = abs / 100;
				var rem = abs - div * 100;
				num = Numbers[rem];
				buffer[pos--] = num.Second;
				buffer[pos--] = num.First;
				abs = div;
				if (abs == 0) break;
			} while (pos > 1);
			pos += num.Offset;
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
			var neg = nextToken == '-';
			if (neg)
				nextToken = sr.Read();
			var buf = sr.SmallBuffer;
			buf[0] = (char)nextToken;
			var size = sr.ReadNumber(buf, 1) + 1;
			nextToken = sr.Read();
			if (nextToken >= '0' && nextToken <= '9' || nextToken == '.')
				throw new SerializationException("Too long decimal number: " + new string(buf, 0, size) + ". At position" + JsonSerialization.PositionInStream(sr));
			if (size > 18)
			{
				if (neg)
					return -decimal.Parse(new string(buf, 0, size), NumberStyles.AllowDecimalPoint | NumberStyles.AllowExponent, Invariant);
				return decimal.Parse(new string(buf, 0, size), NumberStyles.AllowDecimalPoint | NumberStyles.AllowExponent, Invariant);
			}
			long value = 0;
			int scale = 0;
			char ch;
			int num;
			for (int i = 0; i < size && i < buf.Length; i++)
			{
				ch = buf[i];
				if (ch == '.')
				{
					if (scale != 0)
						throw new SerializationException("Multiple '.' found in decimal value: " + new string(buf, 0, size) + ". At position" + JsonSerialization.PositionInStream(sr));
					scale = size - i - 1;
				}
				else
				{
					num = ch - 48;
					if (num >= 0 && num <= 9)
						value = (value << 3) + (value << 1) + num;
					else
					{
						if (neg)
							return -decimal.Parse(new string(buf, 0, size), NumberStyles.AllowDecimalPoint | NumberStyles.AllowExponent, Invariant);
						return decimal.Parse(new string(buf, 0, size), NumberStyles.AllowDecimalPoint | NumberStyles.AllowExponent, Invariant);
					}
				}
			}
			return new decimal((int)value, (int)(value >> 32), 0, neg, (byte)scale);
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
			var buf = sr.SmallBuffer;
			int value = 0;
			int num;
			if (nextToken == '-')
			{
				nextToken = sr.Read();
				buf[0] = (char)nextToken;
				var size = sr.ReadNumber(buf, 1) + 1;
				nextToken = sr.Read();
				for (int i = 0; i < size && i < buf.Length; i++)
				{
					num = buf[i] - '0';
					if (num >= 0 && num <= 9)
						value = (value << 3) + (value << 1) - num;
					else
						return -int.Parse(new string(buf, 0, size), NumberStyles.AllowExponent | NumberStyles.AllowDecimalPoint, Invariant);
				}
			}
			else
			{
				buf[0] = (char)nextToken;
				var size = sr.ReadNumber(buf, 1) + 1;
				nextToken = sr.Read();
				for (int i = 0; i < size && i < buf.Length; i++)
				{
					num = buf[i] - '0';
					if (num >= 0 && num <= 9)
						value = (value << 3) + (value << 1) + num;
					else
						return int.Parse(new string(buf, 0, size), NumberStyles.AllowExponent | NumberStyles.AllowDecimalPoint, Invariant);
				}
			}
			return value;
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
			var buf = sr.SmallBuffer;
			int num;
			if (nextToken == '-')
			{
				nextToken = sr.Read();
				buf[0] = (char)nextToken;
				var size = sr.ReadNumber(buf, 1) + 1;
				nextToken = sr.Read();
				for (int i = 0; i < size && i < buf.Length; i++)
				{
					num = buf[i] - '0';
					if (num >= 0 && num <= 9)
						value = (value << 3) + (value << 1) - num;
					else
						return -long.Parse(new string(buf, 0, size), NumberStyles.AllowExponent | NumberStyles.AllowDecimalPoint, Invariant);
				}
			}
			else
			{
				buf[0] = (char)nextToken;
				var size = sr.ReadNumber(buf, 1) + 1;
				nextToken = sr.Read();
				for (int i = 0; i < size && i < buf.Length; i++)
				{
					num = buf[i] - '0';
					if (num >= 0 && num <= 9)
						value = (value << 3) + (value << 1) + num;
					else
						return long.Parse(new string(buf, 0, size), NumberStyles.AllowExponent | NumberStyles.AllowDecimalPoint, Invariant);
				}
			}
			return value;
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

		public static double DeserializeDouble(BufferedTextReader sr, ref int nextToken)
		{
			var buf = sr.SmallBuffer;
			buf[0] = (char)nextToken;
			var size = sr.ReadNumber(buf, 1) + 1;
			nextToken = sr.Read();
			return double.Parse(new string(buf, 0, size), NumberStyles.Float, Invariant);
		}
		public static List<double> DeserializeDoubleCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<double>();
			DeserializeDoubleCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeDoubleCollection(BufferedTextReader sr, int nextToken, ICollection<double> res)
		{
			res.Add(DeserializeDouble(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeDouble(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<double?> DeserializeDoubleNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<double?>();
			DeserializeDoubleNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeDoubleNullableCollection(BufferedTextReader sr, int nextToken, ICollection<double?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for double value. Expecting number or null");
				nextToken = sr.Read();
			}
			else res.Add(DeserializeDouble(sr, ref nextToken));
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
				else res.Add(DeserializeDouble(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		public static float DeserializeFloat(BufferedTextReader sr, ref int nextToken)
		{
			var buf = sr.SmallBuffer;
			buf[0] = (char)nextToken;
			var size = sr.ReadNumber(buf, 1) + 1;
			nextToken = sr.Read();
			return float.Parse(new string(buf, 0, size), NumberStyles.Float, Invariant);
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
