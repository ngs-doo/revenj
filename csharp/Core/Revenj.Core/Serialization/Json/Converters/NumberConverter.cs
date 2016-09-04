using System;
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
			if (double.IsNaN(value))
				sw.Write("\"NaN\"");
			else if (double.IsInfinity(value))
			{
				if (double.IsPositiveInfinity(value))
					sw.Write("\"Infinity\"");
				else
					sw.Write("\"-Infinity\"");
			}
			else sw.Write(value);
		}
		public static void Serialize(double? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value.Value, sw, buffer);
		}

		public static void Serialize(float value, TextWriter sw, char[] buffer)
		{
			if (float.IsNaN(value))
				sw.Write("\"NaN\"");
			else if (float.IsInfinity(value))
			{
				if (float.IsPositiveInfinity(value))
					sw.Write("\"Infinity\"");
				else
					sw.Write("\"-Infinity\"");
			}
			else sw.Write(value);
		}
		public static void Serialize(float? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value.Value, sw, buffer);
		}

		public static decimal DeserializeDecimal(BufferedTextReader sr, ref int nextToken)
		{
			if (nextToken == '"')
			{
				sr.InitBuffer();
				sr.FillUntil('"');
				nextToken = sr.Read(2);
				try
				{
					return sr.BufferToValue(ConvertToDecimal);
				}
				catch (Exception ex)
				{
					throw new SerializationException("Error parsing decimal at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
				}
			}
			var buf = sr.SmallBuffer;
			buf[0] = (char)nextToken;
			var size = sr.ReadNumber(buf, 1) + 1;
			nextToken = sr.Read();
			try
			{
				return ConvertToDecimal(buf, size, sr);
			}
			catch (Exception ex)
			{
				throw new SerializationException("Error parsing decimal at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
			}
		}

		private static decimal ConvertToDecimal(char[] buf, int len, BufferedTextReader sr)
		{
			if (len > 18)
				return ConvertToDecimalGeneric(buf, len);
			var ch = buf[0];
			int i = 0;
			bool neg = false;
			switch (ch)
			{
				case '+':
					i = 1;
					break;
				case '-':
					i = 1;
					neg = true;
					break;
			}
			long value = 0;
			int scale = 0;
			int num;
			for (; i < len; i++)
			{
				ch = buf[i];
				if (ch == '.')
				{
					if (scale != 0)
						throw new SerializationException("Multiple '.' found in decimal value: " + new string(buf, 0, len) + ". At position" + JsonSerialization.PositionInStream(sr));
					scale = len - i - 1;
				}
				else
				{
					num = ch - 48;
					if (num < 0 || num > 9)
						return ConvertToDecimalGeneric(buf, len);
					value = (value << 3) + (value << 1) + num;
				}
			}
			return new decimal((int)value, (int)(value >> 32), 0, neg, (byte)scale);
		}

		private static decimal ConvertToDecimalGeneric(char[] buf, int len)
		{
			return decimal.Parse(new string(buf, 0, len), NumberStyles.AllowDecimalPoint | NumberStyles.AllowExponent | NumberStyles.AllowLeadingSign, Invariant);
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
			if (nextToken == '"')
			{
				sr.InitBuffer();
				sr.FillUntil('"');
				nextToken = sr.Read(2);
				try
				{
					return sr.BufferToValue(ConvertToInt);
				}
				catch (Exception ex)
				{
					throw new SerializationException("Error parsing int at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
				}
			}
			var buf = sr.SmallBuffer;
			buf[0] = (char)nextToken;
			var size = sr.ReadNumber(buf, 1) + 1;
			nextToken = sr.Read();
			try
			{
				return ConvertToInt(buf, size);
			}
			catch (Exception ex)
			{
				throw new SerializationException("Error parsing int at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
			}
		}
		private static int ConvertToInt(char[] buf, int len)
		{
			var ch = buf[0];
			int i = 0;
			int value = 0;
			switch (ch)
			{
				case '+':
					i = 1;
					break;
				case '-':
					for (i = 1; i < len; i++)
					{
						ch = buf[i];
						int ind = buf[i] - 48;
						value = (value << 3) + (value << 1) - ind;
						if (ind < 0 || ind > 9)
							return ConvertToIntGeneric(buf, len);
					}
					return value;
			}
			for (; i < len; i++)
			{
				ch = buf[i];
				int ind = buf[i] - 48;
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9)
					return ConvertToIntGeneric(buf, len);
			}
			return value;
		}
		private static int ConvertToIntGeneric(char[] buf, int len)
		{
			return int.Parse(new string(buf, 0, len), NumberStyles.AllowDecimalPoint | NumberStyles.AllowExponent | NumberStyles.AllowLeadingSign, Invariant);
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
			if (nextToken == '"')
			{
				sr.InitBuffer();
				sr.FillUntil('"');
				nextToken = sr.Read(2);
				try
				{
					return sr.BufferToValue(ConvertToLong);
				}
				catch (Exception ex)
				{
					throw new SerializationException("Error parsing long at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
				}
			}
			var buf = sr.SmallBuffer;
			buf[0] = (char)nextToken;
			var size = sr.ReadNumber(buf, 1) + 1;
			nextToken = sr.Read();
			try
			{
				return ConvertToLong(buf, size);
			}
			catch (Exception ex)
			{
				throw new SerializationException("Error parsing long at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
			}
		}
		private static long ConvertToLong(char[] buf, int len)
		{
			var ch = buf[0];
			int i = 0;
			long value = 0;
			switch (ch)
			{
				case '+':
					i = 1;
					break;
				case '-':
					for (i = 1; i < len; i++)
					{
						ch = buf[i];
						int ind = buf[i] - 48;
						value = (value << 3) + (value << 1) - ind;
						if (ind < 0 || ind > 9)
							return ConvertToLongGeneric(buf, len);
					}
					return value;
			}
			for (; i < len; i++)
			{
				ch = buf[i];
				int ind = buf[i] - 48;
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9)
					return ConvertToLongGeneric(buf, len);
			}
			return value;
		}
		private static long ConvertToLongGeneric(char[] buf, int len)
		{
			return long.Parse(new string(buf, 0, len), NumberStyles.AllowDecimalPoint | NumberStyles.AllowExponent | NumberStyles.AllowLeadingSign, Invariant);
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
			if (nextToken == '"')
			{
				sr.InitBuffer();
				sr.FillUntil('"');
				nextToken = sr.Read(2);
				try
				{
					return sr.BufferToValue(ConvertToDouble);
				}
				catch (Exception ex)
				{
					throw new SerializationException("Error parsing double at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
				}
			}
			var buf = sr.SmallBuffer;
			buf[0] = (char)nextToken;
			var size = sr.ReadNumber(buf, 1) + 1;
			nextToken = sr.Read();
			try
			{
				return ConvertToDouble(buf, size);
			}
			catch (Exception ex)
			{
				throw new SerializationException("Error parsing double at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
			}
		}
		private static double ConvertToDouble(char[] buf, int len)
		{
			if (len > 18)
				return ConvertToDoubleGeneric(buf, len);
			long value = 0;
			var ch = buf[0];
			int i = 0;
			int sign = 1;
			switch (ch)
			{
				case '+':
					i = 1;
					break;
				case '-':
					i = 1;
					sign = -1;
					break;
			}
			for (; i < len; i++)
			{
				ch = buf[i];
				if (ch == '.') break;
				int ind = buf[i] - 48;
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9)
					return ConvertToDoubleGeneric(buf, len);
			}
			if (ch == '.')
			{
				i++;
				long div = 1;
				for (; i < buf.Length && i < len; i++)
				{
					int ind = buf[i] - 48;
					div = (div << 3) + (div << 1);
					value = (value << 3) + (value << 1) + ind;
					if (ind < 0 || ind > 9)
						return ConvertToDoubleGeneric(buf, len);
				}
				return sign * value / (double)div;
			}
			return sign * value;
		}
		private static double ConvertToDoubleGeneric(char[] buf, int len)
		{
			return double.Parse(new string(buf, 0, len), NumberStyles.AllowDecimalPoint | NumberStyles.AllowExponent | NumberStyles.AllowLeadingSign, Invariant);
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
			if (nextToken == '"')
			{
				sr.InitBuffer();
				sr.FillUntil('"');
				nextToken = sr.Read(2);
				try
				{
					return sr.BufferToValue(ConvertToFloat);
				}
				catch (Exception ex)
				{
					throw new SerializationException("Error parsing float at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
				}
			}
			var buf = sr.SmallBuffer;
			buf[0] = (char)nextToken;
			var size = sr.ReadNumber(buf, 1) + 1;
			nextToken = sr.Read();
			try
			{
				return ConvertToFloat(buf, size);
			}
			catch (Exception ex)
			{
				throw new SerializationException("Error parsing float at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
			}
		}

		private static float ConvertToFloat(char[] buf, int len)
		{
			if (len > 18)
				return ConvertToFloatGeneric(buf, len);
			long value = 0;
			var ch = buf[0];
			int i = 0;
			int sign = 1;
			switch (ch)
			{
				case '+':
					i = 1;
					break;
				case '-':
					i = 1;
					sign = -1;
					break;
			}
			for (; i < len; i++)
			{
				ch = buf[i];
				if (ch == '.') break;
				int ind = buf[i] - 48;
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9)
					return ConvertToFloatGeneric(buf, len);
			}
			if (ch == '.')
			{
				i++;
				long div = 1;
				for (; i < buf.Length && i < len; i++)
				{
					int ind = buf[i] - 48;
					div = (div << 3) + (div << 1);
					value = (value << 3) + (value << 1) + ind;
					if (ind < 0 || ind > 9)
						return ConvertToFloatGeneric(buf, len);
				}
				return sign * value / (float)div;
			}
			return sign * value;
		}

		private static float ConvertToFloatGeneric(char[] buf, int len)
		{
			return float.Parse(new string(buf, 0, len), NumberStyles.AllowDecimalPoint | NumberStyles.AllowExponent | NumberStyles.AllowLeadingSign, Invariant);
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
