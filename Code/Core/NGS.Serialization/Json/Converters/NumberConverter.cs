using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Runtime.Serialization;

namespace NGS.Serialization.Json.Converters
{
	public static class NumberConverter
	{
		private static readonly CultureInfo Invariant = CultureInfo.InvariantCulture;

		public static void Serialize(decimal value, StreamWriter sw)
		{
			sw.Write(value);
		}
		public static void Serialize(decimal? value, StreamWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static void Serialize(int value, StreamWriter sw)
		{
			sw.Write(value);
		}
		public static void Serialize(int? value, StreamWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static void Serialize(long value, StreamWriter sw, char[] buffer)
		{
			sw.Write(value);
		}
		public static void Serialize(long? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static void Serialize(double value, StreamWriter sw, char[] buffer)
		{
			sw.Write(value);
		}
		public static void Serialize(double? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static void Serialize(float value, StreamWriter sw, char[] buffer)
		{
			sw.Write(value);
		}
		public static void Serialize(float? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static decimal DeserializeDecimal(StreamReader sr, ref int nextToken)
		{
			var negative = nextToken == '-';
			if (negative) nextToken = sr.Read();
			decimal res = 0;
			do
			{
				res = res * 10 + (nextToken - '0');
				nextToken = sr.Read();
			} while (nextToken >= '0' && nextToken <= '9');
			if (nextToken == '.')
			{
				nextToken = sr.Read();
				decimal pow = 0.1m;
				do
				{
					res += pow * (nextToken - 48);
					nextToken = sr.Read();
					pow = pow / 10;
				} while (nextToken >= '0' && nextToken <= '9');
			}
			return negative ? -res : res; ;
		}
		public static List<decimal> DeserializeDecimalCollection(StreamReader sr, int nextToken)
		{
			var res = new List<decimal>();
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
			return res;
		}
		public static List<decimal?> DeserializeDecimalNullableCollection(StreamReader sr, int nextToken)
		{
			var res = new List<decimal?>();
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
			return res;
		}

		public static int DeserializeInt(StreamReader sr, ref int nextToken)
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
		public static List<int> DeserializeIntCollection(StreamReader sr, int nextToken)
		{
			var res = new List<int>();
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
			return res;
		}
		public static List<int?> DeserializeIntNullableCollection(StreamReader sr, int nextToken)
		{
			var res = new List<int?>();
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
			return res;
		}

		public static long DeserializeLong(StreamReader sr, ref int nextToken)
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
		public static List<long> DeserializeLongCollection(StreamReader sr, int nextToken)
		{
			var res = new List<long>();
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
			return res;
		}
		public static List<long?> DeserializeLongNullableCollection(StreamReader sr, int nextToken)
		{
			var res = new List<long?>();
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
			return res;
		}

		public static double DeserializeDouble(StreamReader sr, char[] buffer, ref int nextToken)
		{
			var ind = 0;
			do
			{
				buffer[ind++] = (char)nextToken;
				nextToken = sr.Read();
			} while (ind < buffer.Length && nextToken != ',' && nextToken != '}' && nextToken != ']');
			return double.Parse(new string(buffer, 0, ind), NumberStyles.Float, Invariant);
		}
		public static List<double> DeserializeDoubleCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			var res = new List<double>();
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
			return res;
		}
		public static List<double?> DeserializeDoubleNullableCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			var res = new List<double?>();
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
			return res;
		}

		public static float DeserializeFloat(StreamReader sr, char[] buffer, ref int nextToken)
		{
			var ind = 0;
			do
			{
				buffer[ind++] = (char)nextToken;
				nextToken = sr.Read();
			} while (ind < buffer.Length && nextToken != ',' && nextToken != '}' && nextToken != ']');
			return float.Parse(new string(buffer, 0, ind), NumberStyles.Float, Invariant);
		}
		public static List<float> DeserializeFloatCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			var res = new List<float>();
			res.Add(DeserializeFloat(sr, buffer, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeFloat(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}
		public static List<float?> DeserializeFloatNullableCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			var res = new List<float?>();
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for float value. Expecting number or null");
				nextToken = sr.Read();
			}
			else res.Add(DeserializeFloat(sr, buffer, ref nextToken));
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
				else res.Add(DeserializeFloat(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}
	}
}
