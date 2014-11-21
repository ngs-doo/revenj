using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Runtime.Serialization;

namespace Revenj.Serialization.Json.Converters
{
	public static class DrawingConverter
	{
		public static void Serialize(Color value, TextWriter sw, char[] buffer)
		{
			sw.Write(value.ToArgb());
		}
		public static void Serialize(Color? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value.ToArgb());
		}

		public static Color DeserializeColor(TextReader sr, char[] buffer, ref int nextToken)
		{
			if (nextToken == '"')
			{
				var val = StringConverter.Deserialize(sr, buffer, nextToken);
				nextToken = JsonSerialization.GetNextToken(sr);
				return Color.FromName(val);
			}
			else
			{
				var val = NumberConverter.DeserializeInt(sr, ref nextToken);
				return Color.FromArgb(val);
			}
		}

		public static List<Color> DeserializeColorCollection(TextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<Color>();
			DeserializeColorCollection(sr, buffer, nextToken, res);
			return res;
		}
		public static void DeserializeColorCollection(TextReader sr, char[] buffer, int nextToken, ICollection<Color> res)
		{
			res.Add(DeserializeColor(sr, buffer, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeColor(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<Color?> DeserializeColorNullableCollection(TextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<Color?>();
			DeserializeColorNullableCollection(sr, buffer, nextToken, res);
			return res;
		}
		public static void DeserializeColorNullableCollection(TextReader sr, char[] buffer, int nextToken, ICollection<Color?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for Color value. Expecting number, string or null");
				nextToken = sr.Read();
			}
			else res.Add(DeserializeColor(sr, buffer, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for Color value. Expecting number, string or null");
					nextToken = sr.Read();
				}
				else res.Add(DeserializeColor(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}

		public static void Serialize(RectangleF value, TextWriter sw, char[] buffer)
		{
			sw.Write("{\"X\":");
			sw.Write(value.X);
			sw.Write(",\"Y\":");
			sw.Write(value.Y);
			sw.Write(",\"Width\":");
			sw.Write(value.Width);
			sw.Write(",\"Height\":");
			sw.Write(value.Height);
			sw.Write("}");

		}
		public static void Serialize(RectangleF? value, TextWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value.Value, sw, buffer);
		}

		public static RectangleF DeserializeRectangleF(TextReader sr, char[] buffer, ref int nextToken)
		{
			if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken == '}') return new RectangleF();
			float x = 0, y = 0, w = 0, h = 0;
			do
			{
				var name = StringConverter.Deserialize(sr, buffer, nextToken);
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				nextToken = JsonSerialization.GetNextToken(sr);
				var value = NumberConverter.DeserializeFloat(sr, buffer, ref nextToken);
				switch (name)
				{
					case "X":
					case "x":
						x = value;
						break;
					case "Y":
					case "y":
						y = value;
						break;
					case "Width":
					case "width":
						w = value;
						break;
					case "Height":
					case "height":
						h = value;
						break;
					default:
						throw new SerializationException("Expecting 'X', 'Y', 'Width' or 'Height' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + name);
				}
			} while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',');
			if (nextToken != '}') throw new SerializationException("Expecting '}' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			return new RectangleF(x, y, w, h);
		}

		public static List<RectangleF> DeserializeRectangleFCollection(TextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<RectangleF>();
			DeserializeRectangleFCollection(sr, buffer, nextToken, res);
			return res;
		}
		public static void DeserializeRectangleFCollection(TextReader sr, char[] buffer, int nextToken, ICollection<RectangleF> res)
		{
			res.Add(DeserializeRectangleF(sr, buffer, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(DeserializeRectangleF(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<RectangleF?> DeserializeRectangleFNullableCollection(TextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<RectangleF?>();
			DeserializeRectangleFNullableCollection(sr, buffer, nextToken, res);
			return res;
		}
		public static void DeserializeRectangleFNullableCollection(TextReader sr, char[] buffer, int nextToken, ICollection<RectangleF?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for Color value. Expecting number, string or null");
				nextToken = sr.Read();
			}
			else res.Add(DeserializeRectangleF(sr, buffer, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for Color value. Expecting number, string or null");
					nextToken = sr.Read();
				}
				else res.Add(DeserializeRectangleF(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
	}
}
