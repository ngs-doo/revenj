using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
{
	public static class BoolConverter
	{
		public static void Serialize(bool value, TextWriter sw)
		{
			if (value)
				sw.Write("true");
			else
				sw.Write("false");
		}
		public static void Serialize(bool? value, TextWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else if (value == true)
				sw.Write("true");
			else
				sw.Write("false");
		}
		public static bool Deserialize(BufferedTextReader sr, int nextToken)
		{
			if (nextToken == 't')
			{
				//TODO: isNext !?
				if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e')
					return true;
			}
			else if (nextToken == 'f')
			{
				if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e')
					return false;
			}
			else if (nextToken == '"')
			{
				nextToken = sr.Read();
				if (nextToken == 't')
				{
					if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e' && sr.Read() == '"')
						return true;
				}
				else if (nextToken == 'f')
				{
					if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e' && sr.Read() == '"')
						return false;
				}
			}
			throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
		}
		public static List<bool> DeserializeCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<bool>();
			DeserializeCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeCollection(BufferedTextReader sr, int nextToken, ICollection<bool> res)
		{
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
			else if (nextToken == '"')
			{
				nextToken = sr.Read();
				if (nextToken == 't')
				{
					if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e' && sr.Read() == '"')
						res.Add(true);
				}
				else if (nextToken == 'f')
				{
					if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e' && sr.Read() == '"')
						res.Add(false);
				}
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
				else if (nextToken == '"')
				{
					nextToken = sr.Read();
					if (nextToken == 't')
					{
						if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e' && sr.Read() == '"')
							res.Add(true);
					}
					else if (nextToken == 'f')
					{
						if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e' && sr.Read() == '"')
							res.Add(false);
					}
				}
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<bool?> DeserializeNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<bool?>();
			DeserializeNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeNullableCollection(BufferedTextReader sr, int nextToken, ICollection<bool?> res)
		{
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
			else if (nextToken == '"')
			{
				nextToken = sr.Read();
				if (nextToken == 't')
				{
					if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e' && sr.Read() == '"')
						res.Add(true);
				}
				else if (nextToken == 'f')
				{
					if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e' && sr.Read() == '"')
						res.Add(false);
				}
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
				else if (nextToken == '"')
				{
					nextToken = sr.Read();
					if (nextToken == 't')
					{
						if (sr.Read() == 'r' && sr.Read() == 'u' && sr.Read() == 'e' && sr.Read() == '"')
							res.Add(true);
					}
					else if (nextToken == 'f')
					{
						if (sr.Read() == 'a' && sr.Read() == 'l' && sr.Read() == 's' && sr.Read() == 'e' && sr.Read() == '"')
							res.Add(false);
					}
				}
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for boolean value. Expecting true or false");
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
	}
}
