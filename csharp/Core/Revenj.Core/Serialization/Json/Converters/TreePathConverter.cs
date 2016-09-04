using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
{
	public static class TreePathConverter
	{
		public static void SerializeNullable(TreePath? value, TextWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value.Value, sw);
		}

		public static void Serialize(TreePath value, TextWriter sw)
		{
			sw.Write('"');
			sw.Write(value.ToString());
			sw.Write('"');
		}

		public static TreePath? DeserializeNullable(BufferedTextReader sr, int nextToken)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid null value found at " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return Deserialize(sr, nextToken);
		}

		public static TreePath Deserialize(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			sr.InitBuffer();
			nextToken = sr.FillUntil('"');
			sr.Read();
			return TreePath.Create(sr.BufferToString());
		}
		public static List<TreePath> DeserializeCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<TreePath>();
			DeserializeCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeCollection(BufferedTextReader sr, int nextToken, ICollection<TreePath> res)
		{
			res.Add(Deserialize(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(Deserialize(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<TreePath?> DeserializeNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<TreePath?>();
			DeserializeNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeNullableCollection(BufferedTextReader sr, int nextToken, ICollection<TreePath?> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for string value. Expecting '\"' or null");
			}
			else res.Add(Deserialize(sr, nextToken));
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for string value. Expecting '\"' or null");
				}
				else res.Add(Deserialize(sr, nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
	}
}
