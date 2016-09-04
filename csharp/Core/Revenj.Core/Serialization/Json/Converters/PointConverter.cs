using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Runtime.Serialization;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
{
	public static class PointConverter
	{
		public static void Serialize(Point value, TextWriter sw)
		{
			sw.Write("{\"X\":");
			sw.Write(value.X);
			sw.Write(",\"Y\":");
			sw.Write(value.Y);
			sw.Write("}");
		}
		public static void Serialize(Point? value, TextWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value.Value, sw);
		}

		public static Point DeserializePoint(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken == '}') return new Point();
			var firstName = StringConverter.Deserialize(sr, nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			var firstValue = NumberConverter.DeserializeInt(sr, ref nextToken);
			nextToken = JsonSerialization.MoveToNextToken(sr, nextToken);
			if (nextToken == '}')
			{
				if (firstName == "X")
					return new Point(firstValue, 0);
				else if (firstName == "Y")
					return new Point(0, firstValue);
				else
					throw new SerializationException("Expecting 'X' or 'Y' as property names at position " + JsonSerialization.PositionInStream(sr) + ". Found " + firstName);
			}
			if (nextToken != ',') throw new SerializationException("Expecting ',' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			var secondName = StringConverter.Deserialize(sr, nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			var secondValue = NumberConverter.DeserializeInt(sr, ref nextToken);
			nextToken = JsonSerialization.MoveToNextToken(sr, nextToken);
			if (nextToken != '}')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in point.");
				else throw new SerializationException("Expecting '}' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			if (firstName == "X" && secondName == "Y")
				return new Point(firstValue, secondValue);
			else if (firstName == "Y" && secondName == "X")
				return new Point(secondValue, firstValue);
			throw new SerializationException("Expecting 'X' and 'Y' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + firstName + " and " + secondName);
		}

		public static List<Point> DeserializePointCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializePoint(sr, next));
		}
		public static void DeserializePointCollection(BufferedTextReader sr, int nextToken, ICollection<Point> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializePoint(sr, next), res);
		}
		public static List<Point?> DeserializePointNullableCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeNullableStructCollection(sr, nextToken, next => DeserializePoint(sr, next));
		}
		public static void DeserializePointNullableCollection(BufferedTextReader sr, int nextToken, ICollection<Point?> res)
		{
			JsonSerialization.DeserializeNullableStructCollection(sr, nextToken, next => DeserializePoint(sr, next), res);
		}

		public static void Serialize(PointF value, TextWriter sw)
		{
			sw.Write("{\"X\":");
			sw.Write(value.X);
			sw.Write(",\"Y\":");
			sw.Write(value.Y);
			sw.Write("}");
		}
		public static void Serialize(PointF? value, TextWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value.Value, sw);
		}

		public static PointF DeserializePointF(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken == '}') return new PointF();
			var firstName = StringConverter.Deserialize(sr, nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			var firstValue = NumberConverter.DeserializeFloat(sr, ref nextToken);
			nextToken = JsonSerialization.MoveToNextToken(sr, nextToken);
			if (nextToken == '}')
			{
				if (firstName == "X")
					return new PointF(firstValue, 0);
				else if (firstName == "Y")
					return new PointF(0, firstValue);
				else
					throw new SerializationException("Expecting 'X' or 'Y' as property names at position " + JsonSerialization.PositionInStream(sr) + ". Found " + firstName);
			}
			if (nextToken != ',') throw new SerializationException("Expecting ',' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			var secondName = StringConverter.Deserialize(sr, nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			var secondValue = NumberConverter.DeserializeFloat(sr, ref nextToken);
			nextToken = JsonSerialization.MoveToNextToken(sr, nextToken);
			if (nextToken != '}')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in point.");
				else throw new SerializationException("Expecting '}' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			if (firstName == "X" && secondName == "Y")
				return new PointF(firstValue, secondValue);
			else if (firstName == "Y" && secondName == "X")
				return new PointF(secondValue, firstValue);
			throw new SerializationException("Expecting 'X' and 'Y' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + firstName + " and " + secondName);
		}

		public static List<PointF> DeserializePointFCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializePointF(sr, next));
		}
		public static void DeserializePointFCollection(BufferedTextReader sr, int nextToken, ICollection<PointF> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializePointF(sr, next), res);
		}
		public static List<PointF?> DeserializePointFNullableCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeNullableStructCollection(sr, nextToken, next => DeserializePointF(sr, next));
		}
		public static void DeserializePointFNullableCollection(BufferedTextReader sr, int nextToken, ICollection<PointF?> res)
		{
			JsonSerialization.DeserializeNullableStructCollection(sr, nextToken, next => DeserializePointF(sr, next), res);
		}
	}
}
