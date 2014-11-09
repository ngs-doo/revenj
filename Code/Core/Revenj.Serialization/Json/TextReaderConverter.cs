using System;
using System.IO;
using Newtonsoft.Json;

namespace Revenj.Serialization
{
	internal class TextReaderConverter : JsonConverter
	{
		public override bool CanConvert(Type objectType)
		{
			return typeof(TextReader).IsAssignableFrom(objectType);
		}

		public override object ReadJson(JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer)
		{
			return new StringReader(reader.Value as string);
		}

		public override void WriteJson(JsonWriter writer, object value, JsonSerializer serializer)
		{
			var tr = value as TextReader;
			writer.WriteValue(tr.ReadToEnd());
		}
	}
}
