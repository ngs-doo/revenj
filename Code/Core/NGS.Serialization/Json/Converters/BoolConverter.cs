using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;

namespace NGS.Serialization.Json.Converters
{
	public static class BoolConverter
	{
		public static void Serialize(bool value, StreamWriter sw)
		{
			if (value)
				sw.Write("true");
			else
				sw.Write("false");
		}
		public static void Serialize(bool? value, StreamWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else if (value == true)
				sw.Write("true");
			else
				sw.Write("false");
		}
		public static bool Deserialize(StreamReader sr, int nextToken)
		{
			if (nextToken == 't')
			{
				if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e')
					return true;
			}
			else if (nextToken == 'f')
			{
				if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e')
					return false;
			}
			throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
		}
		public static List<bool> DeserializeCollection(StreamReader sr, int nextToken)
		{
			var res = new List<bool>();
			if (nextToken == 't')
			{
				if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e')
					res.Add(true);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
			}
			else if (nextToken == 'f')
			{
				if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e')
					res.Add(false);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
			}
			else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 't')
				{
					if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e')
						res.Add(true);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
				}
				else if (nextToken == 'f')
				{
					if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e')
						res.Add(false);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
				}
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}
		public static List<bool?> DeserializeNullableCollection(StreamReader sr, int nextToken)
		{
			var res = new List<bool?>();
			if (nextToken == 't')
			{
				if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e')
					res.Add(true);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true, false or null");
			}
			else if (nextToken == 'f')
			{
				if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e')
					res.Add(false);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true, false or null");
			}
			else if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true, false or null");
			}
			else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true, false or null");
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 't')
				{
					if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e')
						res.Add(true);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true, false or null");
				}
				else if (nextToken == 'f')
				{
					if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e')
						res.Add(false);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true, false or null");
				}
				else if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true, false or null");
				}
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
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
