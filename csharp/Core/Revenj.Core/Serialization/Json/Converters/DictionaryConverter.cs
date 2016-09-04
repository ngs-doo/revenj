using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
{
	public static class DictionaryConverter
	{
		public static void SerializeNullable(Dictionary<string, string> value, TextWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value, sw);
		}

		public static void Serialize(Dictionary<string, string> value, TextWriter sw)
		{
			sw.Write('{');
			if (value.Count > 0)
			{
				var enumerator = value.GetEnumerator();
				KeyValuePair<string, string> kv;
				var total = value.Count - 1;
				for (var i = 0; enumerator.MoveNext() && i < total; i++)
				{
					kv = enumerator.Current;
					StringConverter.Serialize(kv.Key, sw);
					sw.Write(':');
					StringConverter.SerializeNullable(kv.Value, sw);
					sw.Write(',');
				}
				kv = enumerator.Current;
				StringConverter.Serialize(kv.Key, sw);
				sw.Write(':');
				StringConverter.SerializeNullable(kv.Value, sw);
			}
			sw.Write('}');
		}

		public static Dictionary<string, string> Deserialize(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			var res = new Dictionary<string, string>();
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken == '}') return res;
			var key = StringConverter.Deserialize(sr, nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			var value = StringConverter.DeserializeNullable(sr, nextToken);
			res.Add(key, value);
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				key = StringConverter.Deserialize(sr, nextToken);
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				nextToken = JsonSerialization.GetNextToken(sr);
				value = StringConverter.DeserializeNullable(sr, nextToken);
				res.Add(key, value);
			}
			if (nextToken != '}') throw new SerializationException("Expecting '}' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			return res;
		}
		public static List<Dictionary<string, string>> DeserializeCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => Deserialize(sr, next));
		}
		public static void DeserializeCollection(BufferedTextReader sr, int nextToken, ICollection<Dictionary<string, string>> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => Deserialize(sr, next), res);
		}
		public static List<Dictionary<string, string>> DeserializeNullableCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => Deserialize(sr, next));
		}
		public static void DeserializeNullableCollection(BufferedTextReader sr, int nextToken, ICollection<Dictionary<string, string>> res)
		{
			JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => Deserialize(sr, next), res);
		}
	}
}
