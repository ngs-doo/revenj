using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;

namespace NGS.Serialization.Json.Converters
{
	public static class DictionaryConverter
	{
		public static void SerializeNullable(Dictionary<string, string> value, StreamWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value, sw);
		}

		public static void Serialize(Dictionary<string, string> value, StreamWriter sw)
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

		public static Dictionary<string, string> Deserialize(StreamReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '{') throw new SerializationException("Expecting '{' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			var res = new Dictionary<string, string>();
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken == '}') return res;
			var key = StringConverter.Deserialize(sr, buffer, nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = JsonSerialization.GetNextToken(sr);
			var value = StringConverter.DeserializeNullable(sr, buffer, nextToken);
			res.Add(key, value);
			while ((nextToken = JsonSerialization.GetNextToken(sr)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				key = StringConverter.Deserialize(sr, buffer, nextToken);
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken != ':') throw new SerializationException("Expecting ':' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
				nextToken = JsonSerialization.GetNextToken(sr);
				value = StringConverter.DeserializeNullable(sr, buffer, nextToken);
				res.Add(key, value);
			}
			if (nextToken != '}') throw new SerializationException("Expecting '}' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			return res;
		}
		public static List<Dictionary<string, string>> DeserializeCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => Deserialize(sr, buffer, next));
		}
		public static List<Dictionary<string, string>> DeserializeNullableCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => Deserialize(sr, buffer, next));
		}
	}
}
