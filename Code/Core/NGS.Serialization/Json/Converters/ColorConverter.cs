using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Runtime.Serialization;

namespace NGS.Serialization.Json.Converters
{
	public static class ColorConverter
	{
		public static void Serialize(Color value, StreamWriter sw, char[] buffer)
		{
			sw.Write(value.ToArgb());
		}
		public static void Serialize(Color? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value.ToArgb());
		}

		public static Color Deserialize(StreamReader sr, char[] buffer, ref int nextToken)
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

		public static List<Color> DeserializeCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			var res = new List<Color>();
			res.Add(Deserialize(sr, buffer, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(Deserialize(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}

		public static List<Color?> DeserializeNullableCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			var res = new List<Color?>();
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for Color value. Expecting number, string or null");
				nextToken = sr.Read();
			}
			else res.Add(Deserialize(sr, buffer, ref nextToken));
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
				else res.Add(Deserialize(sr, buffer, ref nextToken));
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
